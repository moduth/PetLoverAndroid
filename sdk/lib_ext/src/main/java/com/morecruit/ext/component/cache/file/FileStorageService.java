package com.morecruit.ext.component.cache.file;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.component.thread.Future;
import com.morecruit.ext.component.thread.ThreadPool;
import com.morecruit.ext.component.thread.ThreadPools;
import com.morecruit.ext.utils.StorageUtils;
import com.morecruit.ext.utils.ToastUtils;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 文件存储服务，外部使用请通过 {@link FileCacheManager}。
 * <p/>
 * Created by zhaiyifan on 2015/8/4.
 */
public class FileStorageService {

    private final static String TAG = "FileStorageService";

    // constant for check.
    private final static long MEGABYTES = 1024 * 1024;
    private final static int STORAGE_CHECK_INTERVAL = 3;
    private final static long STORAGE_LOW_BOUNDS = 10 * MEGABYTES;

    // constant for low storage handling.
    private final static int STORAGE_OP_INTERVAL = 2;

    private final static float STORAGE_REMAIN_PERCENTAGE = 0.1f;
    private final static float STORAGE_REMAIN_PERCENTAGE_EXTREME = 0.05f;

    private final static float STORAGE_EXIST_PERCENTAGE_OFFSET = 0.02f;

    private final static float STORAGE_WARNING_PERCENTAGE = 0.1f;

    // constant for warn interval.
    private final static int MINUTE = 60 * 1000;
    private final static int MAX_WARN_INTERVAL = 30 * MINUTE;
    private final static int COUNT_OF_HALF_INTERVAL = 6;

    private final Context mContext;
    private final Collector mCollector;
    private final AtomicInteger mCheckCounter = new AtomicInteger(0);
    private final AtomicInteger mLowStorageCounter = new AtomicInteger(0);
    private Future mPendingFuture;

    private long mLastWarnTime;
    private int mWarnCount;
    private int mWarnMessage;

    /**
     * Construct a FileStorageService with corresponding {@link Collector}.
     *
     * @param context   Application context.
     * @param collector Collector of {@link FileCacheService}.
     */
    public FileStorageService(Context context, Collector collector) {
        mContext = context.getApplicationContext();
        mCollector = collector;
    }

    /**
     * Check corresponding storage, see {@link Mode}.
     *
     * @param mode check mode.
     * @return true if storage is enough, false if un-available or low storage.
     */
    public boolean checkStorage(Mode mode) {
        return checkStorage(mode, false);
    }

    /**
     * Check corresponding storage, see {@link Mode}.
     *
     * @param mode  check mode.
     * @param force whether force check (ignore operation counter).
     * @return true if storage is enough, false if un-available or low storage.
     */
    public boolean checkStorage(Mode mode, boolean force) {
        if (mode == null) {
            throw new RuntimeException("mode is null");
        }
        if (!force && mCheckCounter.getAndIncrement() < STORAGE_CHECK_INTERVAL) {
            // ignore.
            return true;
        }
        // reset counter.
        mCheckCounter.set(0);
        boolean result = false;
        switch (mode) {
            case EXTERNAL:
                result = handleCheckStorage(true, force);
                break;

            case INTERNAL:
                result = handleCheckStorage(false, force);
                break;

            case BOTH:
                result = handleCheckStorage(true, force);
                result = handleCheckStorage(false, force) && result;
                break;
        }
        return result;
    }

    /**
     * Reset this file storage handler.
     */
    public void reset() {
        mLastWarnTime = 0;
        mWarnCount = 0;
    }

    public void setWarnMessage(int resId) {
        mWarnMessage = resId;
    }

    private boolean handleCheckStorage(boolean external, boolean force) {
        if (external && !StorageUtils.isExternalWritable(mContext)) {
            // external storage not available.
            return false;
        }
        File file = external ? Environment.getExternalStorageDirectory() : Environment.getDataDirectory();
        if (file == null) {
            return false;
        }
        while (!file.exists()) {
            file = file.getParentFile();
        }
        StatFs statFs = new StatFs(file.getAbsolutePath());
        long totalSize = (long) statFs.getBlockCount() * statFs.getBlockSize();
        long availableSize = (long) statFs.getAvailableBlocks() * statFs.getBlockSize();
        // float availablePer = (float) ((double) availableSize / totalSize);
        boolean lowStorage = availableSize < STORAGE_LOW_BOUNDS;
        if (lowStorage) {
            // low storage;
            handleLowStorage(totalSize, availableSize, external, force);
        }
        return !lowStorage;
    }

