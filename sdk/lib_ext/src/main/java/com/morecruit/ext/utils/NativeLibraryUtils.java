package com.morecruit.ext.utils;

import android.annotation.SuppressLint;
import android.os.Build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.morecruit.ext.component.logger.Logger;

/**
 * Native库帮助工具类
 *
 * @author markzhai on 16/3/5
 */
public final class NativeLibraryUtils {

    private final static String TAG = "NativeLibraryUtils";

    private final static String APK_LIB = "lib/";
    private final static int APK_LIB_LEN = APK_LIB.length();

    private final static String LIB_PREFIX = "/lib";
    private final static int LIB_PREFIX_LEN = LIB_PREFIX.length();

    private final static String LIB_SUFFIX = ".so";
    private final static int LIB_SUFFIX_LEN = LIB_SUFFIX.length();

    private NativeLibraryUtils() {
        // static usage.
    }

    /**
     * Copies native binaries to a shared library directory.
     *
     * @param apkFile          APK file to scan for native libraries
     * @param sharedLibraryDir directory for libraries to be copied to
     * @return true if successful
     */
    public static boolean copyNativeBinariesIfNeeded(File apkFile, File sharedLibraryDir) {
        return copyNativeBinariesIfNeeded(apkFile.getPath(), sharedLibraryDir.getPath());
    }

    /**
     * Copies native binaries to a shared library directory.
     *
     * @param apkPath          APK file to scan for native libraries
     * @param sharedLibraryDir directory for libraries to be copied to
     * @return true if successful
     */
    @SuppressLint("InlinedApi")
    public static boolean copyNativeBinariesIfNeeded(String apkPath, String sharedLibraryDir) {
        final String cpuAbi = Build.CPU_ABI;
        final String cpuAbi2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Build.CPU_ABI2 : null;
        // cpu abi3 is useful on x86 (which always show compatibility for arm).
        final String cpuAbi3 = PropertyUtils.getQuickly("ro.product.cpu.upgradeabi", "armeabi");
        return copyNativeBinariesIfNeeded(apkPath, sharedLibraryDir, cpuAbi, cpuAbi2, cpuAbi3);
    }

    private static boolean copyNativeBinariesIfNeeded(String filePath, String sharedLibraryPath,
                                                      String cpuAbi, String cpuAbi2, String cpuAbi3) {
        final String dstDir = sharedLibraryPath;
        return iterateOverNativeBinaries(filePath, cpuAbi, cpuAbi2, cpuAbi3, new IterateHandler() {
            @Override
            public boolean handleEntry(InputStream is, ZipEntry ze, String name) {
                return copyFileIfChanged(is, ze, dstDir, name);
            }
        });
    }

    /**
     * Convenience method to call removeNativeBinaries(File)
     *
     * @param nativeLibraryPath Native library path.
     * @return Whether native binaries is removed.
     */
    public static boolean removeNativeBinaries(String nativeLibraryPath) {
        return removeNativeBinaries(new File(nativeLibraryPath));
    }

    /**
     * Remove the native binaries of a given package. This simply
     * gets rid of the files in the 'lib' sub-directory.
     *
     * @param nativeLibraryDir Native library directory.
     * @return Whether native binaries is removed.
     */
    public static boolean removeNativeBinaries(File nativeLibraryDir) {
        boolean deletedFiles = false;

        /**
         * Just remove any file in the directory. Since the directory is owned
         * by the 'system' UID, the application is not supposed to have written
         * anything there.
         */
        if (nativeLibraryDir.exists()) {
            final File[] binaries = nativeLibraryDir.listFiles();
            if (binaries != null) {
                for (int nn = 0; nn < binaries.length; nn++) {
                    if (!binaries[nn].delete()) {
                        Logger.w(TAG, "Could not delete native binary: " + binaries[nn].getPath());
                    } else {
                        deletedFiles = true;
                    }
                }
            }
            // Do not delete lib directory itself, or this will prevent
            // installation of future updates.
        }

        return deletedFiles;
    }

    private static boolean iterateOverNativeBinaries(String filePath, String cpuAbi, String cpuAbi2, String cpuAbi3, IterateHandler handler) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(filePath);

            boolean hasPrimaryAbi = false;
            boolean hasSecondaryAbi = false;
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                String fileName = ze.getName();
                // Make sure this entry has a filename.
                if (fileName == null) {
                    continue;
                }

                // Make sure we're in the lib directory of the ZIP.
                if (!fileName.startsWith(APK_LIB)) {
                    continue;
                }

                // Make sure the filename is at least to the minimum library name size.
                if (fileName.length() < APK_LIB_LEN + 2 + LIB_PREFIX_LEN + 1 + LIB_SUFFIX_LEN) {
                    continue;
                }

