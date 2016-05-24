package com.morecruit.ext.component.debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.utils.DateUtils;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.MemoryUtils;
import com.morecruit.ext.utils.OomUtils;
import com.morecruit.ext.utils.Singleton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Writer;

/**
 * CrashHandler，处理没有catch的exception
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public class UncaughtExceptionTracer extends Tracer implements Thread.UncaughtExceptionHandler {

    private final static String TAG = "UncaughtExceptionManager";

    private final static String LOG_DIR_NAME = "log";

    // 默认log过时时间，7天
    private final static long LOG_OUT_TIME = 1000 * 60 * 60 * 24 * 7;

    private final static int LOGCAT_MAX_LENGTH = 200000;

    private final static String PREFERENCE_PREFIX = "UncaughtExceptionManager:";
    private final static String PREFERENCE_REPORT_LOG_TIMESTAMP = PREFERENCE_PREFIX + "report_log_timestamp";
    private final static String PREFERENCE_REPORT_HPROF_TIMESTAMP = PREFERENCE_PREFIX + "report_hprof_timestamp";

    private final static Thread.UncaughtExceptionHandler sDefaultParent
            = Thread.getDefaultUncaughtExceptionHandler();

    private final Context mContext;
    private volatile Thread.UncaughtExceptionHandler mParent;
    private volatile UncaughtExceptionInterceptor mInterceptor;

    private volatile UncaughtExceptionReporter mReporter;
    private final Object mReportLogLock = new Object();
    private final Object mReportHprofLock = new Object();

    private volatile PackageInfo mPackageInfo;

    private volatile boolean mCrashing = false;

    private UncaughtExceptionTracer(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 初始化
     */
    public void install() {
        if (this != Thread.getDefaultUncaughtExceptionHandler()) {
            synchronized (this) {
                Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
                if (this != defaultHandler) {
                    assignParent(defaultHandler);
                    Thread.setDefaultUncaughtExceptionHandler(this);
                }
            }
        }
    }

    /**
     * 设置UncaughtExceptionInterceptor拦截器
     *
     * @param interceptor 没有catch到的exception拦截器
     */
    public void setInterceptor(UncaughtExceptionInterceptor interceptor) {
        mInterceptor = interceptor;
    }

    /**
     * 删除过时的log
     */
    public void deleteLogs() {
        deleteLogs(LOG_OUT_TIME);
    }

    /**
     * 删除过时的log，带超时参数
     *
     * @param timeout 过时的时间，默认为7天 {@code LOG_OUT_TIME}.
     */
    public void deleteLogs(final long timeout) {
        final File logDir = getLogDir();
        if (logDir == null) {
            return;
        }
        try {
            final long currTime = System.currentTimeMillis();
            File[] files = logDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File f = new File(dir, filename);
                    return currTime - f.lastModified() > timeout;
                }
            });
            if (files != null) {
                for (File f : files) {
                    FileUtils.delete(f);
                }
            }
        } catch (Exception e) {
            Logger.v(TAG, "exception occurs when deleting outmoded logs", e);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 不要重复进入，避免crash上报或方法本身crash的时候无限地loop
        if (mCrashing) return;
        mCrashing = true;

        try {
            UncaughtExceptionInterceptor interceptor = mInterceptor;
            if (interceptor != null && interceptor.onInterceptExceptionBefore(thread, ex)) {
                return;
            }

            // 自己处理.
            onUncaughtException(thread, ex);
            // 报告给 exception manager.
            ExceptionTracer.getInstance().trace(ex);
            if (interceptor != null && interceptor.onInterceptExceptionAfter(thread, ex)) {
                return;
            }
        } catch (Throwable t) {
            // 安全起见catch一下
        }

        // parent & clear job.
        boolean handled = false;
        try {
            handled = deliverUncaughtExceptionToParent(thread, ex);
        } catch (Throwable t) {
            // 安全起见catch一下
        } finally {
            if (!handled) {
                // 为了保证进程拜拜不择手段
                exit();
            }
        }
    }

    /**
     * 当有未catch的exception发生的时候被调用
     */
    protected void onUncaughtException(Thread thread, Throwable ex) {
        LogWriter logWriter = null;
        try {
            File dir = getLogDir();
            if (dir == null) {
                return;
            }

            File logFile = new File(dir, getLogName());
            logWriter = new LogFileWriter(logFile);

            logWriter.write("\t\n==================BasicInfo==================\t\n");
            writeBasicInfo(logWriter, thread);
            writeException(logWriter, ex);
            logWriter.write("\t\n==================MemoryInfo=================\t\n");
            writeMemoryInfo(logWriter);
            logWriter.write("\t\n=============================================\t\n");
            // flush before writing logcat.
            logWriter.flush();
            writeLogcat(logWriter);

        } catch (Throwable t) {
            Logger.d(TAG, "exception occurs when handling uncaught exception: " + ex.getMessage(), t);
        } finally {
            if (logWriter != null) {
                try {
                    logWriter.close();
                } catch (IOException e) {
                    // empty.
                }
            }
        }
    }

    private void assignParent(Thread.UncaughtExceptionHandler parent) {
        if (parent != this) {
            mParent = parent;
        }
    }

    private boolean deliverUncaughtExceptionToParent(Thread thread, Throwable ex) {
        Thread.UncaughtExceptionHandler parent = mParent;
        if (parent == null || parent == this) {
            parent = sDefaultParent;
        }
        if (parent == null || parent == this) {
            return false;
        }
        parent.uncaughtException(thread, ex);
        return true;
    }

    private void writeBasicInfo(LogWriter writer, Thread thread) throws IOException {
        // prepare package info.
        PackageInfo pkgInfo = getPackageInfo();
        // perform write.
        writer.write("APP_VERSION:" + (pkgInfo != null ? pkgInfo.versionName : null) + "|" + (pkgInfo != null ? pkgInfo.versionCode : null) + "\t\n");
        writer.write("PHONE_MODEL:" + Build.MODEL + "\t\n");
        writer.write("ANDROID_SDK:" + Build.VERSION.SDK + "|" + Build.VERSION.SDK_INT + "\t\n");
        writer.write("UID:" + android.os.Process.myUid() + "\t\n");
        writer.write("PROCESS:" + android.os.Process.myPid() + "\t\n");
        writer.write("THREAD:" + (thread != null ? thread.getName() : null) + "\t\n");
        writer.write(DateUtils.getDate() + "\t\n");
    }

    private void writeException(LogWriter writer, Throwable ex) throws IOException {
        writer.write(Log.getStackTraceString(ex));
    }

    private void writeMemoryInfo(LogWriter writer) throws IOException {
        writer.write(MemoryUtils.getMemoryStats(mContext));
    }

    private void writeLogcat(LogWriter writer) throws IOException {
        writer.write(DebugUtils.dumpLogcat(LOGCAT_MAX_LENGTH));
    }

    private PackageInfo getPackageInfo() {
        if (mPackageInfo == null) {
            synchronized (this) {
                if (mPackageInfo == null) {
                    try {
                        mPackageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        // empty.
                    }
                }
            }
        }
        return mPackageInfo;
    }

    private File getLogDir() {
        String path = getTraceDir(mContext, LOG_DIR_NAME);
        if (path == null) {
            return null;
        }
        File dir = new File(path);
        if (dir.isFile()) {
            // in case.
            FileUtils.delete(dir);
        }
        if (!dir.exists()) {
            return dir.mkdirs() ? dir : null;
        } else {
            return dir;
        }
    }

    // ---------------- report ----------------
    public void setReporter(UncaughtExceptionReporter reporter) {
        if (mReporter == reporter) {
            return;
        }
        synchronized (this) {
            if (mReporter == reporter) {
                return;
            }
            mReporter = reporter;
            if (reporter != null) {
                // do report when new reporter prepared.
                getTracerHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        handleReportLog();
                        handleReportHprof();
                    }
                });
            }
        }
    }

    private void handleReportLog() {
        UncaughtExceptionReporter reporter = mReporter;
        if (reporter == null) {
            return;
        }
        synchronized (mReportLogLock) {
            // obtain log files.
            File dir = getLogDir();
            if (dir == null) {
                return;
            }
            final long now = System.currentTimeMillis();
            final long last = obtainPreference().getLong(PREFERENCE_REPORT_LOG_TIMESTAMP, 0);
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.lastModified() > last;
                }
            });
            // perform report.
            boolean reported = true;
            if (files != null && files.length > 0) {
                reported = reporter.onReportLog(files);
            }
            if (reported) {
                obtainPreference().edit().putLong(PREFERENCE_REPORT_LOG_TIMESTAMP, now).commit();
            }
        }
    }

    private void handleReportHprof() {
        UncaughtExceptionReporter reporter = mReporter;
        if (reporter == null) {
            return;
        }
        synchronized (mReportHprofLock) {
            // obtain hprof files.
            String dir = OomUtils.getHprofDir(mContext);
            if (dir == null) {
                return;
            }
            final long now = System.currentTimeMillis();
            final long last = obtainPreference().getLong(PREFERENCE_REPORT_HPROF_TIMESTAMP, 0);
            File[] files = (new File(dir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.lastModified() > last;
                }
            });
            // perform report.
            boolean reported = true;
            if (files != null && files.length > 0) {
                reported = reporter.onReportHprof(files);
            }
            if (reported) {
                obtainPreference().edit().putLong(PREFERENCE_REPORT_HPROF_TIMESTAMP, now).commit();
            }
        }
    }

    // -------------- preference --------------
    private SharedPreferences obtainPreference() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    // ----------------- utils ----------------
    private static String getLogName() {
        return DateUtils.getDate() + ".log";
    }

    private static void exit() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private interface LogWriter {

        void close() throws IOException;

        void flush() throws IOException;

        void write(String str) throws IOException;
    }

    private static class LogFileWriter implements LogWriter {

        private final Writer mWriter;

        public LogFileWriter(File file) throws IOException {
            mWriter = new BufferedWriter(new FileWriter(file));
        }

        @Override
        public void close() throws IOException {
            mWriter.close();
        }

        @Override
        public void flush() throws IOException {
            mWriter.flush();
        }

        @Override
        public void write(String str) throws IOException {
            Logger.e(TAG, str);
            mWriter.write(str);
        }
    }

    public interface UncaughtExceptionInterceptor {
        /**
         * Called before this uncaught exception be handled by {@link UncaughtExceptionTracer}.
         *
         * @return true if intercepted, which means this event won't be handled by {@link UncaughtExceptionTracer}.
         */
        boolean onInterceptExceptionBefore(Thread t, Throwable ex);

        /**
         * Called after this uncaught exception be handled by {@link UncaughtExceptionTracer} (but before {@link UncaughtExceptionTracer}'s parent).
         *
         * @return true if intercepted, which means this event won't be handled by {@link UncaughtExceptionTracer}'s parent.
         */
        boolean onInterceptExceptionAfter(Thread t, Throwable ex);
    }

    public interface UncaughtExceptionReporter {
        /**
         * Called when should report crash logs. This is called async.
         *
         * @param logFiles log files.
         */
        boolean onReportLog(File[] logFiles);

        /**
         * Called when should report hprof files. This is called async.
         *
         * @param hprofFiles hprof files.
         */
        boolean onReportHprof(File[] hprofFiles);
    }

    // -------------- 单例 --------------

    private static Singleton<UncaughtExceptionTracer, Context> sSingleton = new Singleton<UncaughtExceptionTracer, Context>() {
        @Override
        protected UncaughtExceptionTracer create(Context context) {
            return new UncaughtExceptionTracer(context);
        }
    };

    public static UncaughtExceptionTracer getInstance(Context context) {
        return sSingleton.get(context);
    }
}
