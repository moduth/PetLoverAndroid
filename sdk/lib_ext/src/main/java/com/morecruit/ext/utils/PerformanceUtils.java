package com.morecruit.ext.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import com.morecruit.ext.component.logger.Logger;

/**
 * 获取手机性能/硬件性息
 * <p/>
 * Created by zhaiyifan on 2015/7/22.
 */
public class PerformanceUtils {
    private static final String TAG = "PerformanceUtils";

    private static int sCoreNum = 0;
    private static long sTotalMemo = 0;

    /**
     * 获取cpu核心数
     */
    public static int getCpuCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                return Pattern.matches("cpu[0-9]", pathname.getName());
            }
        }

        if (sCoreNum == 0) {
            try {
                // Get directory containing CPU info
                File dir = new File("/sys/devices/system/cpu/");
                // Filter to only list the devices we care about
                File[] files = dir.listFiles(new CpuFilter());
                // Return the number of cores (virtual CPU devices)
                sCoreNum = files.length;
            } catch (Exception e) {
                Logger.e(TAG, "getCpuCores exception occurred, e=", e);
                sCoreNum = 1;
            }
        }
        return sCoreNum;

    }

    /**
     * 获取android当前可用内存大小
     */
    public static long getFreeMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem;
    }

    /**
     * 获取android机器的总内存大小
     */
    public static long getTotalMemory() {
        if (sTotalMemo == 0) {
            // 系统内存信息文件
            String str1 = "/proc/meminfo";
            String str2;
            String[] arrayOfString;
            long initial_memory = -1;
            FileReader localFileReader = null;
            try {
                localFileReader = new FileReader(str1);
                BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
                str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小

                if (str2 != null) {
                    arrayOfString = str2.split("\\s+");
                    initial_memory = Integer.valueOf(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
                }
                localBufferedReader.close();
            } catch (IOException e) {
                Logger.e(TAG, "getTotalMemory Exception occurred, e =", e);
            } finally {
                if (localFileReader != null) {
                    try {
                        localFileReader.close();
                    } catch (IOException e) {
                        Logger.e(TAG, "close localFileReader Exception occurred, e = ", e);
                    }
                }
            }
            sTotalMemo = initial_memory;// Byte转换为KB或者MB，内存大小规格化
        }
        return sTotalMemo;
    }
}
