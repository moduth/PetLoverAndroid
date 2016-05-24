package com.morecruit.ext.component.debug;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.utils.DateUtils;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.ProcessUtils;
import com.morecruit.ext.utils.Singleton;
import com.morecruit.ext.utils.ToastUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Tracer for leak detective. You can call {@link #install(Application)} for automatic Activity trace.
 * And also, you can call {@link #trace(Object)} for manuel trace</p>
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class LeakTracer extends Tracer {

    private final static String TAG = "LeakTracer";

    /**
     * whether notify when leak occurs.
     */
    private final static boolean NOTIFY = true;

    /**
     * interval of leak analyze.
     */
    private final static long ANALYZE_INTERVAL = 60 * 1000L;

    /**
     * bounds of leak count, object will be treated as leak when leak count of it's trace exceeds (including equals) this.
     */
    private final static int LEAK_COUNT_BOUNDS = 3;
    /**
     * gc of leak count, we should try gc when leak count of object's trace exceeds this (only once).
     */
    private final static int LEAK_COUNT_GC = 2;

    // time to live of dump files in debug mode: 3days.
    private final static long DUMP_FILE_TTL_DEBUGGABLE = 3 * 24 * 60 * 60 * 1000;

    private final static String LEAK_DIR = "leak";
    private final static String INFO_FILE_SUFFIX = ".txt";
    private final static String HPROF_FILE_SUFFIX = ".hprof";

    private final static String PREFERENCE_PREFIX = "LeakTracer:";
    private final static String PREFERENCE_REPORT_TIMESTAMP = PREFERENCE_PREFIX + "report_timestamp";

    private final Context mContext;

    private final List<Trace> mTraces = Collections.synchronizedList(new ArrayList<Trace>());
    private final List<Trace> mTmpTraces = new ArrayList<Trace>();
    private final ReferenceQueue<Object> mQueue = new ReferenceQueue<Object>();

    private volatile Reporter mReporter;
    private final Object mReportLock = new Object();

    private volatile boolean mApplicationInstalled = false;

    private final Runnable mAnalyzeRunnable = new Runnable() {
        @Override
        public void run() {
            analyzeTraces();
            // schedule for next analyze.
            scheduleAnalyze();
        }
    };

    private LeakTracer(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Trace corresponding object immediately, if supported.
     *
     * @param object object to trace.
     */
    public void trace(Object object) {
        trace(object, -1);
    }

    /**
     * Trace corresponding object, if supported.
     *
     * @param object     object to trace.
     * @param timeToLive time to live before this object should be gc.
     */
    public void trace(Object object, long timeToLive) {
        mTraces.add(createTrace(object, timeToLive));
        // schedule analyze after new trace.
        scheduleAnalyze();
    }

    /**
     * Dump current stats.
     *
     * @return current stats.
     */
    public CharSequence dump() {
        List<Trace> traces = getAliveTraces();
        StringBuilder leakSb = null;
        StringBuilder aliveSb = null;
        for (Trace trace : traces) {
            AnalyzeResult result = analyzeTrace(trace, true);
            if (result == null) {
                continue;
            }
            if (aliveSb == null) {
                // now on demand.
                aliveSb = new StringBuilder();
                aliveSb.append("alive:\n");
            }
            aliveSb.append(result.message).append('\n');

            if (result.leak) {
                if (leakSb == null) {
                    // new on demand.
                    leakSb = new StringBuilder();
                    leakSb.append("leak:\n");
                }
                leakSb.append(result.message).append('\n');
            }
        }
        StringBuilder sb = leakSb;
        if (sb != null) {
            sb.append('\n').append(aliveSb);
        } else {
            sb = aliveSb;
        }
        return sb != null ? sb.toString() : null;
    }

    /**
     * Get all current alive traces.
     */
    private List<Trace> getAliveTraces() {
        if (!isTracerThread()) {
            // for non-tracer thread.
            return new ArrayList<Trace>(mTraces);
        }
        // clear up dead traces.
        clearDeadTraces();
        mTmpTraces.clear();
        mTmpTraces.addAll(mTraces);
        return mTmpTraces;
    }

    /**
     * Clear non-alive traces.
     */
    private void clearDeadTraces() {
        Trace trace = (Trace) mQueue.poll();
        while (trace != null) {
            mTraces.remove(trace);
            trace = (Trace) mQueue.poll();
        }
    }

    /**
     * Create trace according to object to be traced.
     *
     * @param object object to be traced.
     */
    private Trace createTrace(Object object, long timeToLive) {
        return new Trace(object, timeToLive, mQueue);
    }

    /**
     * Schedule trace analyze.
     */
    private void scheduleAnalyze() {
        getTracerHandler().removeCallbacks(mAnalyzeRunnable);
        getTracerHandler().postDelayed(mAnalyzeRunnable, ANALYZE_INTERVAL);
    }

    /**
     * Analyze all alive traces.
     */
    private void analyzeTraces() {
        List<Trace> traces = getAliveTraces();
        boolean leak = false;
        boolean dump = false;
        boolean tryGc = false;
        StringBuilder sb = null;
        for (Trace trace : traces) {
            AnalyzeResult result = analyzeTrace(trace, false);
            if (result == null) {
                continue;
            }
            if (result.leak) {
                leak = true;
                if (sb == null) {
                    // new on demand.
                    sb = new StringBuilder();
                }
                sb.append(result.message).append('\n');
            }
            if (result.dump) {
                dump = true;
            }
            if (result.tryGc) {
                tryGc = true;
            }
        }
        // leak occurs.
        if (leak) {
            String leakInfo = sb.toString();
            // dump leak info.
            if (dump) {
                dumpLeak(leakInfo);
            }
            // show notify if needed (when foreground and package debuggable).
            if (NOTIFY && ProcessUtils.isForeground(mContext) && DebugConfig.isPackageDebuggable(mContext)) {
                ToastUtils.show(mContext, "leak occurs in " + mContext.getPackageName() + "\n\n" + leakInfo);
            }
        }
        // try gc.
        if (tryGc) {
            System.gc();
            System.gc();
        }
    }

    /**
     * Perform trace analyze.
     */
    private AnalyzeResult analyzeTrace(Trace trace, boolean justAnalyze) {
        if (trace == null || trace.isAlive()) {
            return null;
        }
        Object obj = trace.get();
        if (obj == null) {
            return null;
        }
        boolean leak = false;
        if (obj instanceof Activity) {
            Activity activity = (Activity) obj;
            // finished activity (not gc or leak).
            if (activity.isFinishing()) {
                leak = true;
            }
        } else {
            // common way: non-null object.
            leak = true;
        }

        AnalyzeResult result = null;
        if (leak) {
            if (!justAnalyze) {
                trace.incLeakCount();
            }
            // new result on demand.
            result = new AnalyzeResult();
            // leak when leak count exceeds (including equals) bounds.
            result.leak = trace.getLeakCount() >= LEAK_COUNT_BOUNDS;
            // dump when leak count just equals bounds.
            result.dump = trace.getLeakCount() == LEAK_COUNT_BOUNDS;
            // try gc when leak count just equals gc bounds.
            result.tryGc = trace.getLeakCount() == LEAK_COUNT_GC;
            result.message = String.valueOf(obj);
        }
        return result;
    }

    /**
     * Dump leak.
     */
    private void dumpLeak(String info) {
        String date = DateUtils.getDate();
        String dir = getLeakDir();
        if (dir == null) {
            return;
        }
        File dirFile = new File(dir);
        // delete others if needed.
        if (!DebugConfig.isPackageDebuggable(mContext)) {
            // keep only one dump in non-package-debuggable mode.
            FileUtils.delete(dirFile, true);
        } else {
            final long now = System.currentTimeMillis();
            FileUtils.delete(dirFile, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    // keep dump files within 3 days.
                    return now - pathname.lastModified() > DUMP_FILE_TTL_DEBUGGABLE;
                }
            });
        }
        // ensure dir.
        FileUtils.mkdirs(dirFile);
        // dump info.
        dumpLeakInfo(new File(dir, date + INFO_FILE_SUFFIX), info);
        // dump hprof.
        dumpLeakHprof(new File(dir, date + HPROF_FILE_SUFFIX));
    }

    private void dumpLeakInfo(File file, String info) {
        if (file == null || info == null) {
            return;
        }
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            writer.write(info);
        } catch (IOException e) {
            Logger.w(TAG, "fail to dump info " + info, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // empty.
                }
            }
        }
    }

    private void dumpLeakHprof(File file) {
        if (file == null) {
            return;
        }
        try {
            Debug.dumpHprofData(file.getAbsolutePath());
        } catch (Throwable e) {
            Logger.w(TAG, "fail to dump hprof", e);
        }
    }

    /**
     * Install automatic leak tracer for application.
     *
     * @param application application.
     * @return true if automatic leak tracer is successfully installed.
     */
    public boolean install(Application application) {
        if (!mApplicationInstalled) {
            synchronized (this) {
                if (!mApplicationInstalled) {
                    mApplicationInstalled = installApplication(application);
                }

            }
        }
        return mApplicationInstalled;
    }

    private boolean installApplication(Application application) {
        ActivityTracer.getInstance().install(application);
        return ActivityTracer.getInstance().registerActivityLifecycleCallbacks(
                new ActivityTracer.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        trace(activity);
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                });
    }

    /**
     * Get the leak dump dir.
     *
     * @return leak dump dir.s
     */
    public String getLeakDir() {
        return getLeakDir(mContext);
    }

    // ----------------- report ----------------

    /**
     * Set the leak reporter.
     *
     * @param reporter Leak reporter.
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
                // do report when new reporter prepared.
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
            String dir = getLeakDir();
            if (dir == null) {
                return;
            }
            final long now = System.currentTimeMillis();
            final long last = obtainPreference().getLong(PREFERENCE_REPORT_TIMESTAMP, 0);
            File[] files = (new File(dir)).listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.lastModified() > last;
                }
            });
            boolean reported = true;
            if (files != null && files.length > 0) {
                reported = reporter.onReport(files);
            }
            if (reported) {
                obtainPreference().edit().putLong(PREFERENCE_REPORT_TIMESTAMP, now).commit();
            }
        }
    }

    // -------------- preference --------------
    private SharedPreferences obtainPreference() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    // ----------------- utils -----------------
    private static String getLeakDir(Context context) {
        if (context == null) {
            return null;
        }
        return getTraceDir(context, LEAK_DIR);
    }

    /**
     * Inner class for analyze result.
     */
    private static class AnalyzeResult {
        boolean leak = false;
        boolean dump = false;
        boolean tryGc = false;
        String message = null;
    }

    /**
     * Inner class for tracing object.
     */
    private static class Trace extends WeakReference<Object> {

        private final long mTimeToLive;
        private final long mStartTime;
        private int mLeakCount = 0;

        public Trace(Object r, long timeToLive, ReferenceQueue<? super Object> q) {
            super(r, q);
            mTimeToLive = timeToLive;
            mStartTime = SystemClock.uptimeMillis();
        }

        public boolean isAlive() {
            return (mTimeToLive > 0) && (SystemClock.uptimeMillis() - mStartTime <= mTimeToLive);
        }

        public int getLeakCount() {
            return mLeakCount;
        }

        public void incLeakCount() {
            mLeakCount++;
        }
    }

    /**
     * Reporter for {@link LeakTracer}.
     */
    public static interface Reporter {
        /**
         * Called when should report.
         *
         * @param files files to report.
         * @return true if reported, false otherwise.
         */
        public boolean onReport(File[] files);
    }

    // ------------------ singleton -------------------
    private final static Singleton<LeakTracer, Context> sSingleton = new Singleton<LeakTracer, Context>() {
        @Override
        protected LeakTracer create(Context context) {
            return new LeakTracer(context);
        }
    };

    /**
     * Get the single instance of {@link #LeakTracer}.
     *
     * @param context Application context.
     */
    public static LeakTracer getInstance(Context context) {
        return sSingleton.get(context);
    }
}
