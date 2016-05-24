package com.morecruit.ext.utils;

/**
 * Singleton abstract class
 * <p/>
 * Created by zhaiyifan on 2015/7/27.
 */
public abstract class Singleton<T, P> {
    private volatile T mInstance;

    protected abstract T create(P p);

    public final T get(P p) {
        if (mInstance == null) {
            synchronized (this) {
                if (mInstance == null) {
                    mInstance = create(p);
                }
            }
        }
        return mInstance;
    }
}

