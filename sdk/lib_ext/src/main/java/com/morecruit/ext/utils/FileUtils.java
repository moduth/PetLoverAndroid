package com.morecruit.ext.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import com.morecruit.ext.Ext;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.morecruit.ext.component.logger.Logger;

/**
 * @author markzhai on 16/2/29
 * @version 1.0.0
 */
public final class FileUtils {
    /**
     * Simple file comparator which only depends on file length and modification time.
     */
    public final static FileComparator SIMPLE_COMPARATOR = (lhs, rhs) ->
            (lhs.length() == rhs.length()) && (lhs.lastModified() == rhs.lastModified());
    /**
     * Strict file comparator which depends on file md5.
     */
    public final static FileComparator STRICT_COMPARATOR = (lhs, rhs) -> {
        String lhsMd5 = SecurityUtils.digest(lhs);
        if (lhsMd5 == null) {
            return false;
        }
        String rhsMd5 = SecurityUtils.digest(rhs);
        return lhsMd5.equals(rhsMd5);
    };
    /**
     * Simple asset file comparator which only depends on asset file length.
     */
    public final static AssetFileComparator SIMPLE_ASSET_COMPARATOR =
            (context, assetPath, dstFile) -> {
                long assetFileLength = getAssetLength(context, assetPath);
                return assetFileLength != -1 && assetFileLength == dstFile.length();
            };
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final String LOG_TAG = "FileUtils";
    private final static String TAG = "FileUtils";
    private final static int ASSET_SPLIT_BASE = 0;
    private static String mSeparator = File.separator;
    private static char mSeparatorChar = File.separatorChar;

    private FileUtils() {
        // static usage.
    }

    /**
     * Copy files. If src is a directory, then all it's sub files will be copied into directory dst.
     * If src is a file, then it will be copied to file dst. Notice, a {@link #SIMPLE_COMPARATOR} is used.
     * If src equals to dst file, this will do nothing and true will be returned.
     *
     * @param src file or directory to copy.
     * @param dst destination file or directory.
     * @return true if copy complete perfectly, false otherwise (more than one file cannot be copied).
     */
    public static boolean copyFiles(File src, File dst) {
        return copyFiles(src, dst, null);
    }

    /**
     * Copy files. If src is a directory, then all it's sub files will be copied into directory dst.
     * If src is a file, then it will be copied to file dst. Notice, a {@link #SIMPLE_COMPARATOR} is used.
     * If src equals to dst file, this will do nothing and true will be returned.
     *
     * @param src    file or directory to copy.
     * @param dst    destination file or directory.
     * @param filter a file filter to determine whether or not copy corresponding file.
     * @return true if copy complete perfectly, false otherwise (more than one file cannot be copied).
     */
    public static boolean copyFiles(File src, File dst, FileFilter filter) {
        return copyFiles(src, dst, filter, SIMPLE_COMPARATOR);
    }

