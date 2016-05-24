package com.morecruit.ext.component.info;

/**
 * 设备信息获取类
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
public class Device extends DeviceDash {
    /**
     * 获得设备信息字符串，形如"imei=xxxxxxx&model=xxxxx&……"<br>
     * <br>
     * 即原WNS的DeviceInfo，供业务层调取
     *
     * @return 设备信息
     */
    public static String getInfo() {
        return getInstance().getDeviceInfo();
    }

    /**
     * 存储器信息
     *
     * @author lewistian
     */
    public static class Storage extends StorageDash {

    }
}