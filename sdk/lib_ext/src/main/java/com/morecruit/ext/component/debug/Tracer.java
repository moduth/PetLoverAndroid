package com.morecruit.ext.component.debug;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.morecruit.ext.utils.Singleton;
import com.morecruit.ext.utils.StorageUtils;

import java.io.File;

/**
 * Tracer抽象基类
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public abstract class Tracer {

    private final static String TRACE_ROOT_DIR = "debug";

    private final static Singleton<Looper, Void> sTracerLooper = new Singleton<Looper, Void>() {
        @Override
        protected Looper create(Void aVoid) {
            TracerThread t = new TracerThread("Tracer");
            t.start();
            return t.getLooper();
        }
    };

    private static Singleton<Handler, Void> sTracerHandler = new Singleton<Handler, Void>() {
        @Override
        protected Handler create(Void aVoid) {
            return new Handler(getTracerLooper());
        }
    };

    /**
     * Returns the trace root directory, which is used to store dump, log files .etc for various tracers.
     *
     * @param context Application context.
     * @return The trace root dir.
     */
    public static String getTraceRootDir(Context context) {
        return StorageUtils.getExternalCacheDir(context, TRACE_ROOT_DIR, true);
    }

    /**
     * Returns the trace directory for specific tracer.
     *
     * @param context Application context.
     * @param name    Diretory name for specific tracer.
     * @return The trace dir.
     */
    protected static String getTraceDir(Context context, String name) {
        return StorageUtils.getExternalCacheDir(context, TRACE_ROOT_DIR + File.separator + name, true);
    }

    /**
     * Returns the tracer looper.
     *
     * @return Tracer looper.
     */
    protected static Looper getTracerLooper() {
        return sTracerLooper.get(null);
    }

    /**
     * Returns the tracer handler associated with tracer looper..
     *
     * @return Tracer handler.
     */
    protected static Handler getTracerHandler() {
        return sTracerHandler.get(null);
    }

    /**
     * Determine whether current thread is tracer thread.
     *
     * @return Whether current thread is tracer thread.
     */
    protected static boolean isTracerThread() {
        return Thread.currentThread() == getTracerLooper().getThread();
    }

    /**
     * Run corresponding runnable on tracer thread.
     */
    protected static void runOnTracerThread(Runnable runnable) {
        if (isTracerThread()) {
            runnable.run();
        } else {
            getTracerHandler().post(runnable);
        }
    }

    /**
     * Throw if current thread is not tracer thread.
     */
    protected static void throwIfNotTracerThread() {
        if (!isTracerThread()) {
            throw new RuntimeException("This should be called only on trace thread, " +
                    "current thread is " + Thread.currentThread());
        }
    }

    final static class TracerThread extends HandlerThread {

        public TracerThread(String name) {
            super(name, android.os.Process.THREAD_PRIORITY_LOWEST);
        }
    }
}
