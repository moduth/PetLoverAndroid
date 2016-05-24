package com.morecruit.ext.component.info;

import android.support.annotation.RequiresPermission;

import com.morecruit.ext.Ext;
import com.morecruit.ext.utils.NetWorkUtils;

/**
 * 网络信息，包含了通用信息(Network.)、
 * 代理信息(Network.Proxy.)、
 * Wifi信息{@link Network.Wifi}
 * DNS信息
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
public class Network extends NetworkDash {
    /**
     * 系统代理信息
     */
    public static abstract class Proxy {
        public static int getPort() {
            return android.net.Proxy.getDefaultPort();
        }

        public static String getHost() {
            return android.net.Proxy.getDefaultHost();
        }
    }

    /**
     * WIFI网卡信息
     */
    public static class Wifi extends WifiDash {

    }

    public static class Dns {
        @RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
        public static NetWorkUtils.DNS getDNS() {
            return NetWorkUtils.getDNS(Ext.getContext());
        }
    }
}