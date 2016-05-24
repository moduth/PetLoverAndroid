package com.morecruit.ext.component.cache.file;

import android.support.v4.util.LruCache;

import com.morecruit.ext.utils.FileUtils;

import java.io.File;

/**
 * 文件缓存，内部使用LRU cache实现，外部使用请通过 {@link FileCacheManager}。
 * <p/>
 * Created by zhaiyifan on 2015/8/4.
 */
final class FileCache<K> {

    private final int mMaxSize;
    private LruCache<K, String> mLruCache;

    public FileCache(int maxSize) {
        mMaxSize = maxSize > 0 ? maxSize : 0;
        mLruCache = new LruCache<K, String>(maxSize > 0 ? maxSize : 1) {
            @Override
            protected int sizeOf(K key, String value) {
                return getFileSize(value);
            }

            @Override
            protected void entryRemoved(boolean evicted, K key, String oldValue, String newValue) {
                if (oldValue == newValue) {
                    return;
                }
                if (oldValue != null && oldValue.equals(newValue)) {
                    return;
                }
                recycle(oldValue);
            }
        };
    }

    public String get(K key) {
        if (mMaxSize <= 0) {
            return null;
        }
        return mLruCache.get(key);
    }

    public void put(K key, String file) {
        if (mMaxSize <= 0) {
            return;
        }
        mLruCache.put(key, file);
    }

    public void remove(K key) {
        if (mMaxSize <= 0) {
            return;
        }
        mLruCache.remove(key);
    }

    public void trimToSize(int maxSize) {
        if (mMaxSize <= 0) {
            return;
        }
        mLruCache.trimToSize(maxSize > 0 ? maxSize : 0);
    }

    public void clear() {
        if (mMaxSize <= 0) {
            return;
        }
        mLruCache.evictAll();
    }

    public int size() {
        if (mMaxSize <= 0) {
            return 0;
        }
        return mLruCache.size();
    }

    public int maxSize() {
        return mMaxSize;
    }

    private void recycle(String path) {
        synchronized (this) {
            if (path != null) {
                FileUtils.delete(new File(path));
            }
        }
    }

    private static int getFileSize(String path) {
        return (path == null || path.length() == 0) ? 0 : 1;
    }
}