    /**
     * Copy files. If src is a directory, then all it's sub files will be copied into directory dst.
     * If src is a file, then it will be copied to file dst. Notice, if src equals to dst file,
     * this will do nothing and true will be returned.
     *
     * @param src        file or directory to copy.
     * @param dst        destination file or directory.
     * @param filter     a file filter to determine whether or not copy corresponding file.
     * @param comparator a file comparator to determine whether src & dst are equal files. Null to overwrite all dst files.
     * @return true if copy complete perfectly, false otherwise (more than one file cannot be copied).
     */
    public static boolean copyFiles(File src, File dst, FileFilter filter, FileComparator comparator) {
        if (src == null || dst == null) {
            return false;
        }
        if (!src.exists()) {
            return false;
        }
        if (src.getAbsolutePath().equals(dst.getAbsolutePath())) {
            // ignore.
            return true;
        }
        if (src.isFile()) {
            return performCopyFile(src, dst, filter, comparator);
        }

        File[] paths = src.listFiles();
        if (paths == null) {
            return false;
        }
        // default is true.
        boolean result = true;
        for (File sub : paths) {
            if (!copyFiles(sub, new File(dst, sub.getName()), filter)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Perform copy file (not directory).
     *
     * @param srcFile    Source file.
     * @param dstFile    Destination File.
     * @param filter     File filter.
     * @param comparator File comparator.
     * @return true if succeed.
     */
    private static boolean performCopyFile(File srcFile, File dstFile, FileFilter filter, FileComparator comparator) {
        if (filter != null && !filter.accept(srcFile)) {
            // filtered, regarded as succeed.
            return true;
        }

        FileChannel inc = null;
        FileChannel ouc = null;
        try {
            if (dstFile.exists()) {
                if (comparator != null && comparator.equals(srcFile, dstFile)) {
                    // equal files.
                    return true;
                } else {
                    // delete it in case of folder.
                    delete(dstFile);
                }
            }

            File toParent = dstFile.getParentFile();
            if (toParent.isFile()) {
                delete(toParent);
            }
            if (!toParent.exists() && !toParent.mkdirs()) {
                return false;
            }

            inc = (new FileInputStream(srcFile)).getChannel();
            ouc = (new FileOutputStream(dstFile)).getChannel();

            ouc.transferFrom(inc, 0, inc.size());

        } catch (Throwable e) {
            Logger.i(TAG, "fail to copy file", e);
            // exception occur, delete broken file.
            delete(dstFile);
            return false;
        } finally {
            IoUtils.closeSilently(inc);
            IoUtils.closeSilently(ouc);
        }
        return true;
    }

    /**
     * Copy file corresponding stream to des file. The source input stream will be left open.
     *
     * @param source source input stream.
     * @param dst    destination file.
     * @return true if copy complete perfectly, false otherwise.
     */
    public static boolean copyFile(InputStream source, File dst) {
        return copyFile(source, dst, false);
    }

    /**
     * Copy file corresponding stream to des file.
     *
     * @param source          source input stream.
     * @param dst             destination file.
     * @param closeWhenFinish whether closeWhenFinish source input stream when operation finished.
     * @return true if copy complete perfectly, false otherwise.
     */
    public static boolean copyFile(InputStream source, File dst, boolean closeWhenFinish) {
        if (source == null || dst == null) {
            return false;
        }
        OutputStream ous = null;
        try {
            ous = new BufferedOutputStream(new FileOutputStream(dst), BUFFER_SIZE);
            return performCopyStream(source, ous);

        } catch (Throwable e) {
            Logger.i(TAG, "fail to copy file", e);
        } finally {
            if (closeWhenFinish) {
                IoUtils.closeSilently(source);
            }
            IoUtils.closeSilently(ous);
        }
        return false;
    }

    private static boolean performCopyStream(InputStream ins, OutputStream ous) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = ins.read(buffer)) > 0) {
                ous.write(buffer, 0, count);
            }
            return true;

        } catch (Throwable e) {
            Logger.i(TAG, "fail to copy stream", e);
        }
        return false;
    }

    /**
     * Move files. If src is a directory, then all it's sub files will be moved into directory dst.
     * If src is a file, then it will be moved to file dst. Notice, if src equals to dst file,
     * this will do nothing and true will be returned.
     *
     * @param src file or directory to move.
     * @param dst destination file or directory.
     * @return true if move complete perfectly, false otherwise (more than one file cannot be moved).
     */
    public static boolean moveFiles(File src, File dst) {
        return moveFiles(src, dst, null);
    }

    /**
     * Move files. If src is a directory, then all it's sub files will be moved into directory dst.
     * If src is a file, then it will be moved to file dst. Notice, if src equals to dst file,
     * this will do nothing and true will be returned.
     *
     * @param src    file or directory to move.
     * @param dst    destination file or directory.
     * @param filter filter to determine which file should be moved, null for all files.
     * @return true if move complete perfectly, false otherwise (more than one file cannot be moved).
     */
    public static boolean moveFiles(File src, File dst, FileFilter filter) {
        if (src == null || dst == null) {
            return false;
        }
        if (!src.exists()) {
            return false;
        }
        if (src.getAbsolutePath().equals(dst.getAbsolutePath())) {
            // ignore.
            return true;
        }
        boolean sameStorage = StorageUtils.isInternal(src.getAbsolutePath())
                == StorageUtils.isInternal(dst.getAbsolutePath());
        // in same storage (mount point), try to rename instead of copy & delete.
        return performMoveFiles(src, dst, filter, sameStorage);
    }

    private static boolean performMoveFiles(File src, File dst, FileFilter filter, boolean rename) {
        if (src.isFile()) {
            boolean result = false;
            if (rename) {
                result = performRenameFile(src, dst, filter);
            }
            if (!result) {
                // copy & delete.
                result = performCopyFile(src, dst, filter, null);
                delete(src);
            }
            return result;
        }

        File[] paths = src.listFiles();
        if (paths == null) {
            return false;
        }
        // default is true.
        boolean result = true;
        for (File sub : paths) {
            if (!performMoveFiles(sub, new File(dst, sub.getName()), filter, rename)) {
                result = false;
            }
        }
        return result;
    }

    private static boolean performRenameFile(File srcFile, File dstFile, FileFilter filter) {
        if (filter != null && !filter.accept(srcFile)) {
            // filtered, regarded as succeed.
            return true;
        }

        if (dstFile.exists()) {
            delete(dstFile);
        }
        File toParent = dstFile.getParentFile();
        if (toParent.isFile()) {
            delete(toParent);
        }
        if (!toParent.exists() && !toParent.mkdirs()) {
            return false;
        }
        return srcFile.renameTo(dstFile);
    }

    /**
     * Copy asset files. If assetName is a file, the it will be copied to file dst. Notice, a {@link #SIMPLE_ASSET_COMPARATOR} is used.
     *
     * @param context   application context.
     * @param assetName asset name to copy.
     * @param dst       destination file.
     */
    public static boolean copyAssets(Context context, String assetName, String dst) {
        return copyAssets(context, assetName, dst, SIMPLE_ASSET_COMPARATOR);
    }

    /**
     * Copy asset files. If assetName is a file, the it will be copied to file dst.
     *
     * @param context    application context.
     * @param assetName  asset name to copy.
     * @param dst        destination file.
     * @param comparator a asset file comparator to determine whether asset & dst are equal files. Null to overwrite all dst files.
     */
    public static boolean copyAssets(Context context, String assetName, String dst, AssetFileComparator comparator) {
        return performCopyAssetsFile(context, assetName, dst, comparator);
    }

    private static boolean performCopyAssetsFile(Context context, String assetPath, String dstPath, AssetFileComparator comparator) {
        if (isEmpty(assetPath) || isEmpty(dstPath)) {
            return false;
        }

        AssetManager assetManager = context.getAssets();
        File dstFile = new File(dstPath);

        boolean succeed = false;
        InputStream in = null;
        OutputStream out = null;
        try {
            if (dstFile.exists()) {
                if (comparator != null && comparator.equals(context, assetPath, dstFile)) {
                    return true;
                } else {
                    // file will be overwrite later.
                    if (dstFile.isDirectory()) {
                        delete(dstFile);
                    }
                }
            }

            File parent = dstFile.getParentFile();
            if (parent.isFile()) {
                delete(parent);
            }
            if (!parent.exists() && !parent.mkdirs()) {
                return false;
            }

            in = assetManager.open(assetPath);
            out = new BufferedOutputStream(new FileOutputStream(dstFile), BUFFER_SIZE);
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            succeed = true;

        } catch (Throwable e) {
            Logger.i(TAG, "fail to copy assets file", e);
            // delete broken file.
            delete(dstFile);
        } finally {
            IoUtils.closeSilently(in);
            IoUtils.closeSilently(out);
        }
        return succeed;
    }

    public static long getAssetLength(Context context, String assetPath) {
        AssetManager assetManager = context.getAssets();
        // try to determine whether or not copy this asset file, using their size.
        try {
            AssetFileDescriptor fd = assetManager.openFd(assetPath);
            return fd.getLength();

        } catch (IOException e) {
            // this file is compressed. cannot determine it's size.
        }

        // try stream.
        InputStream tmpIn = null;
        try {
            tmpIn = assetManager.open(assetPath);
            return tmpIn.available();

        } catch (IOException e) {
            // do nothing.
        } finally {
            IoUtils.closeSilently(tmpIn);
        }
        return -1;
    }

    /**
     * Delete corresponding path, file or directory.
     *
     * @param file path to delete.
     */
    public static boolean delete(File file) {
        return delete(file, false);
    }

    /**
     * Delete corresponding path, file or directory.
     *
     * @param file      path to delete.
     * @param ignoreDir whether ignore directory. If true, all files will be deleted while directories is reserved.
     */
    public static boolean delete(File file, boolean ignoreDir) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }

        File[] fileList = file.listFiles();
        if (fileList == null) {
            return false;
        }

        for (File f : fileList) {
            delete(f, ignoreDir);
        }
        // delete the folder if need.
        return !ignoreDir && file.delete();
    }

    /**
     * Delete corresponding path, file or directory.
     *
     * @param file   path to delete.
     * @param filter filter to determine which file should be deleted, null for all files.
     */
    public static boolean delete(File file, FileFilter filter) {
        if (file == null || !file.exists()) {
            return false;
        }
        if (file.isFile()) {
            // delete file if need.
            return (filter == null || filter.accept(file)) && file.delete();
        }

        File[] fileList = file.listFiles();
        if (fileList == null) {
            return false;
        }

        for (File f : fileList) {
            delete(f, filter);
        }
        // delete the folder if need.
        return (filter == null || filter.accept(file)) && file.delete();
    }

    /**
     * Create directory if incoming directory not exists or is a file (delete before create it).
     *
     * @param dir Directory to create.
     * @return {@code true} if corresponding directory exists or is created sucessfully.
     */
    public static boolean mkdirs(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                return true;
            } else {
                // delete file.
                delete(dir);
            }
        }
        return dir.mkdirs();
    }

    private static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    // ======== From TextFileOperation.java ============
    public static synchronized void save(String filename, String content)
            throws Exception {
        File file = Ext.getContext().getFileStreamPath(filename);
        FileOutputStream outStream = new FileOutputStream(file);
        outStream.write(content.getBytes());
        outStream.close();
    }

    public static String readFile(String filename) {
        byte[] data = null;
        File file = Ext.getContext().getFileStreamPath(filename);
        FileInputStream inStream;
        try {
            inStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 4];
            int len = 0;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int temp1 = inStream.read(buffer);
            // Logger.v("ReadFileLength1", "" + temp1);
            if (temp1 == -1) {
                Logger.v("FileUtils", "DoReadFileFuncuBufferNull: ");
                return null;
            } else {
                // for(;len<0;){
                // len= inStream.read(buffer);
                // outStream.write(buffer, 0, len);
                // }
                outStream.write(buffer, 0, temp1);
                // int temp2 = inStream.read(buffer);
                // Logger.v("ReadFileLength2", "" + temp2);
                while ((len = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
            }
            data = outStream.toByteArray();
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            Logger.v("FileUtils", "ERROR_DoReadFileFunc: ", e);
            e.printStackTrace();
        }

        return new String(data);
    }

    /**
     * 设置文件路径中的分割符号，一般情况下，windows上为"\"，其他平台大多为"/"
     *
     * @param separatorChar char
     */
    public static void setSeparatorChar(char separatorChar) {
        mSeparatorChar = separatorChar;
        mSeparator = String.valueOf(mSeparatorChar);
    }

    /**
     * 判断路径是否存在
     *
     * @param path 路径
     * @return 如果条件成立，返回true
     */
    public static boolean exists(String path) {
        return !StringUtils.isEmpty(path) && new File(path).exists();
    }

    /**
     * 判断路径是文件，且存在
     *
     * @param path 文件路径，如果传入null字符串，则认为文件不存在
     * @return 如果条件成立，返回true;
     */
    public static boolean fileExists(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * 设置最后修改时间
     *
     * @param filePath     文件路径
     * @param lastModified 时间
     * @return true表示成功
     */
    public static boolean updateFileLastModified(String filePath, long lastModified) {
        return (fileExists(filePath)) && new File(filePath).setLastModified(lastModified);
    }

    /**
     * 文件长度，文件存在返回为0
     *
     * @param path 文件路径
     * @return 文件长度
     */
    public static long fileLength(String path) {
        if (fileExists(path)) {
            return new File(path).length();
        }

        return 0;
    }

    /**
     * 判断路径是文件夹，且存在
     *
     * @param path 文件夹路径
     * @return 如果条件成立，返回true;
     */
    public static boolean folderExists(String path) {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    /**
     * 创建文件， 如果不存在则创建，否则返回原文件的File对象
     *
     * @param path 文件路径
     * @return 创建好的文件对象, 返回为空表示失败
     */
    synchronized public static File createFile(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }

        File file = new File(path);
        if (file.isFile()) {
            return file;
        }

        File parentFile = file.getParentFile();
        if (parentFile != null && (parentFile.isDirectory() || parentFile.mkdirs())) {
            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 创建目录。
     * <b>如果目录不存在，创建目录, 如果目录已存在，不再重新创建</b>
     *
     * @param path 目录路径
     * @return 创建好的目录文件对象
     */
    synchronized public static File createFolder(String path) {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        return file.isDirectory() || file.mkdirs() ? file : null;
    }

    /**
     * 获取文件大小
     * <br>如果文件是目录，则返回该目录下所有文件大小的总和，否则返回该文件的大小
     *
     * @param path 文件夹路径
     * @return 所有文件的大小
     */
    synchronized public static long getFileSize(String path) {
        if (StringUtils.isEmpty(path)) {
            return 0;
        }

        return getFileSize(new File(path));
    }

    /**
     * 获取文件大小
     * <br>如果文件是目录，则返回该目录下所有文件大小的总和，否则返回该文件的大小
     *
     * @param file file
     * @return 所有文件的大小
     */
    synchronized public static long getFileSize(File file) {
        long size = 0;

        if (null == file) {
            return 0;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += f.isDirectory() ? getFileSize(f) : f.length();
                }
            }
        } else {
            size = file.length();
        }
        return size;
    }

    /**
     * 删除指定目录中距离现在时间超过interval的文件
     *
     * @param path     目录路径
     * @param interval 时间(单位:毫秒)
     * @return 删除文件个数
     */
    synchronized public static int clearFolder(String path, long interval) {
        if (StringUtils.isEmpty(path)) {
            return 0;
        }
        return clearFolder(new File(path), interval);
    }

    /**
     * 删除指定目录文件，最多保留 maxRemainSize的大小, 如果超过maxRemainSize会把最早的文件删除
     *
     * @param path          文件夹的路径
     * @param maxRemainSize 这个文件夹所包含的文件
     * @param distinct      与clearFolder(String path, long interval)区别
     */
    synchronized public static void clearFolder(String path, long maxRemainSize, boolean distinct) {
        clearFolder(path, maxRemainSize, null);
    }

    /**
     * 删除指定目录文件，最多保留 maxRemainSize的大小, 如果超过maxRemainSize会把最早的文件删除
     *
     * @param path          文件夹的路径
     * @param maxRemainSize 这个文件夹所包含的文件
     * @param except        排除之外的文件路径
     */
    synchronized public static void clearFolder(String path, long maxRemainSize, String[] except) {
        long size = getFileSize(path);
        if (size > maxRemainSize) {
            List<String> exceptFilePathList = except == null ? new ArrayList<String>() : Arrays.asList(except);
            File[] files = new File(path).listFiles();
            if (files != null) {
                List<File> fileList = Arrays.asList(files);
                try {
                    Collections.sort(fileList, new Comparator<File>() {
                        @Override
                        public int compare(File lhs, File rhs) {
                            if (lhs.lastModified() == rhs.lastModified()) {
                                return 0;
                            }
                            return (lhs.lastModified() < rhs.lastModified()) ? -1 : 1;
                        }
                    });
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }

                for (File file : fileList) {
                    if (size <= maxRemainSize) {
                        break;
                    }

                    if (!exceptFilePathList.contains(file.getAbsolutePath())) {
                        file.lastModified();
                        size -= file.length();
                        file.delete();
                    }
                }
            }
        }
    }

    /**
     * 删除指定目录中距离现在时间超过interval(单位:毫秒)的文件
     *
     * @param path     目录路径
     * @param interval 时间(单位:毫秒)
     * @return 删除文件个数
     */
    synchronized public static int clearFolder(File path, long interval) {
        long expiredTimeMillis = System.currentTimeMillis() - interval;
        int deletedItems = 0;
        File[] fileList = path.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    deletedItems += clearFolder(file, interval);
                }
                if (file.lastModified() < expiredTimeMillis) {
                    if (file.delete()) {
                        deletedItems++;
                    }
                }
            }
        }
        return deletedItems;
    }

    /**
     * 删除指定目录中距离现在时间超过interval(单位:毫秒)的文件
     *
     * @param path     目录路径
     * @param interval 时间(单位:毫秒)
     * @return 删除文件个数
     */
    public static int clearFolderNoSynchronized(File path, long interval) {
        final long expiredTimeMillis = System.currentTimeMillis() - interval;
        int deletedFiles = 0;
        File[] fileList = path.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    deletedFiles += clearFolder(file, interval);
                }
                if (file.lastModified() < expiredTimeMillis) {
                    if (file.delete()) {
                        deletedFiles++;
                    }
                }
            }
        }
        return deletedFiles;
    }

    /**
     * 无条件删除指定目录中的文件
     *
     * @param path 目录路径
     * @return 删除文件个数
     */
    synchronized public static int clearFolder(File path) {
        int deletedItems = 0;
        File[] fileList = path.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    deletedItems += clearFolder(file);
                }
                if (file.delete()) {
                    deletedItems++;
                }
            }
        }
        return deletedItems;
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录路径。
     * @return true 表示删除成功，否则为失败
     */
    public static boolean deleteNoSynchronized(String path) {
        return !StringUtils.isEmpty(path) && deleteNoSynchronized(new File(path));
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录。
     * @return true 表示删除成功，否则为失败
     */
    public static boolean deleteNoSynchronized(File path) {
        if (null == path) {
            return true;
        }

        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (!delete(file)) {
                        return false;
                    }
                }
            }
        }
        return !path.exists() || path.delete();
    }

    /**
     * 删除文件或目录
     *
     * @param filePath 文件或目录路径。
     * @return true 表示删除成功，否则为失败
     */
    synchronized public static boolean delete(String filePath) {
        return !StringUtils.isEmpty(filePath) && delete(new File(filePath));
    }

    /**
     * 读取文件内容,并以字符串形式返回
     *
     * @param path 文件路径
     * @return 文件内容
     */
    public static String load(String path) {
        if (path == null) {
            throw new NullPointerException("path should not be null.");
        }

        String string = null;
        try {
            string = StringUtils.stringFromInputStream(new FileInputStream(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string != null ? string : "";
    }

    /**
     * 将字符串数据保存到文件.
     * <br/>注意：如果没有目录则会创建目录
     *
     * @param content 字符串内容
     * @param path    文件路径
     * @return 成功返回true, 否则返回false
     */
    public synchronized static boolean store(String content, String path) {
        if (path == null) {
            throw new NullPointerException("path should not be null.");
        }


        BufferedWriter bufferedWriter = null;
        try {
            File file = createFile(path);
            if (file == null) {
                Logger.d(LOG_TAG, "file == null path=" + path);
                //可能无存储卡或者其他原因导致
                return false;
            }
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(content != null ? content : "");
            bufferedWriter.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 将输入流保存到文件，并关闭流.
     *
     * @param inputStream 字符串内容
     * @param path        文件路径
     * @return boolean
     */
    synchronized public static boolean store(InputStream inputStream, String path) {
        if (path == null) {
            throw new NullPointerException("path should not be null.");
        }
        int length;

        FileOutputStream fileOutputStream = null;

        try {
            File file = createFile(path);
            if (file == null) {
                Logger.d(LOG_TAG, "inputStream file == null path=" + path);
                //可能无存储卡或者其他原因导致
                return false;
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            fileOutputStream = new FileOutputStream(file);
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将输入流保存到文件，并关闭流.
     *
     * @param inputStream 字符串内容
     * @param filePath    文件路径
     * @return boolean
     */
    public static boolean storeNoSynchronized(InputStream inputStream, String filePath) {
        if (filePath == null) {
            throw new NullPointerException("filePath should not be null.");
        }
        int length;

        FileOutputStream storeFileOutputStream = null;

        try {
            File storeFile = createFile(filePath);
            if (storeFile == null) {
                Logger.d(LOG_TAG, "inputStream sotreFile == null filePath=" + filePath);
                //可能无存储卡或者其他原因导致
                return false;
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            storeFileOutputStream = new FileOutputStream(storeFile);
            while ((length = inputStream.read(buffer)) > 0) {
                storeFileOutputStream.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (storeFileOutputStream != null) {
                try {
                    storeFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 复制文件
     *
     * @param desPath 目标文件路径
     * @param srcPath 源文件路径
     * @return false if file copy failed, true if file copy succeeded..
     */
    public static boolean copy(String desPath, String srcPath) {
        if (desPath == null || srcPath == null) {
            throw new NullPointerException("path should not be null.");
        }
        FileInputStream input = null;
        boolean succeed;

        try {
            input = new FileInputStream(srcPath);
            succeed = FileUtils.store(input, desPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            try {
                input.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            return false;
        }

        return succeed;
    }

    /**
     * 获取本地文件或URL的文件名. 包含后缀
     *
     * @param path 本地文件或URL路径
     * @return 文件名
     */
    public static String getFileName(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        int query = path.lastIndexOf('?');
        if (query > 0) {
            path = path.substring(0, query);
        }

        int filenamePos = path.lastIndexOf(mSeparatorChar);
        return (filenamePos >= 0) ? path.substring(filenamePos + 1) : path;
    }

    /**
     * 获取本地文件或URL的文件名. 不包含后缀
     *
     * @param path 本地文件或URL路径
     * @return 文件名
     */
    public static String getFileShortName(String path) {
        String fileName = getFileName(path);
        int separatorIndex = fileName.lastIndexOf('.');
        return separatorIndex > 0 ? fileName.substring(0, separatorIndex) : fileName;
    }

    /**
     * 获取文件所在目录的路径. 不包含最后的separatorChar
     *
     * @param path 文件路径
     * @return 文件所在目录的路径
     */
    public static String getFilePath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        int separatorIndex = -1;

        if (path != null && path.startsWith(mSeparator)) {
            separatorIndex = path.lastIndexOf(mSeparatorChar);
        }

        return (separatorIndex == -1) ? mSeparator : path.substring(0, separatorIndex);
    }

    /**
     * 获取本地文件或URL后缀名. 无后缀名时，返回空字符串
     *
     * @param path 本地文件或URL路径
     * @return 后缀名
     */
    public static String getFileExtension(String path) {
        if (!StringUtils.isEmpty(path)) {
            int query = path.lastIndexOf('?');
            if (query > 0) {
                path = path.substring(0, query);
            }

            int filenamePos = path.lastIndexOf('/');
            String filename = (filenamePos >= 0) ? path.substring(filenamePos + 1) : path;

            // if the filename contains special characters, we don't
            // consider it valid for our matching purposes:
            // 去掉了Pattern.matches("[a-zA-Z_0-9\\.\\-\\(\\)\\%]+", filename) 的判断，中文会返回false
            if (filename.length() > 0) {
                int dotPos = filename.lastIndexOf('.');
                if (0 <= dotPos) {
                    return filename.substring(dotPos + 1);
                }
            }
        }
        return "";
    }

    /**
     * 文件最近修改时间
     *
     * @param path 文件路径
     * @return 从1970年1月1日0点起，单位毫秒
     */
    public static long lastModified(String path) {
        if (StringUtils.isEmpty(path)) {
            return 0;
        }
        return new File(path).lastModified();
    }

    /**
     * 重命名文件
     *
     * @param srcPath 原名
     * @param dstPath 重命名后的文件名
     * @return 成功为true
     */
    public static boolean rename(String srcPath, String dstPath) {
        File file = new File(srcPath);
        return file.isFile() && file.renameTo(new File(dstPath));
    }

    /**
     * 合法化文件名
     * 替换文件名不允许出现的字符，比如{}/\:*?"<>以及无效或者不可视Unicode字符
     *
     * @param fileName 被合法化的文件名
     * @return 合法化后的文件名
     */
    public static String validateFileName(String fileName) {
        // {} \ / : * ? " < > |
        return fileName == null ? null : fileName.replaceAll("([{/\\\\:*?\"<>|}\\u0000-\\u001f\\uD7B0-\\uFFFF]+)", "");
    }

    /**
     * 获取真实的文件路径
     *
     * @param fileName fileName
     * @return 真实的文件路径
     */
    public static String getCanonicalPath(String fileName) {
        try {
            return new File(fileName).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 判断两个路径是否是同一个物理路径
     *
     * @param srcPath 源路径
     * @param dstPath 目标路径
     * @return true/false
     */
    public static boolean isSamePhysicalPath(String srcPath, String dstPath) {
        Long timeStamp = System.currentTimeMillis();
        String srcTmpFilePath = srcPath + File.separator + timeStamp.toString();
        boolean isSame = false;
        FileUtils.createFile(srcTmpFilePath);
        if (FileUtils.exists(srcTmpFilePath)) {
            String dstTmpFilePath = dstPath + File.separator + timeStamp;
            if (FileUtils.exists(dstTmpFilePath)) {
                isSame = true;
            }
        }
        FileUtils.delete(srcTmpFilePath);
        return isSame;
    }

    /**
     * Comparator of files.
     */
    public interface FileComparator {
        boolean equals(File lhs, File rhs);
    }

    /**
     * Comparator of asset and target file.
     */
    public interface AssetFileComparator {
        boolean equals(Context context, String assetPath, File dstFile);
    }
}
