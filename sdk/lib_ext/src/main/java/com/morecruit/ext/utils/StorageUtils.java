package com.morecruit.ext.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import com.morecruit.ext.component.logger.Logger;

/**
 * Utilities for storage
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class StorageUtils {

    private final static int STATE_UNKNOWN = -1;
    private final static int STATE_MOUNTED = 0;
    private final static int STATE_MOUNTED_READ_ONLY = 1;
    private final static int STATE_OTHERS = 2;
    // ------------------------------ dir related -------------------------------
    private final static Object sCacheDirLock = new Object();
    private static int sMonitoredExternalState = STATE_UNKNOWN;
    private final static Singleton<BroadcastReceiver, Void> sReceiver = new Singleton<BroadcastReceiver, Void>() {
        @Override
        protected BroadcastReceiver create(Void param) {
            return new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    onStorageStateChanged();
                }
            };
        }
    };
    private static volatile boolean sReceiverRegistered = false;

    private StorageUtils() {
        // static usage.
    }

    /**
     * Whether external storage is readable.
     *
     * @param context application context.
     */
    public static boolean isExternalReadable(Context context) {
        return isExternalMounted(context) || isExternalMountedReadOnly(context);
    }

    /**
     * Whether external storage is writable.
     *
     * @param context application context.
     */
    public static boolean isExternalWritable(Context context) {
        return isExternalMounted(context);
    }

    /**
     * Get the external storage capability.
     *
     * @return External storage capability.
     */
    public static long getExternalCapability() {
        if (!isExternalReadable(null)) {
            return -1;
        }

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long allBlocks = stat.getBlockCount();
        return allBlocks * blockSize;
    }

    /**
     * Get the external storage remaining space.
     *
     * @return External storage remaining space.
     */
    public static long getExternalRemaining() {
        if (!isExternalReadable(null)) {
            return -1;
        }

        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    private static boolean isExternalMounted(Context context) {
        if (sMonitoredExternalState != STATE_UNKNOWN) {
            return sMonitoredExternalState == STATE_MOUNTED;
        }
        int state = retrieveExternalStorageState();
        if (registerReceiverIfNeeded(context)) {
            // update state when register succeed.
            sMonitoredExternalState = state;
        }
        return state == STATE_MOUNTED;
    }

    private static boolean isExternalMountedReadOnly(Context context) {
        if (sMonitoredExternalState != STATE_UNKNOWN) {
            return sMonitoredExternalState == STATE_MOUNTED_READ_ONLY;
        }
        int state = retrieveExternalStorageState();
        if (registerReceiverIfNeeded(context)) {
            // update state when register succeed.
            sMonitoredExternalState = state;
        }
        return state == STATE_MOUNTED_READ_ONLY;
    }

    /**
     * Get the internal storage remaining space.
     *
     * @return Internal storage remaining space.
     */
    public static long getInternalCapability() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long allBlocks = stat.getBlockCount();
        return allBlocks * blockSize;
    }

    /**
     * Get the internal storage capability.
     *
     * @return Internal storage capability.
     */
    public static long getInternalRemaining() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    static void onStorageStateChanged() {
        sMonitoredExternalState = retrieveExternalStorageState();
    }

    private static boolean registerReceiverIfNeeded(Context context) {
        if (sReceiverRegistered) {
            return true;
        }
        if (context == null || context.getApplicationContext() == null) {
            return false;
        }
        synchronized (sReceiver) {
            if (sReceiverRegistered) {
                return true;
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            context.getApplicationContext().registerReceiver(sReceiver.get(null), filter);
            sReceiverRegistered = true;
            return true;
        }
    }

    private static int retrieveExternalStorageState() {
        String externalState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(externalState)) {
            return STATE_MOUNTED;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalState)) {
            return STATE_MOUNTED_READ_ONLY;
        } else {
            return STATE_OTHERS;
        }
    }

    /**
     * Get common cache dir(external if available, or internal) with corresponding name, which is not persist.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @return Cache directory.
     */
    private static String getCacheDir(Context context, String name) {
        return getCacheDir(context, name, false);
    }

    /**
     * Get common cache dir(external if available, or internal) with corresponding name.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @param persist Whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return Cache directory.
     */
    public static String getCacheDir(Context context, String name, boolean persist) {
        String dir = getExternalCacheDir(context, name, persist);
        return dir != null ? dir : getInternalCacheDir(context, name, persist);
    }

    /**
     * Get external cache dir with corresponding name, which is not persist.
     */
    public static String getExternalCacheDir(Context context, String name) {
        return getExternalCacheDir(context, name, false);
    }

    /**
     * Get external cache dir with corresponding name.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return External cache directory.
     */
    public static String getExternalCacheDir(Context context, String name, boolean persist) {
        String dir = getExternalCacheDir(context, persist);
        if (dir == null) {
            return null;
        }
        if (isEmpty(name)) {
            return dir;
        }
        File file = new File(dir + File.separator + name);
        if (!file.exists() || !file.isDirectory()) {
            synchronized (sCacheDirLock) {
                if (!file.isDirectory()) {
                    file.delete();
                    file.mkdirs();
                } else if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * Get external ROOT cache dir with corresponding name.
     *
     * @param context Application context.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return External ROOT cache directory.
     */
    public static String getExternalCacheDir(Context context, boolean persist) {
        if (!isExternalWritable(context)) {
            return null;
        }
        File externalDir = !persist ? InnerEnvironment.getExternalCacheDir(context, false)
                : InnerEnvironment.getExternalFilesDir(context, "cache", false);
        return externalDir == null ? null : externalDir.getAbsolutePath();
    }

    /**
     * Get extra external cache dir with corresponding name, which is not persist.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @return Extra external cache directory.
     */
    public static String getExternalCacheDirExt(Context context, String name) {
        return getExternalCacheDirExt(context, name, false);
    }

    /**
     * Get extra external cache dir with corresponding name.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return Extra external cache directory.
     */
    public static String getExternalCacheDirExt(Context context, String name, boolean persist) {
        String dir = getExternalCacheDirExt(context, persist);
        if (dir == null) {
            return null;
        }
        if (isEmpty(name)) {
            return dir;
        }
        File file = new File(dir + File.separator + name);
        if (!file.exists() || !file.isDirectory()) {
            synchronized (sCacheDirLock) {
                if (!file.isDirectory()) {
                    file.delete();
                    file.mkdirs();
                } else if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * Get extra external ROOT cache dir with corresponding name.
     *
     * @param context Application context.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return Extra external ROOT cache directory.
     */
    public static String getExternalCacheDirExt(Context context, boolean persist) {
        if (!isExternalWritable(context)) {
            return null;
        }
        File externalDir = !persist ? InnerEnvironment.getExternalCacheDir(context, true)
                : InnerEnvironment.getExternalFilesDir(context, "cache", true);
        return externalDir == null ? null : externalDir.getAbsolutePath();
    }

    /**
     * Get internal cache dir with corresponding name, which is not persist.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @return Internal cache directory.
     */
    public static String getInternalCacheDir(Context context, String name) {
        return getInternalCacheDir(context, name, false);
    }

    /**
     * Get internal cache dir with corresponding name.
     *
     * @param context Application context.
     * @param name    Cache directory name.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return Internal cache directory.
     */
    public static String getInternalCacheDir(Context context, String name, boolean persist) {
        String dir = getInternalCacheDir(context, persist);
        if (isEmpty(name)) {
            return dir;
        }
        File file = new File(dir + File.separator + name);
        if (!file.exists() || !file.isDirectory()) {
            synchronized (sCacheDirLock) {
                if (!file.isDirectory()) {
                    file.delete();
                    file.mkdirs();
                } else if (!file.exists()) {
                    file.mkdirs();
                }
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * Get internal ROOT cache dir with corresponding name.
     *
     * @param context Application context.
     * @param persist whether this cache dir should be persist or not. A persist cache is regarded as "data", not "cache".
     * @return Internal ROOT cache directory.
     */
    public static String getInternalCacheDir(Context context, boolean persist) {
        return !persist ? context.getCacheDir().getAbsolutePath()
                : context.getFilesDir().getAbsolutePath() + File.separator + "cache";
    }

    /**
     * Determine whether a path belongs to internal storage.
     *
     * @param path Path to check.
     * @return Whether this path belongs to internal storage.
     */
    public static boolean isInternal(String path) {
        String internalCacheDir = Environment.getDataDirectory().getAbsolutePath();
        return path != null && path.startsWith(internalCacheDir);
    }

    /**
     * Determine whether a path belongs to data.
     *
     * @param path Path to check.
     * @return Whether this path belongs to data.
     */
    public static boolean isData(String path) {
        return path != null && (path.startsWith(Environment.getDataDirectory().getAbsolutePath()) ||
                path.startsWith(InnerEnvironment.getExternalStorageAndroidDataDir().getAbsolutePath()));
    }

    // ---------------- internal utils -----------------
    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    static class InnerEnvironment {

        private static final String TAG = "InnerEnvironment";

        private static final String EXTEND_SUFFIX = "-ext";

        private static final File EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY
                = new File(new File(Environment.getExternalStorageDirectory(),
                "Android"), "data");

        public static File getExternalStorageAndroidDataDir() {
            return EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY;
        }

        public static File getExternalStorageAppCacheDirectory(String packageName) {
            return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
                    packageName), "cache");
        }

        public static File getExternalStorageAppFilesDirectory(String packageName) {
            return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
                    packageName), "files");
        }

        @SuppressLint("NewApi")
        public static File getExternalCacheDir(Context context, boolean extend) {
            if (!extend && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                return context.getExternalCacheDir();
            }
            synchronized (InnerEnvironment.class) {
                File externalCacheDir = getExternalStorageAppCacheDirectory(
                        context.getPackageName() + (extend ? EXTEND_SUFFIX : ""));
                if (!externalCacheDir.exists()) {
                    try {
                        (new File(getExternalStorageAndroidDataDir(), ".nomedia")).createNewFile();
                    } catch (IOException e) {
                        Logger.i(TAG, e.getMessage());
                    }
                    if (!externalCacheDir.mkdirs()) {
                        Log.w(TAG, "Unable to create external cache directory");
                        return null;
                    }
                }
                return externalCacheDir;
            }
        }

        @SuppressLint("NewApi")
        public static File getExternalFilesDir(Context context, String type, boolean extend) {
            if (!extend && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                return context.getExternalFilesDir(type);
            }
            synchronized (InnerEnvironment.class) {
                File externalFilesDir = getExternalStorageAppFilesDirectory(
                        context.getPackageName() + (extend ? EXTEND_SUFFIX : ""));
                if (!externalFilesDir.exists()) {
                    try {
                        (new File(getExternalStorageAndroidDataDir(),
                                ".nomedia")).createNewFile();
                    } catch (IOException e) {
                    }
                    if (!externalFilesDir.mkdirs()) {
                        Log.w(TAG, "Unable to create external files directory");
                        return null;
                    }
                }
                if (type == null) {
                    return externalFilesDir;
                }
                File dir = new File(externalFilesDir, type);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        Log.w(TAG, "Unable to create external media directory " + dir);
                        return null;
                    }
                }
                return dir;
            }
        }
    }
}
