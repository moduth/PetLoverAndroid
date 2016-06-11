package com.github.moduth.petlover;

import android.app.Application;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;

import com.github.moduth.petlover.callback.IGlobalEventHandler;
import com.github.moduth.petlover.callback.IGlobalEventNotifier;
import com.github.moduth.petlover.event.BaseEvent;
import com.github.moduth.petlover.internal.di.components.ApplicationComponent;
import com.github.moduth.petlover.internal.di.components.DaggerApplicationComponent;
import com.github.moduth.petlover.internal.di.modules.ApplicationModule;
import com.morecruit.ext.Ext;
import com.morecruit.ext.component.info.Device;
import com.morecruit.ext.component.info.Network;
import com.morecruit.ext.component.logger.Logger;
import com.morecruit.ext.utils.ViewUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class PLApplication extends Application {
    private final static  String TAG = "PLApplication";

    private ApplicationComponent mApplicationComponent;


    @Override
    public void onCreate() {
        super.onCreate();
        initInjector();
        initExtension();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }

    private void initInjector() {
        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initExtension() {
        Ext.init(this, new ExtImpl());
    }

    static final class ExtImpl extends Ext {

        @Override
        public String getCurOpenId() {
            //TODO get UserId from ApplicationComponent
            return "";
        }

        @Override
        public String getDeviceInfo() {
            return Device.getInfo();
        }

        @Override
        public String getPackageNameForResource() {
            return "com.github.moduth.petlover";
        }

        @Override
        public int getScreenHeight() {
            return ViewUtils.getScreenHeight();
        }

        @Override
        public int getScreenWidth() {
            return ViewUtils.getScreenWidth();
        }

        @Override
        public boolean isAvailable() {
            return Network.isAvailable();
        }

        @Override
        public boolean isWap() {
            return Network.isWap();
        }

        @Override
        public boolean isMobile() {
            return Network.isMobile();
        }

        @Override
        public boolean is2G() {
            return Network.is2G();
        }

        @Override
        public boolean is3G() {
            return Network.is3G();
        }

        @Override
        public boolean isWifi() {
            return Network.isWifi();
        }

        @Override
        public boolean isEthernet() {
            return Network.isEthernet();
        }

        @Override
        public boolean fontInterceptorOnInterceptSetTextSize(View view, float textSize) {
            if (view instanceof TextView) {
                ((TextView) view).setTextSize(textSize);
                return true;
            }
            return false;
        }

        @Override
        public void showAlertDialog(Context context, String title, String message,
                                    String positive, DialogInterface.OnClickListener positiveListener,
                                    String negative, DialogInterface.OnClickListener negativeListener) {

        }

        public static Status status = new Status();

        /**
         * 全局事件处理器
         */
        public static EventHandler globalEventHandler = new EventHandler();

        /**
         * 应用状态和运行环境，包括前后台设置、锁屏判断、网络情况等
         */
        public static final class Status {
            private volatile boolean mIsBackground = true;  // 是否在后台，默认是在后台
            private volatile long mEnterForegroundTime;     // 记录进入前台的时间
            private volatile boolean networkCutBySystem;

            public static boolean isLockScreen() {
                KeyguardManager keyguardManager = (KeyguardManager) getContext()
                        .getSystemService(Context.KEYGUARD_SERVICE);
                return keyguardManager.inKeyguardRestrictedInputMode();
            }

            private void setBackground(boolean background, long enterForegroundTime) {
                mIsBackground = background;
                mEnterForegroundTime = enterForegroundTime;
            }

            public boolean isBackground() {
                return mIsBackground;
            }

            public boolean isForegroundInTime(int seconds) {
                return !mIsBackground && (System.currentTimeMillis() - mEnterForegroundTime > seconds * 1000);
            }
        }

        public static final class EventHandler implements IGlobalEventHandler {

            // 应用进入后台
            public void onEnterBackground() {

            }

            // 应用进入前台
            public void onEnterForeground() {

            }

            // 屏幕电量
            @Override
            public void onScreenOn() {

            }

            // 屏幕变暗
            @Override
            public void onScreenOff() {

            }

            // usb连接
            @Override
            public void onUSBConnected() {

            }

            // usb断开
            @Override
            public void onUSBDisconnected() {

            }

            // application create
            @Override
            public void onCreate(Context context) {

            }

            // application terminate
            @Override
            public void onTerminate(Context context) {

            }

            // 低内存
            @Override
            public void onLowMemory() {

            }

            // 登入
            @Override
            public void onLogin(long uin) {

            }

            // 登出
            @Override
            public void onLogout() {

            }

            // 清除数据
            @Override
            public void onClearData() {

            }

            // 手机启动
            @Override
            public void onPhoneBoot(Context context, Intent intent) {
                //String action = intent.getAction();
                //if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                //}
            }

            // 全局事件通知（
            public void onEvent(BaseEvent event) {

            }

            // 全局事件通知（主线程）
            public void onEventMainThread(BaseEvent event) {

            }
        }

        public static final class EventNotifier implements IGlobalEventNotifier {

            public static class BootReceiver extends BroadcastReceiver {

                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        if (context != null && intent != null) {
                            globalEventHandler.onPhoneBoot(context, intent);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        String action = intent.getAction();
                        if (action != null && action.equals(Intent.ACTION_SCREEN_OFF)) {
                            Logger.i(TAG, "SCREEN OFF, isBackGround:" + status.isBackground());
                            if (!status.isBackground()) {// 屏幕关掉,如果此时不是在后台则通知wns进入了后台运行模式
                                enterBackground();
                            }
                            globalEventHandler.onScreenOff();
                        } else if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
                            Logger.i(TAG, "SCREEN ON, isBackGround:" + status.isBackground());
                            if (!status.isBackground()) {// 屏幕点亮，此时如果不在后台则恢复前台运行
                                enterForeground();
                            }
                            globalEventHandler.onScreenOn();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };

            private final BroadcastReceiver mUMSReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        String action = intent.getAction();
                        if (action != null && Intent.ACTION_UMS_CONNECTED.equals(action)) {
                            globalEventHandler.onUSBConnected();
                        } else if (action != null && Intent.ACTION_UMS_DISCONNECTED.equals(action)) {
                            globalEventHandler.onUSBDisconnected();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };

            private final BroadcastReceiver mPowerSaverReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    status.networkCutBySystem = isNetworkCutBySystem();
                }
            };

            private boolean isNetworkCutBySystem() {
                PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                try {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH && pm.isPowerSaveMode()) {
                        if (status.isBackground()) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }


            public void create(Context context) {
                // Screen receiver
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                context.registerReceiver(mScreenReceiver, intentFilter);

                // USB receiver
                IntentFilter filterUMS = new IntentFilter();
                filterUMS.addAction(Intent.ACTION_UMS_CONNECTED);
                filterUMS.addAction(Intent.ACTION_UMS_DISCONNECTED);
                context.registerReceiver(mUMSReceiver, filterUMS);

                // Power Saver receiver
                IntentFilter filterPS = new IntentFilter();
                filterPS.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
                context.registerReceiver(mPowerSaverReceiver, filterPS);

                EventBus.getDefault().register(globalEventHandler);
                globalEventHandler.onCreate(context);
            }

            public void terminate(Context context) {
                context.unregisterReceiver(mScreenReceiver);

                EventBus.getDefault().unregister(globalEventHandler);
                globalEventHandler.onLogout();
                globalEventHandler.onTerminate(context);
            }

            public void lowMemory() {
                globalEventHandler.onLowMemory();
            }

            public void enterForeground() {
                status.setBackground(false, System.currentTimeMillis());
                globalEventHandler.onEnterForeground();
            }

            public void enterBackground() {
                status.setBackground(true, 0);
                status.networkCutBySystem = isNetworkCutBySystem();

                globalEventHandler.onEnterBackground();
            }

            public void login(long uin) {
                globalEventHandler.onLogin(uin);
            }

            public void logout() {
                globalEventHandler.onLogout();
            }

            public void clearData() {
                globalEventHandler.onClearData();
            }

            public void shout(Object event) {
                EventBus.getDefault().post(event);
            }
        }
    }
}
