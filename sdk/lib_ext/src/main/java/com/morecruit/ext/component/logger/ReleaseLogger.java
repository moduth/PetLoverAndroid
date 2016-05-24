package com.morecruit.ext.component.logger;

import com.morecruit.ext.utils.Singleton;

import static com.morecruit.ext.component.logger.LogConstants.TAG;

/**
 * release-mode com.morecruit.ext.component.logger, will hide v/d/i logcat output
 * <p/>
 * Created by zhaiyifan on 2015/7/31.
 */
public class ReleaseLogger implements ILog {

    private ReleaseLogger() {
    }

    private static Singleton<ReleaseLogger, Void> sSingleton = new Singleton<ReleaseLogger, Void>() {
        @Override
        protected ReleaseLogger create(Void aVoid) {
            return new ReleaseLogger();
        }
    };

    public static ReleaseLogger getInstance() {
        return sSingleton.get(null);
    }

    @Override
    public void v(String tag, String text) {
        Log4j.v(tag, text);
    }

    @Override
    public void v(String text) {
        Log4j.v(TAG, text);
    }

    @Override
    public void d(String tag, String text) {
        Log4j.d(tag, text);
    }

    @Override
    public void d(String text) {
        Log4j.d(TAG, text);
    }

    @Override
    public void i(String tag, String text) {
        Log4j.i(tag, text);
    }

    @Override
    public void i(String text) {
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