package com.morecruit.ext.component.info;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.morecruit.ext.Ext;

/**
 * 设备信息获取类
 * <p>
 * Created by zhaiyifan on 2015/7/31.
 */
public class DeviceDash implements NetworkStateListener {
    private static final DeviceDash instance = new DeviceDash();
    private static final String NOT_AVAILABLE = "N/A";

    public static DeviceDash getInstance() {
        return instance;
    }

    private String mDeviceInfo = null;

    public DeviceDash() {
        NetworkDash.addListener(this);
    }

    public String getDeviceInfo() {
        if (mDeviceInfo == null || mDeviceInfo.length() < 1) {
            return updateDeviceInfo();
        }

        return mDeviceInfo;
    }

    public String updateDeviceInfo() {

        StringBuilder builder = new StringBuilder();
        {

            String apn;
            NetworkState state = NetworkDash.getCurrState();

            switch (state.getType()) {
                case MOBILE_3G: {
                    apn = "3g";
                }
                break;

                case MOBILE_2G: {
                    apn = "2g";
                }
                break;

                case MOBILE_4G: {
                    apn = "4g";
                }
                break;

                case WIFI: {
                    apn = "wifi";
                }
                break;

                case ETHERNET: {
                    apn = "ethernet";
                }
                break;

                default: {
                    apn = "wan";
                }
            }


            String isp = "";
            switch (NetworkDash.getProvider(true)) {
                case NONE:
                    isp = "0";
                    break;
                case CHINA_MOBILE:
                    isp = "1";
                    break;
                case CHINA_UNICOM:
                    isp = "2";
                    break;
                case CHINA_TELECOM:
                    isp = "3";
                    break;
                case NEVER_HEARD:
                    isp = "4";
                    break;
            }
            builder.append("imei=").append(getDeviceId()).append('&');

            builder.append("model=").append(android.os.Build.MODEL).append('&');
            builder.append("os=").append(android.os.Build.VERSION.RELEASE).append('&');
            builder.append("isp=").append(isp).append('&');
            builder.append("apilevel=").append(android.os.Build.VERSION.SDK_INT).append('&');

            builder.append("network=").append(apn).append('&');
            builder.append("sdcard=").append(StorageDash.hasExternal() ? 1 : 0).append('&');
            builder.append("sddouble=").append("0").append('&');
            builder.append("display=").append(getScreenSize()).append('&');
            builder.append("manu=").append(android.os.Build.MANUFACTURER).append('&');
            builder.append("wifi=").append(WifiDash.getWifiInfo()).append('&');
            builder.append("storage=").append(getStorageInfo()).append('&');
            builder.append("cell=").append(NetworkDash.getCellLevel()).append('&');
        }

        mDeviceInfo = builder.toString();

        return mDeviceInfo;
    }

    private String getStorageInfo() {
        StorageInfo innerInfo = StorageDash.getInnerInfo();
        StorageInfo extInfo = StorageDash.getExternalInfo();

        return String.format("{IN : %s |EXT: %s}", (innerInfo == null) ? "N/A" : innerInfo.toString(),
                (extInfo == null) ? "N/A" : extInfo.toString());
    }

    private static String getDeviceId() {
        String result;

        try {
            TelephonyManager mTelephonyMgr = (TelephonyManager) Ext.getContext().getSystemService(Context.TELEPHONY_SERVICE);

            result = mTelephonyMgr.getDeviceId();
        } catch (Exception e) {
            result = NOT_AVAILABLE;
        }

        return result;
    }

    private static String getScreenSize() {
        String result;

        WindowManager manager = (WindowManager) Ext.getContext().getSystemService(Context.WINDOW_SERVICE);

        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();

            manager.getDefaultDisplay().getMetrics(displayMetrics);

            result = "" + displayMetrics.widthPixels + '*' + displayMetrics.heightPixels;
        } catch (Exception e) {
            result = NOT_AVAILABLE;
        }

        return result;
    }

    @Override
    public void onNetworkStateChanged(NetworkState lastState, NetworkState newState) {
        // 网络变动时刷新设备信息
        updateDeviceInfo();
    }

    /**
     * cpu架构类型
     */
    public enum CpuArch {
        ARM("armeabi"), X86("x86"), MIPS("mips"), ARM_V7A("armeabi-v7a");
        private String type;

        CpuArch(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     * 获取手机主cpu架构 {@link android.os.Build#CPU_ABI}
     *
     * @see CpuArch
     */
    public static CpuArch getCpuBuild() {
        return getCpuArchByAbi(android.os.Build.CPU_ABI);
    }

    /**
     * 获取手机次cpu架构 {@link android.os.Build#CPU_ABI2}
     *
     * @see CpuArch
     */
    public static CpuArch getSecondaryCpuBuild() {
        return getCpuArchByAbi(android.os.Build.CPU_ABI2);
    }

    public static CpuArch getCpuArchByAbi(String platform) {
        if (platform == null) {
            return CpuArch.ARM;
        }
        if (platform.contains("x86")) {
            return CpuArch.X86;
        } else if (platform.contains("mips")) {
            return CpuArch.MIPS;
        } else if (platform.equalsIgnoreCase("armeabi")) {
            return CpuArch.ARM;
        } else if (platform.equalsIgnoreCase("armeabi-v7a")) {
            return CpuArch.ARM_V7A;
        } else {
            return CpuArch.ARM;
        }
    }
}
