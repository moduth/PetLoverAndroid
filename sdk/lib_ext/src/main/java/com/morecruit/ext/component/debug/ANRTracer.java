package com.morecruit.ext.component.debug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;
import android.widget.Toast;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.utils.DateUtils;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.IoUtils;
import com.morecruit.ext.utils.PropertyUtils;
import com.morecruit.ext.utils.ThreadUtils;
import com.morecruit.ext.utils.ToastUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ANR检测的tracer. 使用{@link #install}开启.
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class ANRTracer extends Tracer {

    private final static String TAG = "ANRTracer";

    // anr tolerance is 5s.
    private final static long ANR_TOLERANCE = 5 * 1000;
    private final static String ANR_TRACE_PATH = readANRTracePath();

    // dump file constant.
    private final static String DUMP_FILE_DIR = "anr";
    private final static String DUMP_FILE_SUFFIX = ".txt";
    // default time to live of dump files: 3days.
    private final static long DUMP_FILE_TTL = 3 * 24 * 60 * 60 * 1000;

    // file buffer size.
    private final static int BUFFER_SIZE = 8192;

    // date related.
    private final static String DATE_PATTERN = "yyyy-MM-dd_HH-mm-ss.SSS";

    private final static String PREFERENCE_PREFIX = "ANRTracer:";
    private final static String PREFERENCE_REPORT_TIMESTAMP = PREFERENCE_PREFIX + "report_timestamp";

    private final Context mContext;
    private final ANRHandler mANRHandler = new ANRHandler();
    private final AtomicBoolean mInstalled = new AtomicBoolean(false);

    // current record, should ONLY be accessed on main thread.
    private final Record mCurrentRecord = new Record();

    private final Printer mLooperPrinter = new Printer() {
        @Override
        public void println(String x) {
            handleLooperReport(x);
        }
    };

    // time to live of dump files.
    private long mDumpTTL = DUMP_FILE_TTL;

    private final AtomicReference<Reporter> mReporter = new AtomicReference<Reporter>();

    private ANRTracer(Context context) {
        mContext = context;
    }

    /**
     * Install this tracer for automatic anr trace.
     */
    public void install() {
        if (!mInstalled.getAndSet(true)) {
            ThreadUtils.addLooperPrinter(Looper.getMainLooper(), mLooperPrinter);
        }
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

    /**
     * Set thread trace reporter.
     *
     * @param reporter thread trace reporter.
     */
    public void setReporter(final Reporter reporter) {
        if (mReporter.getAndSet(reporter) != reporter) {
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
        synchronized (mReporter) {
            String dir = getDumpDir();
            if (dir == null) {
                return;
            }
            final long now = System.currentTimeMillis();
            final long last = obtainPreferences().getLong(PREFERENCE_REPORT_TIMESTAMP, 0);
            File[] list = (new File(dir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.lastModified() > last;
                }
            });
            boolean reported = true;
            if (list != null && list.length != 0) {
                reported = reporter.onReport(list);
            }
            if (reported) {
                // remember report time stamp.
                obtainPreferences().edit().putLong(PREFERENCE_REPORT_TIMESTAMP, now).commit();
            }
        }
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
        Record record = mCurrentRecord;
        record.startTime = now();
        record.msg = msg;
        // schedule check on trace thread.
        scheduleCheckRecord(record);
    }

    private void performReportEnd(String msg) {
        Record record = mCurrentRecord;
        record.endTime = now();
        // un-schedule check on trace thread.
        unscheduleCheckRecord(record);
        // reset record when end.
        record.reset();
    }

    private void scheduleCheckRecord(Record record) {
        Message msg = Message.obtain(mANRHandler, ANRHandler.WHAT_CHECK_RECORD, record);
        mANRHandler.sendMessageDelayed(msg, ANR_TOLERANCE);
    }

    private void unscheduleCheckRecord(Record record) {
        mANRHandler.removeMessages(ANRHandler.WHAT_CHECK_RECORD, record);
    }

    private void performCheckRecord(Record record) {
        long startTime = record.startTime;
        if (startTime == 0) {
            // invalid or ended record.
            return;
        }
        long endTime = record.endTime;
        if (endTime == 0) {
            // not end yet, use now as check time.
            endTime = now();
        }

        if (endTime - startTime >= ANR_TOLERANCE) {
            // anr occurs. copy in case of record reuse.
            final Record copy = record.copy();
            // ensure record is valid (thread issue).
            if (copy.startTime != 0) {
                // handle anr with 1s delay.
                getTracerHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleANR(copy);
                    }
                }, 1000);
            }
        }
    }

    private void handleANR(Record record) {
        String dump = dumpRecord(record);
        // notify.
        notifyANR(dump);

        // write information into logcat & file.
        Writer logcatWriter = null;
        Writer logfileWriter = null;
        try {
            File logfile = generateLogfile();
            logcatWriter = new LogcatWriter(Log.WARN, TAG);
            logfileWriter = logfile == null ? null
                    : new BufferedWriter(new FileWriter(logfile));

            // write record dump.
            writeString(dump, logcatWriter, logfileWriter);
            // write thread trace.
            writeString("\n\n", logcatWriter, logfileWriter);
            writeThreadTrace(logcatWriter, logfileWriter);
            // write anr trace (only to file).
            writeString("\n\n", logfileWriter);
            writeANRTrace(logfileWriter);
        } catch (Throwable e) {
            Logger.w(TAG, "fail to handle anr for record: " + record.msg);
        } finally {
            IoUtils.closeSilently(logcatWriter);
            IoUtils.closeSilently(logfileWriter);
        }
    }

    private void notifyANR(String detail) {
        if (DebugConfig.isPackageDebuggable(mContext)) {
            // notify only package debuggable.
            ToastUtils.show(mContext, "anr occurs in " +
                    mContext.getPackageName() + "\n\n" + detail, Toast.LENGTH_LONG);
        }
    }

    private void writeString(String str, Writer... writers) throws IOException {
        for (Writer w : writers) {
            if (w != null) {
                w.write(str);
            }
        }
    }

    private void writeThreadTrace(Writer... writers) throws IOException {
        // TODO need more further consideration about the necessity.
    }

    private void writeANRTrace(Writer... writers) throws IOException {
        if (TextUtils.isEmpty(ANR_TRACE_PATH)) {
            return;
        }
        File traceFile = new File(ANR_TRACE_PATH);
        if (!traceFile.exists() || !traceFile.isFile()) {
            return;
        }
        // write anr head.
        writeString("anr traces:\n", writers);
        // write anr content.
        Reader reader = new BufferedReader(new FileReader(traceFile));
        char[] buffer = new char[BUFFER_SIZE];
        int count;
        while ((count = reader.read(buffer)) > 0) {
            for (Writer w : writers) {
                if (w != null) {
                    w.write(buffer, 0, count);
                }
            }
        }
    }

    private String dumpRecord(Record record) {
        int length = 128 + (record.msg == null ? 0 : record.msg.length());
        StringBuilder sb = new StringBuilder(length);
        sb.append("startTime: ").append(DateUtils.getDate(DATE_PATTERN, record.startTime));
        sb.append("\nendTime: ").append(DateUtils.getDate(DATE_PATTERN, record.endTime));
        if (!TextUtils.isEmpty(record.msg)) {
            sb.append("\ndetail: ").append(record.msg);
        }
        return sb.toString();
    }

    private File generateLogfile() {
        String dir = getDumpDir();
        if (dir == null) {
            return null;
        }

        File dirFile = new File(dir);
        // delete outmoded files beyond ttl (ignored in package-debuggable mode).
        if (!DebugConfig.isPackageDebuggable(mContext)) {
            final long now = now();
            final long ttl = mDumpTTL;
            FileUtils.delete(dirFile, ttl < 0 ? null : new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return now - pathname.lastModified() > ttl;
                }
            });
        }
        // ensure dir.
        FileUtils.mkdirs(dirFile);

        String name = DateUtils.getDate(DATE_PATTERN, now()) + DUMP_FILE_SUFFIX;
        return new File(dirFile, name);
    }

    // ------------- pref --------------
    private SharedPreferences obtainPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    // ------------- utils -------------
    private static boolean isLooperStartMsg(String msg) {
        return msg != null && msg.length() != 0 && msg.charAt(0) == '>';
    }

    private static boolean isLooperEndMsg(String msg) {
        return msg != null && msg.length() != 0 && msg.charAt(0) == '<';
    }

    private static String readANRTracePath() {
        String tracePath = PropertyUtils.getQuickly("dalvik.vm.stack-trace-file", null);
        if (TextUtils.isEmpty(tracePath)) {
            tracePath = Environment.getDataDirectory().getAbsolutePath() + "/anr/traces.txt";
        }
        return tracePath;
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    @SuppressLint("HandlerLeak")
    final class ANRHandler extends Handler {

        public final static int WHAT_CHECK_RECORD = 0;

        ANRHandler() {
            super(getTracerLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CHECK_RECORD:
                    performCheckRecord((Record) msg.obj);
                    break;
            }
        }
    }

    final static class Record implements Cloneable {

        long startTime;
        long endTime;
        String msg;

        public Record copy() {
            try {
                return (Record) super.clone();
            } catch (CloneNotSupportedException e) {
                // impossible, just ignore.
                return null;
            }
        }

        public void reset() {
            startTime = 0;
            endTime = 0;
            msg = null;
        }
    }

    final static class LogcatWriter extends Writer {

        private final int mPriority;
        private final String mTag;

        public LogcatWriter(int priority, String tag) {
            mPriority = priority;
            mTag = tag;
        }

        @Override
        public void write(char[] buf, int offset, int count) throws IOException {
            throw new RuntimeException("Only string can be written into this writer.");
        }

        @Override
        public void write(String str) throws IOException {
            Log.println(mPriority, mTag, str);
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    /**
     * Reporter for ANR trace.
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
}
