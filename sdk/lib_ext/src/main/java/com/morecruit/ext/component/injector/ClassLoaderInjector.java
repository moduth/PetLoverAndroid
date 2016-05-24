package com.morecruit.ext.component.injector;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.morecruit.ext.utils.FileUtils;
import com.morecruit.ext.utils.SecurityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.morecruit.ext.component.logger.Logger;

/**
 * Class loader injector, 可以注入新的apk或者dex文件到runtime class loader.
 * <p>
 * Created by markzhai on 2015/8/4.
 */
public final class ClassLoaderInjector {

    private final static String TAG = "ClassLoaderInjector";

    private final static String LIBS_DIR = "libs";
    private final static String LIB_SUFFIX = ".zip";

    private final static String CHECKSUM_SUFFIX = ".checksum";

    private final static int BUFFER_SIZE = 128;

    private ClassLoaderInjector() {
        // static usage.
    }

    public static void injectWithAssets(Context context, String assetPath) throws Exception {
        if (TextUtils.isEmpty(assetPath)) {
            throw new RuntimeException("invalid asset path");
        }
        String targetPath = generateTargetPath(context, assetPath);
        if (TextUtils.isEmpty(targetPath)) {
            throw new RuntimeException("invalid target path for asset " + assetPath);
        }

        File targetFile = new File(targetPath);
        // verify.
        if (isFileValid(targetFile) && !verify(context, assetPath, targetPath)) {
            Logger.w(TAG, targetPath + " not pass the verification for " + assetPath);
            // delete file if not pass the verification.
            FileUtils.delete(targetFile);
        }
        // perform asset copy if needed.
        FileUtils.copyAssets(context, assetPath, targetPath);
        if (!isFileValid(targetFile)) {
            throw new FileNotFoundException("fail to copy asset file " + assetPath + " to target " + targetPath);
        }
        // perform inject.
        SystemClassLoaderInjector.inject(context, targetPath);
        // check class exist ?
        context.getClassLoader().loadClass(SystemClassLoaderInjector.ANTI_LAZY_LOAD);
        Logger.i(TAG, "succeed to perform inject for " + assetPath);
    }

    private static boolean verify(Context context, String assetPath, String targetPath) {
        String targetChecksum = SecurityUtils.digest(new File(targetPath));
        if (TextUtils.isEmpty(targetChecksum)) {
            Logger.w(TAG, "fail to read target checksum for " + targetPath);
            return false;
        }
        String checksum = readStringFromAssets(context, assetPath + CHECKSUM_SUFFIX);
        if (TextUtils.isEmpty(checksum)) {
            Logger.w(TAG, "fail to read checksum for " + assetPath);
            // should not happen.
            return false;
        }
        return checksum.equalsIgnoreCase(targetChecksum);
    }

    private static String readStringFromAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String s = null;
        Reader reader = null;
        try {
            reader = new InputStreamReader(assetManager.open(path));
            char[] buffer = new char[BUFFER_SIZE];
            StringBuilder sb = new StringBuilder();
            int count;
            while ((count = reader.read(buffer)) > 0) {
                sb.append(buffer, 0, count);
            }
            s = sb.toString();

        } catch (IOException e) {
            Logger.d(TAG, "error occurs while reading assets " + path, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // empty.
                }
            }
        }
        return s;
    }

    private static String generateTargetPath(Context context, String name) {
        name = SecurityUtils.digest(name) + LIB_SUFFIX;
        File file = context.getDir(LIBS_DIR, Context.MODE_PRIVATE);
        return file.getAbsolutePath() + File.separator + name;
    }

    private static boolean isFileValid(File file) {
        return file != null && file.exists() && file.isFile() && file.length() > 0;
    }
}
