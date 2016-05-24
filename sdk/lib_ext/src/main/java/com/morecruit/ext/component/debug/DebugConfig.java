package com.morecruit.ext.component.debug;

import android.content.Context;
import android.content.pm.ApplicationInfo;

/**
 * <p>Configuration for debug.</p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class DebugConfig {

    private static boolean sRuntimeDebuggable = false;
    private static volatile Boolean sPackageDebuggable = null;

    private DebugConfig() {
        // static usage.
    }

    /**
     * Whether <em>RUNTIME</em> or <em>PACKAGE</em> is debuggable.
     *
     * @param context package context.
     * @return true if runtime or package is debuggable.
     */
    public static boolean isDebuggable(Context context) {
        return isRuntimeDebuggable() || isPackageDebuggable(context);
    }

    /**
     * Whether <em>RUNTIME</em> is debuggable.
     *
     * @return true if runtime is debuggable.
     */
    public static boolean isRuntimeDebuggable() {
        return sRuntimeDebuggable;
    }

    /**
     * Whether this <em>PACKAGE</em> is debuggable.
     *
     * @param context package context.
     * @return true if this package is debuggable.
     */
    public static boolean isPackageDebuggable(Context context) {
        if (sPackageDebuggable != null) {
            return sPackageDebuggable;
        }
        synchronized (DebugConfig.class) {
            if (sPackageDebuggable != null) {
                return sPackageDebuggable;
            }
            ApplicationInfo appInfo = context != null ? context.getApplicationInfo() : null;
            if (appInfo == null) {
                return false;
            }
            return sPackageDebuggable = ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
        }
    }

    /**
     * Set whether <em>RUNTIME</em> is debuggable.
     *
     * @param debuggable whether runtime is debuggable.
     */
    public static void setRuntimeDebuggable(boolean debuggable) {
        sRuntimeDebuggable = debuggable;
    }
}
