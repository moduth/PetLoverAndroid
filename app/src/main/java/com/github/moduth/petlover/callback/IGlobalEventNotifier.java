package com.github.moduth.petlover.callback;

import android.content.Context;

/**
 * 全局事件通知器接口
 * <p/>
 * Created by zhaiyifan on 2015/8/6.
 */
public interface IGlobalEventNotifier {
    void create(Context context);

    void terminate(Context context);

    void lowMemory();

    void enterForeground();

    void enterBackground();

    void login(long uin);

    void logout();

    void clearData();

    //void shout(int what, Object...
    void shout(Object event);
}
