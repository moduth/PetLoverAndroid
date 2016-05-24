package com.morecruit.ext.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Debug;
import android.view.InflateException;

import java.io.File;
import java.io.FileFilter;

import com.morecruit.ext.component.logger.Logger;

/**
 * OOM检测工具类，会dump出hprof
 *
 * @author markzhai on 16/3/5
 */
public final class OomUtils {

    private final static String TAG = "OomUtils";

    private final static String OOM_DIR = "oom";
    private final static String OOM_SUFFIX = ".hprof";

    // time to live of dump files.
    private final static long DUMP_FILE_TTL = 3 * 24 * 60 * 60 * 1000;

    private OomUtils() {
        // static usage.
    }

    /**
     * Dump hprof to certain directory (see {@link #getHprofDir(Context)})
     * if corresponding throwable is regarded as oom exception.
     *
     * @param context Application context.
     * @param e       Throwable which may be regarded as oom exception.
     * @return Whether dump is performed.
     */
    public static boolean dumpHprofIfNeeded(Context context, Throwable e) {
        if (context == null) {
            // no valid context.
            return false;
        }
        if (e == null || !isOOM(e)) {
            // dump only when oom.
            return false;
        }
        try {
            String dir = getOOMDir(context);
            if (dir == null) {
                return false;
            }
            String name = DateUtils.getDate() + "#" + e.getClass().getSimpleName() + OOM_SUFFIX;
            File file = new File(dir + File.separator + name);
            // delete others if needed.
            if (!isDebuggable(context)) {
                // keep only one dump file in non-package-debuggable mode.
                FileUtils.delete(file.getParentFile(), true);
            } else {
                final long now = System.currentTimeMillis();
                FileUtils.delete(file.getParentFile(), new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        // keep dump files within 3 days.
                        return now - pathname.lastModified() > DUMP_FILE_TTL;
                    }
                });
            }
            // perform dump.
            if (FileUtils.mkdirs(file.getParentFile())) {
                Debug.dumpHprofData(file.getAbsolutePath());
            }
        } catch (Throwable t) {
            Logger.w(TAG, "fail to dump hprof", t);
        }
        return true;
    }

    /**
     * Get directory to store hprof file.
     *
     * @param context Application context.
     * @return Directory to store hprof file.
     */
    public static String getHprofDir(Context context) {
        if (context == null) {
            return null;
        }
        return getOOMDir(context);
    }

    /**
     * Check whether corresponding throwable is regarded as oom exception.
     *
     * @param e Throwable to check.
     * @return Whether corresponding throwable is regarded as oom exception.
     */
    public static boolean isOOM(Throwable e) {
        if (e == null) {
            return false;
        }
        return (e instanceof OutOfMemoryError) || (e instanceof InflateException);
    }

    private static String getOOMDir(Context context) {
        return StorageUtils.getExternalCacheDir(context, OOM_DIR, true);
    }

    private static boolean isDebuggable(Context context) {
        ApplicationInfo appInfo = context.getApplicationInfo();
        return (appInfo != null) && ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
    }
}

