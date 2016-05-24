package com.morecruit.ext.component.debug;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Printer;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.utils.DateUtils;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.IoUtils;
import com.morecruit.ext.utils.Singleton;
import com.morecruit.ext.utils.ThreadUtils;
import com.morecruit.ext.utils.ToastUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tracer for time cost of thread execution. You can call {@link #trace()} or {@link #trace(Looper)}
 * to enable automatic trace for thread with looper.
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class ThreadTracer extends Tracer {

    private final static String TAG = "ThreadTracer";

    // main thread name.
    private final static String THREAD_NAME_MAIN = "main";

    // log file constant.
    private final static String DUMP_FILE_DIR = "thread";
    private final static String DUMP_FILE_SUFFIX = ".txt";

    private final static int FILE_BUFFER_SIZE = 20;
    private final static long FILE_FLUSH_DELAY = 10 * 1000;

    // default time to live of dump files.
    private final static long DEFAULT_DUMP_FILE_TTL = 3 * 24 * 60 * 60 * 1000;

    private final static String PREFERENCE_PREFIX = "ThreadTracer:";
    private final static String PREFERENCE_REPORT_TIMESTAMP = PREFERENCE_PREFIX + "report_timestamp";

    private final static String PACKAGE_ANDROID = "android";

    /**
     * Level for user notification.
     */
    public final static int LEVEL_NOTIFY = 0;
    /**
     * Level for logcat.
     */
    public final static int LEVEL_LOGCAT = 1;
    /**
     * Level for logfile.
     */
    public final static int LEVEL_LOGFILE = 2;
    // length of level array.
    private final static int LEVEL_LENGTH = 3;

    private final long[] mGlobalLevels = createLevels();

    // initialize global levels.
    {
        mGlobalLevels[LEVEL_NOTIFY] = 100;
        mGlobalLevels[LEVEL_LOGCAT] = 100;
        mGlobalLevels[LEVEL_LOGFILE] = 100;
    }

    private final ThreadLocal<long[]> mThreadLevels = new ThreadLocal<long[]>() {
        @Override
        protected long[] initialValue() {
            return createLevels();
        }
    };

    private final ThreadLocal<ThreadRecord> mLocalRecord = new ThreadLocal<ThreadRecord>() {
        @Override
        protected ThreadRecord initialValue() {
            return createRecord();
        }
    };

    private final ThreadLocal<StringBuilder> mLocalStringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(128);
        }
    };

    private final Printer mLooperPrinter = new Printer() {
        @Override
        public void println(String x) {
            handleLooperReport(x);
        }
    };

    private final Context mContext;

    // used only by tracer thread.
    private final ArrayList<String> mIdentifyPackages = new ArrayList<String>();

    // file buffer should be accessed only by tracer thread.
    private final String[] mFileBuffer = new String[FILE_BUFFER_SIZE];
    private int mFileBufferIndex = 0;
    // used only by tracer thread.
    private final Runnable mFlushFileBufferRunnable = new Runnable() {
        @Override
        public void run() {
            flushFileBuffer(mFileBuffer, mFileBufferIndex);
            // reset buffer.
            mFileBufferIndex = 0;
        }
    };
    // used only by tracer thread.
    private final HashMap<String, Writer> mTmpWriters = new HashMap<String, Writer>();

    // time to live of dump files.
    private long mDumpTTL = DEFAULT_DUMP_FILE_TTL;

    private volatile Reporter mReporter;
    private final Object mReportLock = new Object();

    private ThreadTracer(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Trace looper associate with current thread automatically.
     *
     * @throws RuntimeException if current thread has no looper.
     */
    public void trace() {
        trace(Looper.myLooper());
    }

    /**
     * Trace corresponding looper automatically.
     *
     * @param looper looper to trace.
     * @throws RuntimeException if looper is not valid.
     */
    public void trace(Looper looper) {
        if (looper == null) {
            throw new RuntimeException("null looper");
        }
        if (looper == getTracerLooper()) {
            // ignore looper of tracer self.
            return;
        }
        ThreadUtils.addLooperPrinter(looper, mLooperPrinter);
    }

    /**
     * Manually report before a certain task on current thread. Remember to call {@link #reportEnd} after the execution of this task.
     *
     * @param msg start msg.
     */
    public void reportStart(String msg) {
        performReportStart(msg);
    }

    /**
     * Manually report after a certain task on current thread. This should be called only after {@link #reportStart}.
     *
     * @param msg end msg.
     */
    public void reportEnd(String msg) {
        performReportEnd(msg);
    }

    /**
     * Set current thread's level for corresponding type.
     *
     * @param levelType level type, see {@link #LEVEL_NOTIFY}, {@link #LEVEL_LOGCAT}, {@link #LEVEL_LOGFILE}.
     * @param level     level in ms.
     */
    public void setLevel(int levelType, long level) {
        setLevel(mThreadLevels.get(), levelType, level);
    }

    /**
     * Set corresponding looper's level for corresponding type.
     *
     * @param looper    looper to set level.
     * @param levelType level type, see {@link #LEVEL_NOTIFY}, {@link #LEVEL_LOGCAT}, {@link #LEVEL_LOGFILE}.
     * @param level     level in ms.
     */
    public void setLevel(Looper looper, int levelType, long level) {
        if (looper == null) {
            throw new RuntimeException("null looper");
        }
        final int fLevelType = levelType;
        final long fLevel = level;
        Handler handler = new Handler(looper);
        handler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                setLevel(mThreadLevels.get(), fLevelType, fLevel);
            }
        });
    }

    /**
     * Get current thread's level for corresponding type.
     *
     * @param levelType level type, see {@link #LEVEL_NOTIFY}, {@link #LEVEL_LOGCAT}, {@link #LEVEL_LOGFILE}.
     * @return current thread's level.
     */
    public long getLevel(int levelType) {
        return getLevel(mThreadLevels.get(), levelType);
    }

    /**
     * Set global level for corresponding type. This will be used for thread which has no valid level.
     *
     * @param levelType level type, see {@link #LEVEL_NOTIFY}, {@link #LEVEL_LOGCAT}, {@link #LEVEL_LOGFILE}.
     * @param level     level in ms.
     */
    public void setGlobalLevel(int levelType, long level) {
        setLevel(mGlobalLevels, levelType, level);
    }

    /**
     * Get global level for corresponding type.
     *
     * @param levelType level type, see {@link #LEVEL_NOTIFY}, {@link #LEVEL_LOGCAT}, {@link #LEVEL_LOGFILE}.
     * @return global level.
     */
    public long getGlobalLevel(int levelType) {
        return getLevel(mGlobalLevels, levelType);
    }

    private static void setLevel(long[] levels, int levelType, long level) {
        if (levelType < 0 || levelType >= levels.length) {
            throw new RuntimeException("invalid level type " + levelType);
        }
        levels[levelType] = level;
    }

    private static long getLevel(long[] levels, int levelType) {
        if (levelType < 0 || levelType >= levels.length) {
            throw new RuntimeException("invalid level type " + levelType);
        }
        return levels[levelType];
    }

    /**
     * Add package to identify.
     *
     * @param packageName package name.
     */
    public void addIdentifyPackage(final String packageName) {
        getTracerHandler().post(new Runnable() {
            @Override
            public void run() {
                // access through tracer thread.
                mIdentifyPackages.add(packageName);
            }
        });
    }

    /**
     * Get dir path for dump files.
     *
     * @return dir path for dump files.
     */
    public String getDumpDir() {
        return getTraceDir(mContext, DUMP_FILE_DIR);
    }

    /**
     * Set the time to live of dump files, any files beyond this will be automatically delete.
     *
     * @param timeToLiveInMillis time to live in milliseconds. -1 for no ttl.
     */
    public void setDumpTTL(long timeToLiveInMillis) {
        mDumpTTL = timeToLiveInMillis;
    }

    private void handleLooperReport(String traceMsg) {
        if (isLooperStartMsg(traceMsg)) {
            performReportStart(traceMsg);

        } else if (isLooperEndMsg(traceMsg)) {
            // ignore looper end msg.
            performReportEnd("");
        }
    }

    private void performReportStart(String msg) {
        ThreadRecord record = mLocalRecord.get();
        record.realStartTime = now(false);
        record.threadStartTime = now(true);
        record.startMsg = msg;
    }

    private void performReportEnd(String msg) {
        ThreadRecord record = mLocalRecord.get();
        record.realEndTime = now(false);
        record.threadEndTime = now(true);
        record.endMsg = msg;
        // finish record if has valid start info.
        boolean finish = record.realStartTime != 0;
        if (finish) {
            finishRecord(record);
            // reset record for next use & avoid mismatch use.
            record.reset();
        }
    }

    private void finishRecord(ThreadRecord record) {
        long cost = record.realEndTime - record.realStartTime;
        long level;

        level = getProperLevel(LEVEL_NOTIFY);
        if (level >= 0 && cost >= level) {
            notifyRecord(record);
        }

        level = getProperLevel(LEVEL_LOGCAT);
        if (level >= 0 && cost >= level) {
            logcatRecord(record);
        }

        level = getProperLevel(LEVEL_LOGFILE);
        if (level >= 0 && cost >= level) {
            logfileRecord(record);
        }
    }

    private void notifyRecord(ThreadRecord record) {
        if (!DebugConfig.isPackageDebuggable(mContext)) {
            // notify only package debuggable.
            return;
        }
        final String msg = generateRecordSummary(record);
        ToastUtils.show(mContext, msg);
    }

    private void logcatRecord(ThreadRecord record) {
        final String msg = generateRecordSummary(record);
        Logger.w(TAG, msg);
    }

    private void logfileRecord(ThreadRecord record) {
        final long timestamp = System.currentTimeMillis();
        final String msg = generateRecordSummary(record);
        // use trace thread to log file.
        getTracerHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mFileBufferIndex >= mFileBuffer.length) {
                    // buffer overflow, should never happen.
                    return;
                }
                String time = DateUtils.getDate(timestamp);
                StringBuilder sb = getLocalStringBuilder(true);
                mFileBuffer[mFileBufferIndex++] = sb.append(msg).append('\t').append(time).toString();
                boolean flushNow = mFileBufferIndex >= mFileBuffer.length;
                scheduleFlushFileBuffer(flushNow ? 0 : FILE_FLUSH_DELAY);
            }
        });
    }

    private long getProperLevel(int levelType) {
        long level = getLevel(levelType);
        return level >= 0 ? level : getGlobalLevel(levelType);
    }

    /**
     * Schedule flush file buffer, this should be called only on trace thread.
     */
    private void scheduleFlushFileBuffer(long delay) {
        throwIfNotTracerThread();
        getTracerHandler().removeCallbacks(mFlushFileBufferRunnable);
        if (delay > 0) {
            getTracerHandler().postDelayed(mFlushFileBufferRunnable, delay);
        } else {
            mFlushFileBufferRunnable.run();
        }
    }

    /**
     * Flush file buffer, this should be called only on trace thread.
     */
    private void flushFileBuffer(String[] buffer, int count) {
        throwIfNotTracerThread();
        if (buffer == null || buffer.length == 0 || count == 0) {
            // null buffer.
            return;
        }
        String dir = getDumpDir();
        if (dir == null) {
            return;
        }

        // delete outmoded files beyond ttl (ignored in package-debuggable mode).
        if (!DebugConfig.isPackageDebuggable(mContext)) {
            final long now = System.currentTimeMillis();
            final long ttl = mDumpTTL;
            FileUtils.delete(new File(dir), ttl < 0 ? null : new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return now - pathname.lastModified() > ttl;
                }
            });
        }

        // ensure dir.
        FileUtils.mkdirs(new File(dir));

        HashMap<String, Writer> writers = mTmpWriters;
        try {
            String date = DateUtils.getDate("yyyy-MM-dd", System.currentTimeMillis());
            for (int i = 0; i < buffer.length && i < count; i++) {
                String str = buffer[i];
                if (str == null) {
                    continue;
                }
                String recordName = generateRecordFilename(str);
                StringBuilder sb = getLocalStringBuilder(true);
                sb.append(date);
                if (recordName != null) {
                    sb.append('-').append(recordName);
                }
                sb.append(DUMP_FILE_SUFFIX);

                String fileName = sb.toString();
                Writer writer = writers.get(fileName);
                if (writer == null) {
                    writer = new FileWriter(new File(dir, fileName), true);
                    writers.put(fileName, writer);
                }
                writer.write(str);
                writer.write('\n');
                // release buffer.
                buffer[i] = null;
            }

        } catch (IOException e) {
            Logger.w(TAG, "fail to flush file buffer", e);
        } finally {
            for (Writer writer : writers.values()) {
                IoUtils.closeSilently(writer);
            }
            // clear after use.
            writers.clear();
        }
    }

    private String generateRecordSummary(ThreadRecord record) {
        if (record.summary == null) {
            StringBuilder sb = getLocalStringBuilder(true);
            sb.append(record.threadName)
                    .append('\t').append(record.realEndTime - record.realStartTime)
                    .append('\t').append(record.threadEndTime - record.threadStartTime);
            if (!TextUtils.isEmpty(record.startMsg)) {
                sb.append('\t').append(record.startMsg);
            }
            if (!TextUtils.isEmpty(record.endMsg)) {
                sb.append('\t').append(record.endMsg);
            }
            record.summary = sb.toString();
            // clear after use.
            sb.setLength(0);
        }
        return record.summary;
    }

    /**
     * Generate file name for record summary, see {@link #generateRecordSummary}. This should be called only on trace thread.
     */
    private String generateRecordFilename(String recordSummary) {
        StringBuilder sb = getLocalStringBuilder(true);
        try {
            // thread name.
            int threadNameIndex = recordSummary.indexOf('\t');
            if (threadNameIndex > 0) {
                sb.append(recordSummary, 0, threadNameIndex);
            }
            // package name.
            String pkgName = generateIdentifyPackage(recordSummary);
            if (pkgName != null) {
                if (sb.length() != 0) sb.append('-');
                sb.append(pkgName);
            }
            return sb.toString();
        } finally {
            // clear after use.
            sb.setLength(0);
        }
    }

    /**
     * Generate identify package name for record summary. This should be called only on trace thread.
     */
    private String generateIdentifyPackage(String recordSummary) {
        // prefer user-defined package.
        for (String pkgName : mIdentifyPackages) {
            if (recordSummary.contains(pkgName)) {
                return pkgName;
            }
        }
        // use context package name.
        if (recordSummary.contains(mContext.getPackageName())) {
            return mContext.getPackageName();
        }
        // use android.
        if (recordSummary.contains(PACKAGE_ANDROID)) {
            return PACKAGE_ANDROID;
        }
        return null;
    }

    // ------------- report ------------

    /**
     * Set thread trace reporter.
     *
     * @param reporter thread trace reporter.
     */
    public void setReporter(final Reporter reporter) {
        if (mReporter == reporter) {
            return;
        }
        synchronized (this) {
            if (mReporter == reporter) {
                return;
            }
            mReporter = reporter;
            if (reporter != null) {
                runOnTracerThread(new Runnable() {
                    @Override
                    public void run() {
                        handleReport(reporter);
                    }
                });
            }
        }
    }

    private void handleReport(Reporter reporter) {
        synchronized (mReportLock) {
            String dir = getDumpDir();
            if (dir == null) {
                return;
            }
            final long lastTimestamp = obtainPreferences().getLong(PREFERENCE_REPORT_TIMESTAMP, 0);
            final long startOfToday = DateUtils.getStartOfDay(System.currentTimeMillis());
            File[] list = (new File(dir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    long time = pathname.lastModified();
                    // after prev report & before today.
                    return time > lastTimestamp && time < startOfToday;
                }
            });
            boolean reported = true;
            if (list != null && list.length != 0) {
                reported = reporter.onReport(list);
            }
            if (reported) {
                // remember report time stamp.
                obtainPreferences().edit().putLong(PREFERENCE_REPORT_TIMESTAMP, startOfToday).commit();
            }
        }
    }

    // ------------- pref --------------
    private SharedPreferences obtainPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    // ------------- utils -------------

    /**
     * Get thread local string builder, this is re-used, so use as soon as retrieve it.
     */
    private StringBuilder getLocalStringBuilder(boolean clear) {
        StringBuilder sb = mLocalStringBuilder.get();
        if (clear) {
            sb.setLength(0);
        }
        return sb;
    }

    private static boolean isLooperStartMsg(String msg) {
        return msg != null && msg.length() != 0 && msg.charAt(0) == '>';
    }

    private static boolean isLooperEndMsg(String msg) {
        return msg != null && msg.length() != 0 && msg.charAt(0) == '<';
    }

    private static ThreadRecord createRecord() {
        String threadName;
        if (ThreadUtils.isMainThread()) {
            threadName = THREAD_NAME_MAIN;
        } else {
            threadName = Thread.currentThread().getName();
        }
        return new ThreadRecord(threadName);
    }

    private static long[] createLevels() {
        long[] levels = new long[LEVEL_LENGTH];
        levels[LEVEL_NOTIFY] = -1;
        levels[LEVEL_LOGCAT] = -1;
        levels[LEVEL_LOGFILE] = -1;
        return levels;
    }

    private static long now(boolean threadTime) {
        return threadTime ? SystemClock.currentThreadTimeMillis() : SystemClock.uptimeMillis();
    }

    /**
     * Record for single thread.
     */
    final static class ThreadRecord {

        final String threadName;

        long realStartTime;
        long realEndTime;

        long threadStartTime;
        long threadEndTime;

        String startMsg;
        String endMsg;

        String summary;

        ThreadRecord(String threadName) {
            this.threadName = threadName;
        }

        public void reset() {
            realStartTime = realEndTime = 0;
            threadStartTime = threadEndTime = 0;
            startMsg = endMsg = null;
            summary = null;
        }
    }

    /**
     * Reporter for thread trace.
     */
    public static interface Reporter {
        /**
         * Called when should perform report.
         *
         * @param files files to report.
         * @return true if report succeeds, false otherwise.
         */
        public boolean onReport(File[] files);
    }

    // ----------------- singleton ----------------
    private final static Singleton<ThreadTracer, Context> sSingleton = new Singleton<ThreadTracer, Context>() {
        @Override
        protected ThreadTracer create(Context context) {
            return new ThreadTracer(context);
        }
    };

    public static ThreadTracer getInstance(Context context) {
        return sSingleton.get(context);
    }
}