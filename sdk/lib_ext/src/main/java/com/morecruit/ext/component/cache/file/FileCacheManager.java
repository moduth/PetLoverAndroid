package com.morecruit.ext.component.cache.file;

import android.content.Context;

import com.morecruit.ext.utils.AssertUtils;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.StorageUtils;
import com.morecruit.ext.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * FileCacheManager向外部提供文件缓存接口
 * <p/>
 * Created by zhaiyifan on 2015/8/5.
 */
public class FileCacheManager {

    private final static Object sGlobalLock = new Object();

    private final static boolean FILE_CLEAR_PERSIST = false;

    private final static HashMap<String, FileCacheService> sFileCacheService
            = new HashMap<String, FileCacheService>();

    // storage handler for file cache.
    private volatile static FileStorageService sStorageService;

    private final static FileStorageService.Collector sStorageCollector = new FileStorageService.Collector() {
        @Override
        public Collection<FileCacheService> collect() {
            synchronized (sFileCacheService) {
                return sFileCacheService.size() <= 0 ? null
                        : new ArrayList<FileCacheService>(sFileCacheService.values());
            }
        }
    };

    /**
     * 获得默认的文件存储服务
     *
     * @param context Application context.
     */
    public static FileStorageService getFileStorageService(Context context) {
        if (sStorageService == null) {
            synchronized (sStorageCollector) {
                if (sStorageService == null) {
                    sStorageService = new FileStorageService(context, sStorageCollector);
                }
            }
        }
        return sStorageService;
    }

    /**
     * 获得通用的非持久文件缓存服务，仅支持外部存储
     *
     * @param context          Application context.
     * @param name             FileCacheService的名字, 必须是唯一的.
     * @param externalCapacity 该服务的外部存储容量 (文件数量)
     */
    public static FileCacheService getFileCacheService(Context context, String name, int externalCapacity) {
        // default internal storage is disabled.
        return getFileCacheService(context, name, externalCapacity, 0);
    }

    /**
     * 获得通用的非持久文件缓存服务，支持内部和外部存储
     *
     * @param context          Application context.
     * @param name             FileCacheService的名字, 必须是唯一的.
     * @param externalCapacity 该服务的外部存储容量 (文件数量)
     * @param internalCapacity 该服务的内部存储容量 (文件数量)
     */
    public static FileCacheService getFileCacheService(Context context, String name, int externalCapacity, int internalCapacity) {
        // default none persist.
        return getFileCacheService(context, name, externalCapacity, internalCapacity, false);
    }

    /**
     * 获得通用的文件缓存服务，支持内部和外部存储
     *
     * @param context          Application context.
     * @param name             FileCacheService的名字, 必须是唯一的.
     * @param externalCapacity 该服务的外部存储容量 (文件数量)
     * @param internalCapacity 该服务的内部存储容量 (文件数量)
     * @param persist          该FileCacheService是否是持久的，也就是说当做"数据"还是"缓存"
     */
    public static FileCacheService getFileCacheService(Context context, String name, int externalCapacity, int internalCapacity, boolean persist) {
        AssertUtils.assertTrue(!StringUtils.isEmpty(name));
        synchronized (sFileCacheService) {
            FileCacheService cacheService = sFileCacheService.get(name);
            if (cacheService == null) {
                cacheService = new FileCacheService(context, name, externalCapacity, internalCapacity, persist);
                sFileCacheService.put(name, cacheService);
            }
            return cacheService;
        }
    }

    // 图片文件缓存

    /**
     * 默认的图片文件缓存名
     */
    public final static String IMAGE_FILE_CACHE_NAME = "image";
    /**
     * 默认的图片文件缓存外部存储容量
     */
    public final static int IMAGE_EXTERNAL_CAPACITY = 3000;
    /**
     * 默认的图片文件缓存内部存储容量
     */
    public final static int IMAGE_INTERNAL_CAPACITY = 800;

    /**
     * 获得图片文件缓存服务, 其外部容量是 {@link #IMAGE_EXTERNAL_CAPACITY},
     * 内部容量是 {@link #IMAGE_INTERNAL_CAPACITY}. 该服务是非持久的.
     *
     * @param context Application context.
     */
    public static FileCacheService getImageFileCacheService(Context context) {
        return getFileCacheService(context, IMAGE_FILE_CACHE_NAME, IMAGE_EXTERNAL_CAPACITY, IMAGE_INTERNAL_CAPACITY);
    }

