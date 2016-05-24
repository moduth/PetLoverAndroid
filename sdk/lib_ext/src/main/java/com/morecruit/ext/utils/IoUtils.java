package com.morecruit.ext.utils;

import android.database.Cursor;

import com.morecruit.ext.Ext;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import com.morecruit.ext.component.logger.Logger;

/**
 * IO操作工具
 *
 * @author markzhai on 16/3/5
 */
public class IoUtils {
    private static final String TAG = "IoUtils";
    private static final int BUFFER_SIZE = 1024; // 流转换的缓存大小
    private static final int CONNECT_TIMEOUT = 3000; // 从网络下载文件时的连接超时时间

    /**
     * 静默地关闭一个 Closeable 对象 (无视关闭中的一切错误).
     *
     * @param closeable Closeabl e对象.
     */
    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable e) {
                // ignore.
            }
        }
    }

    /**
     * 静默地关闭一个 Cursor 对象 (无视关闭中的一切错误).
     *
     * @param cursor Cursor 对象.
     */
    public static void closeSilently(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            try {
                cursor.close();
            } catch (Throwable e) {
                // ignore.
            }
        }
    }

    /**
     * 使用charset指定的字符集编码将inputStream转换成String
     *
     * @param is      inputStream
     * @param charset 字符集名
     * @throws IOException
     */
    public static String inputStreamToString(InputStream is, String charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        String line = null;
        boolean isFirstLine = true;
        while ((line = reader.readLine()) != null) {
            if (!isFirstLine) {
                sb.append("\n");
            }
            isFirstLine = false;
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * 从InputStream中读取Bytes
     *
     * @throws IOException
     */
    public static byte[] inputStreamToBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = null;
        baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int length = 0;
        while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
            baos.write(buffer, 0, length);
            baos.flush();
        }
        return baos.toByteArray();
    }

    /**
     * 从Assets读取文字
     */
    public static String readStringFromAssets(String fileName) {
        return readStringFromAssets(fileName, Charset.UTF8);
    }

    /**
     * 从Assets读取文字
     */
    public static String readStringFromAssets(String fileName, String encoding) {
        InputStream is = null;
        try {
            is = Ext.getContext().getAssets().open(fileName);
            return inputStreamToString(is, encoding);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 从raw资源中读取文字
     */
    public static String readStringFromRawResource(int resId) {
        return readStringFromRawResource(resId, Charset.UTF8);
    }

    /**
     * 从资源中读取文字
     */
    public static String readStringFromRawResource(int resId, String encoding) {
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            is = Ext.getContext().getResources().openRawResource(resId);
            return inputStreamToString(is, encoding);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return "";
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 从指定路径的文件中读取String
     */
    public static String readStringFromFile(String filePath) {

        return readStringFromFile(filePath, Charset.UTF8);
    }

    /**
     * 从指定路径的文件中读取String
     */
    public static String readStringFromFile(String filePath, String encoding) {
        try {
            return new String(readBytesFromFile(filePath), encoding);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return "";
        }
    }

    /**
     * 从指定路径的文件中读取byte数组
     */
    public static byte[] readBytesFromFile(String filePath) {
        File file = new File(filePath);
        return readBytesFromFile(file);
    }

    /**
     * 从File中读取byte数组
     */
    public static byte[] readBytesFromFile(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return inputStreamToBytes(fis);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return new byte[0];
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 从指定的raw资源中读取Bytes
     */
    public static byte[] readBytesFromRawResource(int resId) {
        InputStream is = null;
        try {
            is = Ext.getContext().getResources().openRawResource(resId);
            return inputStreamToBytes(is);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return new byte[0];
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 从Url中读取Bytes
     */
    public static byte[] readBytesFromURL(URL url) {
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(CONNECT_TIMEOUT);
            conn.connect();
            is = conn.getInputStream();
            return inputStreamToBytes(is);
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return new byte[0];
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    public static boolean writeToFile(File file, String text) {
        return writeToFile(file, text, Charset.UTF8);
    }

    public static boolean writeToFile(File file, String text, String encoding) {
        try {
            return writeToFile(file, text.getBytes(encoding));
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return false;
        }
    }

    public static boolean writeToFile(File file, byte[] buffer) {
        return writeToFile(file, new ByteArrayInputStream(buffer));
    }

    /**
     * 将InputStream写入File
     */
    public static boolean writeToFile(File file, InputStream is) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while ((length = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
                fos.write(buffer, 0, length);
                fos.flush();
            }
            return true;
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 下载文件至path目录，文件名根据url自动生成
     */
    public static File downloadToFile(String url, String path) {
        return downloadToFile(url, path, null);
    }

    /**
     * 下载文件至path目录，文件名由fileName指定
     *
     * @param url      文件的url
     * @param path     存储文件的目录
     * @param fileName 存储的文件名
     */
    public static File downloadToFile(String url, String path, String fileName) {
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            URL myURL = new URL(url);
            conn = (HttpURLConnection) myURL.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(CONNECT_TIMEOUT);
            conn.connect();
            is = conn.getInputStream();

            if (fileName == null) {
                fileName = makeFileNameFromUrl(url);
            }
            File file = new File(path, fileName);
            writeToFile(file, is);
            return file;
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.disconnect();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
    }

    /**
     * 释放assert资源至path目录
     */
    public static File releaseAssertToFile(String assertName, String toPath) {
        InputStream is = null;
        File file = new File(toPath, assertName);
        try {
            makedirs(toPath);
            is = Ext.getContext().getAssets().open(assertName);
            if (writeToFile(file, is)) {
                return null;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
        return file;
    }

    /**
     * 释放raw资源至path目录
     */
    public static File releaseRawToFile(int rawResId, String toPath, String toFileName) {
        InputStream is = null;
        File file = new File(toPath, toFileName);
        try {
            makedirs(toPath);
            is = Ext.getContext().getResources().openRawResource(rawResId);
            if (writeToFile(file, is)) {
                return null;
            }
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Logger.e(TAG, e.getMessage(), e);
            }
        }
        return file;
    }

    /**
     * 根据url生成一个合法的文件名（即去掉一些不能作为文件名的非法字符）
     */
    public static String makeFileNameFromUrl(String url) {

        if (url == null) {
            return null;
        }
        //要先从原url中把域名去掉
        int start = url.indexOf("//");
        start = start == -1 ? 0 : start;
        start = url.indexOf('/', start + 2);
        start = start == -1 ? 0 : start;
        return url.substring(start).replaceAll("[^\\w\\-_]", "");//只保留部分字符
    }

    /**
     * 根据url生成一个MD5字符串文件名
     */
    public static String makeMD5FileNameFromUrl(String url) {

        if (url == null) {
            return null;
        }
        return CryptoUtils.bytesToHexString(CryptoUtils.MD5.toMd5(url));
    }

    /**
     * 创建目录
     */
    public static boolean makedirs(String dirPath) {
        File file = new File(dirPath);
        return file.mkdirs() || (file.exists() && file.isDirectory());
    }

    /**
     * 复制文件
     *
     * @param oldPath 旧路径
     * @param newPath 新路径
     */
    public static void copyFile(String oldPath, String newPath) {
        FileInputStream fin = null;
        FileOutputStream fout = null;
        try {
            int byteRead = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                fin = new FileInputStream(oldPath); //读入原文件
                fout = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteRead = fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, byteRead);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (fout != null) {
                try {
                    fout.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取文件
     */
    public static String readfile(String filepath) {
        String filecontent = "";
        File file = new File(filepath);
        if (!file.isDirectory()) {
            filecontent = IoUtils.readStringFromFile(filepath);
            return filecontent;
        } else if (file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(filepath + File.separator + filelist[i]);
                if (!readfile.isDirectory()) {
                    filecontent = IoUtils.readStringFromFile(filepath + File.separator + filelist[i]);
                    filepath = filepath + File.separator + filelist[i];
                    return filecontent;
                } else if (readfile.isDirectory()) {
                    readfile(filepath + File.pathSeparator + filelist[i]);
                }
            }
        }
        return "";
    }

    /**
     * 获取文件夹下文件数量
     */
    public static int getFileSize(String path) {
        File file = new File(path);
        String files[] = null;
        int num = 0;
        if (file.isDirectory()) {
            files = file.list();
        }
        if (files != null) {
            num = files.length;
        }
        return num;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    public static void deleteOldFile(String path, int day) {
        //计算一周前的日期
        Calendar cdweek = Calendar.getInstance();
        cdweek.add(Calendar.DATE, -day);
        Date d = cdweek.getTime();
        //1.获得系统目录下的所有的文件
        File fileBag = new File(path);
        if (fileBag.isDirectory()) {
            String[] filesName = fileBag.list();
            for (int i = 0; i < filesName.length; i++) {
                File file = new File(path + filesName[i]);
                //文件的最后一次修改的时间
                Long time = file.lastModified();
                Calendar cd = Calendar.getInstance();
                cd.setTimeInMillis(time);
                Date fileDate = cd.getTime();
                boolean flag = fileDate.before(d);
                if (flag) {
                    file.delete();
                }
            }
        }
    }
}
