package com.morecruit.ext.component.cache.file;

import android.content.Context;
import android.os.Build;
import android.os.Process;

import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.component.thread.ThreadPool;
import com.morecruit.ext.component.thread.ThreadPools;
import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.StorageUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 本地文件缓存服务，外部使用请通过 {@link FileCacheManager}。
 * <p/>
 * Created by zhaiyifan on 2015/8/4.
 */
public class FileCacheService {

    private final static String TAG = "FileCacheService";

    private final static String DIR_NAME = "file";

    private final static ThreadPool sThreadPool = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ?
            ThreadPools.newCachedThreadPool("file-cache", 2) : ThreadPools.newCachedThreadPool("file-cache");

    static {
        sThreadPool.setPriority(Process.THREAD_PRIORITY_LOWEST);
    }

    private final Context mContext;

    private final String mName;

    private final boolean mPersist;

    private final FileCache<String> mExternalCache;
    private final FileCache<String> mInternalCache;

    private boolean mInitialized = false;

    private final static ThreadLocal<StringBuilder> sStringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }
    };

    /**
     * Construct a FileCacheService with only external storage support.
     *
     * @param context          Application context.
     * @param name             Unique name.
     * @param externalCapacity Capacity of external cache.
     */
    public FileCacheService(Context context, String name, int externalCapacity) {
        // default non persist.
        this(context, name, externalCapacity, false);
    }

    /**
     * Construct a FileCacheService with only external storage support.
     *
     * @param context          Application context.
     * @param name             Unique name.
     * @param externalCapacity Capacity of external cache.
     * @param persist          Whether this cache is persist. If true, it will be accounted as "data", not "cache".
     */
    public FileCacheService(Context context, String name, int externalCapacity, boolean persist) {
        // default internal storage is disabled (capacity is 0).
        this(context, name, externalCapacity, 0, persist);
    }

    /**
     * Construct a FileCacheService with full internal and external storage support.
     *
     * @param context          Application context.
     * @param name             Unique name.
     * @param externalCapacity Capacity of external cache.
     * @param internalCapacity Capacity of internal cache.
     * @param persist          Whether this cache is persist. If true, it will be accounted as "data", not "cache".
     */
    public FileCacheService(Context context, String name, int externalCapacity, int internalCapacity, boolean persist) {
        if (isEmpty(name)) {
            throw new NullPointerException("file cache: name can NOT be empty!");
        }
        if (externalCapacity <= 0 && internalCapacity <= 0) {
            throw new IllegalArgumentException("file cache: should has at least one valid capacity");
        }

        mContext = context.getApplicationContext();
        mName = DIR_NAME + File.separator + name;
        mPersist = persist;

        mExternalCache = new FileCache<String>(externalCapacity);
        mInternalCache = new FileCache<String>(internalCapacity);

        init();
    }

    private void init() {
        // async init.
        sThreadPool.submit(new ThreadPool.Job<Object>() {
            @Override
            public Object run(ThreadPool.JobContext jc) {
                ensureCache(false);
                ensureCache(true);
                mInitialized = true;
                return null;
            }
        });
    }

    /**
     * Ensure corresponding cache storage: init the existing file cache,
     * make sure won't exceed the capacity.
     *
     * @param external true for external cache, false for internal one.
     */
    private synchronized void ensureCache(boolean external) {
        String dir = getDir(external);
        FileCache<String> cache = getCache(external);

        if (isEmpty(dir)) {
            return;
        }

        File dirFile = new File(dir);
        String[] nameList = dirFile.list();
        if (nameList == null || nameList.length == 0) {
            return;
        }

        FileEntry[] entryList = new FileEntry[nameList.length];
        for (int i = 0; i < entryList.length; i++) {
            entryList[i] = new FileEntry(dir, nameList[i]);
        }

        // sort the file list with increase order.
        Arrays.sort(entryList, sFileComparator);
        for (FileEntry file : entryList) {
            if (file == null) {
                continue;
            }
            if (!file.isFile) {
                if (file.path != null) {
                    FileUtils.delete(new File(file.path));
                }
                continue;
            }
            cache.put(file.name, file.path);
        }
    }

    public synchronized void clear() {
        clear(false);
        clear(true);
    }

    public synchronized void clear(boolean external) {
        getCache(external).clear();
        // perform delete in case of in-memory cache is not available.
        String dir = getDir(external);
        if (dir != null) {
            FileUtils.delete(new File(dir), true);
        }
    }

    public synchronized void clear(boolean external, int remain) {
        getCache(external).trimToSize(remain);
    }

    /**
     * Get the cache path for corresponding file name.
     *
     * @param fileName file name.
     */
    public String getPath(String fileName) {
        return getPath(fileName, isExternalAvailable());
    }

    /**
     * Get the cache path for corresponding file name.
     *
     * @param fileName file name.
     * @param external external or internal storage.
     */
    public String getPath(String fileName, boolean external) {
        if (isEmpty(fileName)) {
            return null;
        }
        String dir = getDir(external);
        if (dir == null) {
            return null;
        }
        if (getCache(external).maxSize() <= 0) {
            // empty cache.
            return null;
        }
        StringBuilder sb = sStringBuilder.get();
        // clear before use.
        sb.setLength(0);
        sb.append(dir).append(File.separatorChar).append(fileName);
        return sb.toString();
    }

    /**
     * Get the cached file.
     *
     * @param fileName file name.
     * @return cached file, null if not exist.
     */
    public File getFile(String fileName) {
        return getFile(fileName, false);
    }

    /**
     * Get the cached file and(or) create it if needed. If cached file can be created
     * and returned, then this fileName will be {@link #putFile}.
     *
     * @param fileName         file name.
     * @param createIfNotExist whether create file if not exist.
     * @return cache file, null if not exist( when createIfNotExist is false), or cannot create file( when createIfNotExist is true).
     */
    public File getFile(String fileName, boolean createIfNotExist) {
        if (isEmpty(fileName)) {
            return null;
        }

        File file = getFileInner(fileName);
        if (file != null) {
            return file;
        }
        // create if not exist.
        if (createIfNotExist) {
            boolean externalAvailable = isExternalAvailable();
            file = createFile(fileName, externalAvailable);
            if (isFileValid(file)) {
                putFileInner(fileName, file, externalAvailable);
                return file;
            }
            if (externalAvailable) {
                // try internal one if needed.
                file = createFile(fileName, false);
                if (isFileValid(file)) {
                    putFileInner(fileName, file, false);
                    return file;
                }
            }
        }
        return null;
    }

    private File getFileInner(String fileName) {
        String path;
        File file;

        boolean externalAvailable = isExternalAvailable();
        path = getCache(externalAvailable).get(fileName);
        if (path == null && !mInitialized) {
            // in case of in-memory cache is not available yet.
            path = getPath(fileName, externalAvailable);
        }
        file = path == null ? null : new File(path);
        if (isFileValid(file)) {
            return file;
        }
        // try internal one if needed.
        if (externalAvailable) {
            path = getCache(false).get(fileName);
            if (path == null && !mInitialized) {
                // in case of in-memory cache is not available yet.
                path = getPath(fileName, false);
            }
            file = path == null ? null : new File(path);
            if (isFileValid(file)) {
                return file;
            }
        }
        return null;
    }

    /**
     * Put file into this cache. If the file is already in this cache, only memory record will be updated.
     * Otherwise, file will be moved into this cache as well.
     *
     * @param fileName cache file name.
     * @param file     file to put. Null means use the file generated by {@link #getPath(String)}.
     */
    public boolean putFile(String fileName, File file) {
        if (isEmpty(fileName)) {
            return false;
        }

        if (file == null) {
            // try to use file already in cache.
            boolean externalAvailable = isExternalAvailable();
            String path = getPath(fileName, externalAvailable);
            file = path != null ? new File(path) : null;
            // try internal one.
            if (externalAvailable && !isFileValid(file)) {
                path = getPath(fileName, false);
                file = path != null ? new File(path) : null;
            }
        }
        if (!isFileValid(file)) {
            return false;
        }

        boolean put;
        boolean external = !StorageUtils.isInternal(file.getAbsolutePath());
        put = putFileInner(fileName, file, external);
        if (!put) {
            // try another one.
            put = putFileInner(fileName, file, !external);
        }
        return put;
    }

    private boolean putFileInner(String fileName, File file, boolean external) {
        FileCache<String> cache = getCache(external);
        String path = getPath(fileName, external);
        if (path == null) {
            return false;
        }

        boolean result;
        File dstFile = new File(path);
        if (file.getAbsolutePath().equals(dstFile.getAbsolutePath())) {
            // equals, no need to move file.
            result = true;
        } else {
            result = FileUtils.moveFiles(file, dstFile);
        }

        if (result) {
            cache.put(fileName, dstFile.getAbsolutePath());
            // ensure storage after put.
            ensureStorage(external);
        }
        return result;
    }

    /**
     * Delete cache file, both external and internal.
     */
    public void deleteFile(String fileName) {
        if (isEmpty(fileName)) {
            return;
        }
        // delete cache entry.
        getCache(false).remove(fileName);
        getCache(true).remove(fileName);
        // delete file, not necessary.
        String internalPath = getPath(fileName, false);
        String externalPath = getPath(fileName, true);
        if (internalPath != null) {
            FileUtils.delete(new File(internalPath));
        }
        if (externalPath != null) {
            FileUtils.delete(new File(externalPath));
        }
    }

    public int getSize(boolean external) {
        return external ? mExternalCache.size() : mInternalCache.size();
    }

    /**
     * Get the capacity of file cache.
     *
     * @param external external storage or not.
     */
    public int getCapacity(boolean external) {
        return external ? mExternalCache.maxSize() : mInternalCache.maxSize();
    }

    /**
     * Whether this file cache is persist, which means it can be accessed persist.
     */
    public boolean isPersist() {
        return mPersist;
    }

    private FileCache<String> getCache(boolean external) {
        return external ? mExternalCache : mInternalCache;
    }

    private String getDir(boolean external) {
        return external ? StorageUtils.getExternalCacheDir(mContext, mName, mPersist)
                : StorageUtils.getInternalCacheDir(mContext, mName, mPersist);
    }

    private File createFile(String fileName, boolean external) {
        String path = getPath(fileName, external);
        if (path == null) {
            return null;
        }
        try {
            File file = new File(path);
            // delete original file, if exist.
            FileUtils.delete(file);
            file.createNewFile();
            return file;

        } catch (IOException e) {
            Logger.i(TAG, "fail to create file " + path, e);
        }
        return null;
    }

    private void ensureStorage(boolean external) {
        FileStorageService storageService = FileCacheManager.getFileStorageService(mContext);
        if (storageService != null) {
            storageService.checkStorage(external ? FileStorageService.Mode.EXTERNAL : FileStorageService.Mode.INTERNAL);
        }
    }

    @Override
    public String toString() {
        return "FileCache#" + mName
                + "#capacity=" + getCapacity(true) + "," + getCapacity(false)
                + "#size=" + getSize(true) + "," + getSize(false);
    }

    // ---------------- internal utils -----------------
    private boolean isExternalAvailable() {
        return StorageUtils.isExternalWritable(mContext) && getCache(true).maxSize() > 0;
    }

    private static boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile();
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * increase order.
     */
    private static Comparator<FileEntry> sFileComparator = new Comparator<FileEntry>() {
        @Override
        public int compare(FileEntry lhs, FileEntry rhs) {
            return lhs.lastModified < rhs.lastModified ? -1 : (lhs.lastModified == rhs.lastModified ? 0 : 1);
        }
    };

    final static class FileEntry {

        public final String path;
        public final String name;
        public final long lastModified;

        public final boolean isFile;

        public FileEntry(String dir, String name) {
            File file = new File(dir, name);

            this.path = file.getPath();
            this.name = name;
            this.lastModified = file.lastModified();

            this.isFile = true;//file.isFile();
        }
    }
}
