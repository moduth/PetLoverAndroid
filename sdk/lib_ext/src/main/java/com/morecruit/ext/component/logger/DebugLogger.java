package com.morecruit.ext.component.logger;

import com.morecruit.ext.utils.Singleton;

import static com.morecruit.ext.component.logger.LogConstants.TAG;

/**
 * Debug-mode com.morecruit.ext.component.logger. e/w/d write file and logcat，v/d only logcat
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
public class DebugLogger implements ILog {

    private DebugLogger() {
    }

    private static Singleton<DebugLogger, Void> sSingleton = new Singleton<DebugLogger, Void>() {
        @Override
        protected DebugLogger create(Void aVoid) {
            return new DebugLogger();
        }
    };

    public static DebugLogger getInstance() {
        return sSingleton.get(null);
    }

    @Override
    public void v(String tag, String text) {
        Log.v(tag, text);
    }

    @Override
    public void v(String text) {
        Log.v(TAG, text);
    }

    @Override
    public void d(String tag, String text) {
        Log.d(tag, text);
    }

    @Override
    public void d(String text) {
        Log.d(TAG, text);
    }

    @Override
    public void i(String tag, String text) {
        Log.i(tag, text);
        Log4j.i(tag, text);
    }

    @Override
    public void i(String text) {
        Log.i(TAG, text);
        Log4j.i(text);
    }

    @Override
    public void w(String tag, String text) {
        Log.w(tag, text);
        Log4j.w(tag, text);
    }

    @Override
    public void w(String text) {
        Log.w(TAG, text);
        Log4j.w(text);
    }

    @Override
    public void e(String tag, String text) {
        Log.e(tag, text);
        Log4j.e(tag, text);
    }

    @Override
    public void e(Exception e) {
        Log.e(TAG, e);
        Log4j.e(e);
    }

    @Override
    public void e(String text) {
        Log.e(TAG, text);
        Log4j.e(text);
    }
}