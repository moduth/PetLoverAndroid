package com.morecruit.ext.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.FileReader;

import com.morecruit.ext.component.logger.Logger;

/**
 * 内存工具类
 *
 * @author markzhai on 16/3/5
 */
public final class MemoryUtils {

    private final static String TAG = "MemoryUtils";

    private static volatile Long sSystemTotalMemory;

    private MemoryUtils() {
        // static usage.
    }

    /**
     * Get total memory of whole system.
     *
     * @param context Application context.
     * @return Total memory of whole system.
     */
    public static long getSystemTotalMemory(Context context) {
        if (sSystemTotalMemory != null) {
            return sSystemTotalMemory;
        }
        synchronized (MemoryUtils.class) {
            if (sSystemTotalMemory != null) {
                return sSystemTotalMemory;
            }
            return sSystemTotalMemory = obtainSystemTotalMemory(context);
        }
    }

    /**
     * Get available memory of whole system.
     *
     * @param context Application context.
     * @return Available memory of whole system.
     */
    public static long getSystemAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    private static long obtainSystemTotalMemory(Context context) {
        long totalMem = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            totalMem = mi.totalMem;

        } else {
            String memPath = "/proc/meminfo";
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(memPath), 8192);
                String memLine = reader.readLine();
                if (memLine != null) {
                    String[] split = memLine.split("\\s+");
                    totalMem = Integer.valueOf(split[1]).intValue() * 1024;
                }

            } catch (Throwable e) {
                Logger.i(TAG, "fail to obtain system total memory", e);
            } finally {
                IoUtils.closeSilently(reader);
            }
        }
        return totalMem;
    }

    /**
     * Get current memory stats.
     *
     * @param context Application context.
     * @return Current memory stats.
     */
    @SuppressLint("NewApi")
    public static String getMemoryStats(Context context) {
        StringBuilder text = new StringBuilder();
        try {
            text.append("\ntotalMemory()=").append(toMib(context, Runtime.getRuntime().totalMemory()));
            text.append("\nmaxMemory()=").append(toMib(context, Runtime.getRuntime().maxMemory()));
            text.append("\nfreeMemory()=").append(toMib(context, Runtime.getRuntime().freeMemory()));

            Debug.MemoryInfo memInfo = new Debug.MemoryInfo();
            Debug.getMemoryInfo(memInfo);
            text.append("\ndalvikPrivateDirty=").append(toMib(memInfo.dalvikPrivateDirty));
            text.append("\ndalvikPss=").append(toMib(memInfo.dalvikPss));
            text.append("\ndalvikSharedDirty=").append(toMib(memInfo.dalvikSharedDirty));
            text.append("\nnativePrivateDirty=").append(toMib(memInfo.nativePrivateDirty));
            text.append("\nnativePss=").append(toMib(memInfo.nativePss));
            text.append("\nnativeSharedDirty=").append(toMib(memInfo.nativeSharedDirty));
            text.append("\notherPrivateDirty=").append(toMib(memInfo.otherPrivateDirty));
            text.append("\notherPss").append(toMib(memInfo.otherPss));
            text.append("\notherSharedDirty=").append(toMib(memInfo.otherSharedDirty));

            text.append("\ntotalPrivateDirty=").append(toMib(memInfo.getTotalPrivateDirty()));
            text.append("\ntotalPss=").append(toMib(memInfo.getTotalPss()));
            text.append("\ntotalSharedDirty=").append(toMib(memInfo.getTotalSharedDirty()));
        } catch (Throwable e) {
            Logger.d(TAG, "fail to get memory stats", e);
        }
        return text.toString();
    }

    /**
     * Log current memory stats.
     *
     * @param context Application context.
     */
    public static void logMemoryStats(Context context) {
        String text = getMemoryStats(context);
        Logger.i(TAG, text);
    }

    private static String toMib(int size) {
        return String.format("%.2fMB", size / 1024.0);
    }

    private static String toMib(Context context, long nativeHeapSize) {
        return Formatter.formatFileSize(context, nativeHeapSize);
    }
}

