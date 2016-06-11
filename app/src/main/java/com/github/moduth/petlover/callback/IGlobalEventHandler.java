package com.github.moduth.petlover.callback;

import android.content.Context;
import android.content.Intent;

/**
 * 全局事件处理器接口
 * <p/>
 * Created by zhaiyifan on 2015/8/6.
 */
public interface IGlobalEventHandler {
    void onEnterForeground();

    void onEnterBackground();

    void onScreenOn();

    void onScreenOff();

    void onUSBConnected();

    void onUSBDisconnected();

    void onCreate(final Context context);

    void onTerminate(final Context context);

    void onLowMemory();

    void onLogin(long uin);

    void onLogout();

    void onClearData();

    void onPhoneBoot(Context context, Intent intent);
}