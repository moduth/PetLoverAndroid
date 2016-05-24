package com.morecruit.ext.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.morecruit.ext.component.logger.Logger;

/**
 * APK文件的工具类
 *
 * @author markzhai on 16/3/5
 * @version 1.0.0
 */
public final class ApkUtils {

    private final static String TAG = "ApkUtils";

    private static Class<AssetManager> CLASS_ASSET;
    private static Method METHOD_ADD_ASSET;

    static {
        try {
            CLASS_ASSET = AssetManager.class;
            METHOD_ADD_ASSET = CLASS_ASSET.getDeclaredMethod("addAssetPath", String.class);

        } catch (NoSuchMethodException e) {
            Logger.w(TAG, e);
        } catch (Throwable e) {
            // for security consideration.
            Logger.w(TAG, e);
        }
    }

    private ApkUtils() {
        // static usage.
    }

    /**
     * Get the resources of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @return Resources of apk file.
     */
    public static Resources getResources(Context context, String apkPath) {
        if (!checkApkFile(apkPath)) {
            return null;
        }

        Resources resources = null;
        PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
        ApplicationInfo appInfo = pkgInfo == null ? null : pkgInfo.applicationInfo;
        if (appInfo != null) {
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;

            try {
                resources = context.getPackageManager().getResourcesForApplication(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                Logger.w(TAG, e);
            } catch (Throwable e) {
                Logger.w(TAG, e);
            }
        }

        return resources != null ? resources : getResourcesWithReflect(context, apkPath);
    }

    private static Resources getResourcesWithReflect(Context context, String apkPath) {
        if (CLASS_ASSET == null ||
                METHOD_ADD_ASSET == null) {
            return null;
        }

        if (!checkApkFile(apkPath)) {
            return null;
        }

        Resources resources = null;
        try {
            AssetManager asset = CLASS_ASSET.newInstance();
            Object[] args = new Object[]{apkPath};
            METHOD_ADD_ASSET.invoke(asset, args);

            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            Configuration config = context.getResources().getConfiguration();
            resources = new Resources(asset, dm, config);

        } catch (InvocationTargetException e) {
            Logger.w(TAG, e);
        } catch (InstantiationException e) {
            Logger.w(TAG, e);
        } catch (IllegalAccessException e) {
            Logger.w(TAG, e);
        } catch (Throwable e) {
            Logger.w(TAG, e);
        }
        return resources;
    }

    /**
     * Add extra path for resources.
     *
     * @param resources Resources to add extra path.
     * @param path      Extra path to add.
     * @return Whether extra path is successfully added.
     */
    public static boolean addResourcesExtra(Resources resources, String path) {
        if (resources == null) {
            return false;
        }
        if (!checkApkFile(path)) {
            return false;
        }
        if (METHOD_ADD_ASSET == null) {
            return false;
        }
        try {
            Object[] args = new Object[]{path};
            METHOD_ADD_ASSET.invoke(resources.getAssets(), args);
            return true;

        } catch (InvocationTargetException e) {
            Logger.w(TAG, e);
        } catch (IllegalAccessException e) {
            Logger.w(TAG, e);
        }
        return false;
    }

    /**
     * Get package info of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @return Package info.
     */
    public static PackageInfo getPackageInfo(Context context, String apkPath) {
        return getPackageInfo(context, apkPath, 0);
    }

    /**
     * Get package info of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @param flags   Additional option flags. Use any combination of
     *                {@link PackageManager#GET_ACTIVITIES},
     *                {@link PackageManager#GET_GIDS},
     *                {@link PackageManager#GET_CONFIGURATIONS},
     *                {@link PackageManager#GET_INSTRUMENTATION},
     *                {@link PackageManager#GET_PERMISSIONS},
     *                {@link PackageManager#GET_PROVIDERS},
     *                {@link PackageManager#GET_RECEIVERS},
     *                {@link PackageManager#GET_SERVICES},
     *                {@link PackageManager#GET_SIGNATURES}, to modify the data returned.
     * @return Package info.
     */
    public static PackageInfo getPackageInfo(Context context, String apkPath, int flags) {
        if (!checkApkFile(apkPath)) {
            return null;
        }

        PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, flags);
        if (pkgInfo == null) {
            return null;
        }
        if (((flags & PackageManager.GET_SIGNATURES) != 0) && (pkgInfo.signatures == null)) {
            // try to collect certificates with local algorithm if not available (because of low-level platform version or in-consistent signature).
            pkgInfo.signatures = CertificatesUtils.collectCertificates(apkPath);
        }
        return pkgInfo;
    }