                // Make sure this entry is actually a .so file.
                String nameWithSlash = fileName.substring(fileName.lastIndexOf('/'));
                if (!(nameWithSlash.endsWith(LIB_SUFFIX) && nameWithSlash.startsWith(LIB_PREFIX))) {
                    continue;
                }

                // Check to make sure the CPU ABI of this file is one we support.
                if (fileName.regionMatches(APK_LIB_LEN, cpuAbi, 0, cpuAbi.length())
                        && fileName.charAt(APK_LIB_LEN + cpuAbi.length()) == '/') {
                    hasPrimaryAbi = true;
                } else if (cpuAbi2 != null // cpu abi2 may be null.
                        && fileName.regionMatches(APK_LIB_LEN, cpuAbi2, 0, cpuAbi2.length())
                        && fileName.charAt(APK_LIB_LEN + cpuAbi2.length()) == '/') {

                    hasSecondaryAbi = true;
                   /*
                    * If this library matches both the primary and secondary ABIs,
                    * only use the primary ABI.
                    */
                    if (hasPrimaryAbi) {
                        Logger.i(TAG, "Already saw primary ABI, skipping secondary ABI " + cpuAbi2);
                        continue;
                    } else {
                        Logger.i(TAG, "Using secondary ABI " + cpuAbi2);
                    }
                } else if (cpuAbi3 != null // cpu abi3 may be null.
                        && fileName.regionMatches(APK_LIB_LEN, cpuAbi3, 0, cpuAbi3.length())
                        && fileName.charAt(APK_LIB_LEN + cpuAbi3.length()) == '/') {

                    if (hasPrimaryAbi || hasSecondaryAbi) {
                        Logger.i(TAG, "Already saw primary or secondary ABI, skipping third ABI " + cpuAbi3);
                        continue;
                    } else {
                        Logger.i(TAG, "Using third ABI " + cpuAbi3);
                    }
                } else {
                    Logger.i(TAG, "abi didn't match anything entry " + fileName + ", ABI is " + cpuAbi + " ABI2 is " + cpuAbi2 + " ABI3 is " + cpuAbi3);
                    continue;
                }

                // If this is a .so file, check to see if we need to copy it.
                InputStream is = zf.getInputStream(ze);
                try {
                    if (!handler.handleEntry(is, ze, nameWithSlash.substring(1))) {
                        Logger.w(TAG, "Failure for handle match entry " + fileName);
                        return false;
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            }

        } catch (IOException e) {
            Logger.w(TAG, "Couldn't open APK " + filePath);
            return false;
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    // empty;
                }
            }
        }

        return true;
    }

    private static boolean copyFileIfChanged(InputStream is, ZipEntry ze, String dstDir, String dstName) {

        FileUtils.mkdirs(new File(dstDir));

        String dstPath = dstDir + File.separator + dstName;
        if (!isFileDifferent(dstPath, ze.getSize(), ze.getTime(), ze.getCrc())) {
            return true;
        }

        File dstFile = new File(dstPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(dstFile);

            byte[] buffer = new byte[1024];
            int numBytes;
            while ((numBytes = is.read(buffer)) > 0) {
                fos.write(buffer, 0, numBytes);
            }
        } catch (IOException e) {
            Logger.w(TAG, "Couldn't write dst file " + dstFile, e);
            FileUtils.delete(dstFile);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // empty.
                }
            }
        }

        // set the last modified time.
        if (!dstFile.setLastModified(ze.getTime())) {
            Logger.w(TAG, "Couldn't set time for dst file " + dstFile);
        }

        return true;
    }

    private static boolean isFileDifferent(String filePath, long fileSize, long modifiedTime, long zipCrc) {
        File file = new File(filePath);
        // length.
        if (file.length() != fileSize) {
            Logger.i(TAG, "file size doesn't match: " + file.length() + " vs " + fileSize);
            return true;
        }

        // modified time.
        if (file.lastModified() != modifiedTime) {
            Logger.i(TAG, "mod time doesn't match: " + file.lastModified() + " vs " + modifiedTime);
            return true;
        }

        // crc.
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);

            CRC32 crc32 = new CRC32();
            byte[] crcBuffer = new byte[8192];
            int numBytes;
            while ((numBytes = fis.read(crcBuffer)) > 0) {
                crc32.update(crcBuffer, 0, numBytes);
            }
            long crc = crc32.getValue();

            Logger.i(TAG, filePath + ": crc = " + crc + ", zipCrc = " + zipCrc);
            if (crc != zipCrc) {
                return true;
            }

        } catch (FileNotFoundException e) {
            Logger.w(TAG, "Couldn't open file " + filePath, e);
            return true;
        } catch (IOException e) {
            Logger.w(TAG, "Couldn't read file " + filePath, e);
            return true;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // empty.
                }
            }
        }

        return false;
    }

    private static interface IterateHandler {

        boolean handleEntry(InputStream is, ZipEntry entry, String name);
    }
}