    // 音频文件缓存
    /**
     * 默认的音频文件缓存名
     */
    public final static String AUDIO_FILE_CACHE_NAME = "audio";
    /**
     * 默认的音频文件缓存外部存储容量
     */
    public final static int AUDIO_EXTERNAL_CAPACITY = 100;
    /**
     * 默认的音频文件缓存内部存储容量
     */
    public final static int AUDIO_INTERNAL_CAPACITY = 100;

    /**
     * 获得音频文件缓存服务, 其外部容量是 {@link #AUDIO_EXTERNAL_CAPACITY},
     * 内部容量是 {@link #AUDIO_INTERNAL_CAPACITY}. 该服务是非持久的.
     *
     * @param context Application context.
     */
    public static FileCacheService getAudioFileCacheService(Context context) {
        return getFileCacheService(context, AUDIO_FILE_CACHE_NAME, AUDIO_EXTERNAL_CAPACITY, AUDIO_INTERNAL_CAPACITY);
    }

    // 临时文件缓存.
    /**
     * 默认的临时文件缓存名
     */
    public final static String TMP_FILE_CACHE_NAME = "tmp";
    /**
     * 默认的临时文件缓存外部存储容量
     */
    public final static int TMP_EXTERNAL_CAPACITY = 500;
    /**
     * 默认的临时文件缓存内部存储容量
     */
    public final static int TMP_INTERNAL_CAPACITY = 200;

    /**
     * 获得非持久临时文件缓存服务e, 其外部容量是 {@link #TMP_EXTERNAL_CAPACITY},
     * 内部容量是 {@link #TMP_INTERNAL_CAPACITY}. 该服务是非持久的.
     *
     * @param context Application context.
     */
    public static FileCacheService getTmpFileCacheService(Context context) {
        return getTmpFileCacheService(context, false);
    }

    /**
     * 获得临时文件缓存服务, 其外部容量是 {@link #TMP_EXTERNAL_CAPACITY},
     * 内部容量是 {@link #TMP_INTERNAL_CAPACITY}.
     *
     * @param context Application context.
     * @param persist 该临时FileCacheService是否是持久的，也就是说当做"数据"还是"缓存"
     */
    public static FileCacheService getTmpFileCacheService(Context context, boolean persist) {
        return getFileCacheService(context, TMP_FILE_CACHE_NAME, TMP_EXTERNAL_CAPACITY, TMP_INTERNAL_CAPACITY, persist);
    }

    /**
     * 清掉所有cache，包括图片（内存）, 文件（图片、音频、临时文件等）.
     *
     * @param context Application context.
     */
    public static void clear(Context context) {
        synchronized (sGlobalLock) {
            clear(context, null);
        }
    }

    /**
     * 清掉所有cache，包括图片（内存）, 文件（图片、音频、临时文件等）.
     *
     * @param context Application context.
     * @param uid     可标示的id来区别某些类型的缓存，暂时还不支持。
     */
    public static void clear(Context context, String uid) {
        synchronized (sGlobalLock) {
            clearFiles(context);
        }
    }

    private static void clearFiles(Context context) {
        // 删除所有cache（包括持久的）, 无视目录以避免 "Device or resource busy".

        // external
        String externalDir = StorageUtils.getExternalCacheDir(context, false);
        if (externalDir != null) {
            FileUtils.delete(new File(externalDir), true);
        }
        if (FILE_CLEAR_PERSIST) {
            externalDir = StorageUtils.getExternalCacheDir(context, true);
            if (externalDir != null) {
                FileUtils.delete(new File(externalDir), true);
            }
        }

        // extend external
        String externalDirExt = StorageUtils.getExternalCacheDirExt(context, false);
        if (externalDirExt != null) {
            FileUtils.delete(new File(externalDirExt), true);
        }
        if (FILE_CLEAR_PERSIST) {
            externalDirExt = StorageUtils.getExternalCacheDirExt(context, true);
            if (externalDirExt != null) {
                FileUtils.delete(new File(externalDirExt), true);
            }
        }

        // internal
        String internalDir = StorageUtils.getInternalCacheDir(context, false);
        if (internalDir != null) {
            FileUtils.delete(new File(internalDir), true);
        }
        if (FILE_CLEAR_PERSIST) {
            internalDir = StorageUtils.getInternalCacheDir(context, true);
            if (internalDir != null) {
                FileUtils.delete(new File(internalDir), true);
            }
        }
    }
}