    /**
     * Get application info of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @return Application info.
     */
    public static ApplicationInfo getApplicationInfo(Context context, String apkPath) {
        return getApplicationInfo(context, apkPath, 0);
    }

    /**
     * Get application info of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @param flags   Additional option flags. Use any combination of
     *                {@link PackageManager#GET_ACTIVITIES},
     *                {@link PackageManager#GET_GIDS},
     *                {@link PackageManager#GET_CONFIGURATIONS},
     *                {@link PackageManager#GET_INSTRUMENTATION},
     *                {@link PackageManager#GET_PERMISSIONS},
     *                {@link PackageManager#GET_PROVIDERS},
     *                {@link PackageManager#GET_RECEIVERS},
     *                {@link PackageManager#GET_SERVICES},
     *                {@link PackageManager#GET_SIGNATURES}, to modify the data returned.
     * @return Application info.
     */
    public static ApplicationInfo getApplicationInfo(Context context, String apkPath, int flags) {
        if (!checkApkFile(apkPath)) {
            return null;
        }

        PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, flags);
        ApplicationInfo appInfo = pkgInfo == null ? null : pkgInfo.applicationInfo;
        if (appInfo != null) {
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
        }
        return appInfo;
    }

    /**
     * Get apk info of corresponding apk file.
     *
     * @param context Application context.
     * @param apkPath Apk file path.
     * @return Apk info.
     */
    public static ApkInfo getApkInfo(Context context, String apkPath) {
        if (!checkApkFile(apkPath)) {
            return null;
        }

        ApkInfo apkInfo = null;
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageArchiveInfo(apkPath, 0);
            Resources res = getResources(context, apkPath);
            if (pkgInfo != null && res != null) {
                ApplicationInfo appInfo = pkgInfo.applicationInfo;
                String name = appInfo == null ? null : getString(res, appInfo.labelRes);
                Drawable icon = appInfo == null ? null : getDrawable(res, appInfo.icon);
                if (icon == null && appInfo != null) {
                    // obtain the default app icon.
                    icon = context.getPackageManager().getApplicationIcon(appInfo);
                }

                apkInfo = new ApkInfo();
                apkInfo.packageInfo = pkgInfo;
                apkInfo.packageName = pkgInfo.packageName;
                apkInfo.name = name;
                apkInfo.icon = icon;
                apkInfo.version = pkgInfo.versionCode;
                apkInfo.versionName = pkgInfo.versionName;
            }

        } catch (Throwable e) {
            Logger.w(TAG, e);
        }

        return apkInfo;
    }

    private static Drawable getDrawable(Resources resources, int id) {
        try {
            return resources.getDrawable(id);
        } catch (Resources.NotFoundException e) {
            // do nothing.
        }
        return null;
    }

    private static String getString(Resources resources, int id) {
        try {
            return resources.getString(id);
        } catch (Resources.NotFoundException e) {
            // do nothing.
        }
        return null;
    }

    private static boolean checkApkFile(String apkPath) {
        if (apkPath == null || apkPath.length() == 0) {
            return false;
        }

        File apkFile = new File(apkPath);
        return !(!apkFile.exists() || !apkFile.isFile());
    }

    /**
     * Information of apk.
     */
    public static class ApkInfo {
        public PackageInfo packageInfo;

        public String packageName;

        public String name;

        public Drawable icon;

        public float version;

        public String versionName;
    }
}
