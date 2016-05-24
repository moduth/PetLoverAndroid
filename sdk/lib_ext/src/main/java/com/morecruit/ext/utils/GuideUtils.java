package com.morecruit.ext.utils;

import android.app.Activity;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.morecruit.ext.component.cache.sp.Once;

import java.lang.ref.WeakReference;

import com.morecruit.ext.component.logger.Logger;

/**
 * Guide utils which shows a mask on screen.
 *
 * @author markzhai on 16/3/5
 * @version 1.0.0
 */
public class GuideUtils {

    private static volatile WeakReference<PopupWindow> sRefWindow;
    private static volatile boolean sIsShowing;

    /**
     * 显示全屏蒙版
     */
    public static void showGuide(final Activity context, int drawableResourceId) {
        try {
            //如果已经被取消，就不再显示
            if (!sIsShowing) {
                return;
            }
            LinearLayout content = new LinearLayout(context);
            final PopupWindow window = new PopupWindow(context);
            window.setOutsideTouchable(true);
            window.setFocusable(true);
            window.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            window.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            window.setContentView(content);
            window.setBackgroundDrawable(context.getResources().getDrawable(drawableResourceId));
            window.showAtLocation(context.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
            window.setTouchInterceptor((v, event) -> {
                window.dismiss();
                return true;
            });
            window.setOnDismissListener(() -> {
                sIsShowing = false;
                sRefWindow.clear();
            });
            sRefWindow = new WeakReference<>(window);
        } catch (Exception e) {
            //ignore
        }
    }

    /**
     * 只会显示一次的全屏蒙版，蒙版以键值来区分
     *
     * @param context            activity context
     * @param drawableResourceId 要显示的蒙版的resourceId
     * @param keyName            用来标识蒙版的唯一名称
     */
    public static void showGuideOnce(final Activity context,
                                     final int drawableResourceId,
                                     final String keyName) {
        new Thread(() -> {
            try {
                if (!Once.beenDone(Once.THIS_APP_VERSION, keyName)) {
                    sIsShowing = true;
                    SystemClock.sleep(500);//延时保护
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showGuide(context, drawableResourceId);
                                Once.markDone(keyName);
                            } catch (Exception ex) {
                                Logger.e("showVeilPictureOnce " + keyName, ex.getMessage(), ex);
                            }
                        }
                    });
                }
            } catch (Exception ex) {
                Logger.e("showVeilPictureOnce " + keyName, ex.getMessage(), ex);
            }

        }).start();
    }
}
