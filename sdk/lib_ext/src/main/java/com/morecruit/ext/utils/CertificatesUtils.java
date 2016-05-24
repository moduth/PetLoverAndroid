package com.morecruit.ext.utils;

import android.content.pm.Signature;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * APK文件的证书工具
 *
 * @author markzhai on 16/3/5
 * @version 1.0.0
 */
public final class CertificatesUtils {

    private final static String TAG = "Certificates";

    private final static boolean DEBUG_JAR = false;

    /**
     * File name in an APK for the Android manifest.
     */
    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    public final static String[] MANIFEST_ENTRY = new String[]{
            ANDROID_MANIFEST_FILENAME};
    /**
     * File name in an APK for the Android dex file.
     */
    private static final String ANDROID_DEX_FILENAME = "classes.dex";
    public final static String[] IMPORTANT_ENTRY = new String[]{
            ANDROID_MANIFEST_FILENAME, ANDROID_DEX_FILENAME};

    private static final Object mSync = new Object();
    private static WeakReference<byte[]> mReadBuffer;

    private CertificatesUtils() {
        // static usage.
    }

    /**
     * Collect signature of apk files.
     *
     * @param archivePath apk archive path.
     * @return signature.
     */
    public static Signature[] collectCertificates(String archivePath) {
        return collectCertificates(archivePath, false);
    }

    /**
     * Collect signature of apk files. The implementation is from android.content.pm.PackageParser.
     *
     * @param archivePath   apk archive path.
     * @param importantOnly whether only the important entry(manifest, dex .etc.) is considered.
     * @return signature.
     */
    public static Signature[] collectCertificates(String archivePath, boolean importantOnly) {
        return collectCertificates(archivePath, importantOnly ? IMPORTANT_ENTRY : null);
    }

    /**
     * Collect signature of apk files. The implementation is from android.content.pm.PackageParser.
     *
     * @param archivePath apk archive path.
     * @param entryNames  entry names which should be considered. null means all.
     * @return signature.
     */
    public static Signature[] collectCertificates(String archivePath, String... entryNames) {
        if (!isArchiveValid(archivePath)) {
            return null;
        }

        Signature[] signatures = null;

        WeakReference<byte[]> readBufferRef;
        byte[] readBuffer = null;
        synchronized (mSync) {
            readBufferRef = mReadBuffer;
            if (readBufferRef != null) {
                mReadBuffer = null;
                readBuffer = readBufferRef.get();
            }
            if (readBuffer == null) {
                readBuffer = new byte[8192];
                readBufferRef = new WeakReference<byte[]>(readBuffer);
            }
        }

        try {
            JarFile jarFile = new JarFile(archivePath);

            Certificate[] certs = null;

            Enumeration<JarEntry> entries = createJarEntries(jarFile, entryNames);
            while (entries.hasMoreElements()) {
                final JarEntry je = entries.nextElement();
                if (je == null) continue;
                if (je.isDirectory()) continue;

                final String name = je.getName();

                if (name.startsWith("META-INF/"))
                    continue;

                final Certificate[] localCerts = loadCertificates(jarFile, je, readBuffer);
                if (DEBUG_JAR) {
                    Log.i(TAG, "File " + archivePath + " entry " + je.getName()
                            + ": certs=" + certs + " ("
                            + (certs != null ? certs.length : 0) + ")");
                }

                if (localCerts == null) {
                    Log.e(TAG, "File " + archivePath
                            + " has no certificates at entry "
                            + je.getName() + "; ignoring!");
                    jarFile.close();
                    return null;
                } else if (certs == null) {
                    certs = localCerts;
                } else {
                    // Ensure all certificates match.
                    for (int i = 0; i < certs.length; i++) {
                        boolean found = false;
                        for (int j = 0; j < localCerts.length; j++) {
                            if (certs[i] != null &&
                                    certs[i].equals(localCerts[j])) {
                                found = true;
                                break;
                            }
                        }
                        if (!found || certs.length != localCerts.length) {
                            Log.e(TAG, "File " + archivePath
                                    + " has mismatched certificates at entry "
                                    + je.getName() + "; ignoring!");
                            jarFile.close();
                            return null;
                        }
                    }
                }
            }
            jarFile.close();

            synchronized (mSync) {
                mReadBuffer = readBufferRef;
            }

            if (certs != null && certs.length > 0) {
                final int N = certs.length;
                signatures = new Signature[certs.length];
                for (int i = 0; i < N; i++) {
                    signatures[i] = new Signature(
                            certs[i].getEncoded());
                }
            } else {
                Log.e(TAG, "File " + archivePath
                        + " has no certificates; ignoring!");
                return null;
            }
        } catch (CertificateEncodingException e) {
            Log.w(TAG, "Exception reading " + archivePath, e);
            return null;
        } catch (IOException e) {
            Log.w(TAG, "Exception reading " + archivePath, e);
            return null;
        } catch (RuntimeException e) {
            Log.w(TAG, "Exception reading " + archivePath, e);
            return null;
        }

        return signatures;
    }

    private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
                                                  byte[] readBuffer) {
        InputStream is = null;
        try {
            // We must read the stream for the JarEntry to retrieve
            // its certificates.
            is = new BufferedInputStream(jarFile.getInputStream(je));
            while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                // not using
            }
            return je != null ? je.getCertificates() : null;
        } catch (IOException e) {
            Log.w(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName(), e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Exception reading " + je.getName() + " in "
                    + jarFile.getName(), e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    Log.w(TAG, "Close IS Exception" + je.getName() + " in "
                            + jarFile.getName(), e);
                }
            }
        }
        return null;
    }

    private static boolean isArchiveValid(String archivePath) {
        if (archivePath == null || archivePath.length() == 0) {
            return false;
        }
        File file = new File(archivePath);
        return file.exists() && file.isFile();
    }

    private static Enumeration<JarEntry> createJarEntries(JarFile jarFile, String... entryNames) {
        if (entryNames == null || entryNames.length == 0) {
            return jarFile.entries();
        } else {
            return new JarFileEnumerator(jarFile, entryNames);
        }
    }

    static class JarFileEnumerator implements Enumeration<JarEntry> {

        private final JarFile jarFile;
        private final String[] entryNames;
        private int index = 0;

        public JarFileEnumerator(JarFile jarFile, String... entryNames) {
            this.jarFile = jarFile;
            this.entryNames = entryNames;
        }

        @Override
        public boolean hasMoreElements() {
            return index < entryNames.length;
        }

        @Override
        public JarEntry nextElement() {
            return jarFile.getJarEntry(entryNames[index++]);
        }
    }
}