    private void handleLowStorage(long totalSize, long availableSize, boolean external, boolean force) {
        if (!force && mLowStorageCounter.getAndIncrement() < STORAGE_OP_INTERVAL) {
            return;
        }
        // reset counter.
        mLowStorageCounter.set(0);

        onLowStorage(totalSize, availableSize, external);
    }

    /**
     * Called when storage if low.
     *
     * @param totalSize     total storage size.
     * @param availableSize available(remaining) storage size.
     * @param external      whether or not external storage.
     */
    protected void onLowStorage(long totalSize, long availableSize, boolean external) {
        Logger.w(TAG, "low storage: totalSize=" + totalSize + ", availableSize=" + availableSize + ", external=" + external);
        synchronized (this) {
            if (mPendingFuture != null && !mPendingFuture.isDone()) {
                // if pending task if processing, ignore.
                return;
            }
            final Context context = mContext;
            final boolean externalStorage = external;
            mPendingFuture = ThreadPools.defaultThreadPool().submit(new ThreadPool.Job<Object>() {
                @Override
                public Object run(ThreadPool.JobContext jc) {
                    jc.setMode(ThreadPool.MODE_CPU);

                    Collection<FileCacheService> fileCacheServices = mCollector.collect();
                    if (fileCacheServices != null) {
                        int totalSize = 0;
                        int totalCapacity = 0;
                        for (FileCacheService cacheService : fileCacheServices) {
                            int capacity = cacheService.getCapacity(externalStorage);
                            int size = cacheService.getSize(externalStorage);
                            int remain = calculateRemainSize(capacity, size);
                            cacheService.clear(externalStorage, remain);
                            Logger.i(TAG, "clear cache service:" + cacheService + ": remain=" + remain);

                            totalSize += size;
                            totalCapacity += capacity;
                        }
                        float totalPercent = totalCapacity <= 0 ? Float.MAX_VALUE
                                : (float) totalSize / (float) totalCapacity;
                        if (totalPercent < STORAGE_WARNING_PERCENTAGE) {
                            // storage warning.
                            notifyStorageWarning(context);
                        }
                    }
                    return null;
                }
            });
        }
    }

    private int calculateRemainSize(int capacity, int currSize) {
        if (capacity <= 0) {
            return capacity;
        }
        float remainPercent;
        float percent = (float) currSize / (float) capacity;
        if (percent < STORAGE_REMAIN_PERCENTAGE + STORAGE_EXIST_PERCENTAGE_OFFSET) {
            remainPercent = STORAGE_REMAIN_PERCENTAGE_EXTREME;
        } else {
            remainPercent = STORAGE_REMAIN_PERCENTAGE;
        }
        return (int) (capacity * remainPercent);
    }

    private void notifyStorageWarning(final Context context) {
        if (context == null) {
            return;
        }
        if (!shouldShowWarning()) {
            return;
        }
        final int msg = mWarnMessage;
        if (msg == 0) {
            return;
        }
        ToastUtils.show(context, msg, Toast.LENGTH_LONG);
    }

    private boolean shouldShowWarning() {
        //           1
        // y = 1 - -----
        //          x+1
        float count = mWarnCount;
        long duration = (long) ((1 - 1 / (count / COUNT_OF_HALF_INTERVAL + 1)) * MAX_WARN_INTERVAL);
        long now = System.currentTimeMillis();
        boolean should = now - mLastWarnTime >= duration;
        if (should) {
            // remember.
            if (mWarnCount < Integer.MAX_VALUE) mWarnCount++;
            mLastWarnTime = now;
        }
        return should;
    }

    // ----------- inner class, interface, enum ----------

    /**
     * FileCacheService Collector for {@link FileCacheService}.
     */
    public interface Collector {
        /**
         * Try to collect all present {@link FileCacheService}s. Called by {@link FileStorageService}.
         *
         * @return All present {@link FileCacheService}s.
         */
        Collection<FileCacheService> collect();
    }

    /**
     * Mode for check storage.
     */
    public enum Mode {
        /**
         * Mode for external storage.
         */
        EXTERNAL,
        /**
         * Mode for internal storage.
         */
        INTERNAL,
        /**
         * Mode for both internal and external storage.
         */
        BOTH
    }
}
