package com.morecruit.ext.component.debug;

import android.content.Context;

import com.morecruit.ext.utils.OomUtils;
import com.morecruit.ext.utils.ToastUtils;

/**
 * Tracer for exception. This is used to trace all exceptions reported via {@link #trace(Throwable)}.
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class ExceptionTracer {

    private final static String TAG = "ExceptionTracer";

    private volatile Context mContext;
    private volatile ExceptionInterceptor mExceptionInterceptor;

    private ExceptionTracer() {
    }

    /**
     * Install this exception manager, this should be called before everything.
     *
     * @param context application context.
     */
    public void install(Context context) {
        if (context == null) {
            return;
        }
        // assign only once.
        if (mContext != null) {
            return;
        }
        synchronized (this) {
            if (mContext != null) {
                return;
            }
            mContext = context.getApplicationContext();
        }
    }

    /**
     * Trace corresponding exception.
     *
     * @param e exception.
     */
    public void trace(Throwable e) {
        if (e == null) {
            return;
        }
        // hprof handle before everything.
        dumpHprofIfNeeded(e);

        // exception interceptor.
        final ExceptionInterceptor interceptor = mExceptionInterceptor;
        if (interceptor != null && interceptor.onInterceptException(e)) {
            // handled by interceptor.
            return;
        }

        // exception handling detail.
        if (e instanceof OutOfMemoryError) {
            handleOOM((OutOfMemoryError) e);
        }
    }

    /**
     * Set the exception interceptor.
     *
     * @param interceptor Exception interceptor.
     */
    public void setInterceptor(ExceptionInterceptor interceptor) {
        mExceptionInterceptor = interceptor;
    }

    // ------------ specific handle ------------
    private void handleOOM(OutOfMemoryError e) {
        // notify vm to gc.
        System.gc();
        System.gc();
    }

    // ------------ oom dump & notify -----------
    private void dumpHprofIfNeeded(Throwable e) {
        final Context context = mContext;
        if (context == null) {
            // no valid context.
            return;
        }
        if (!DebugConfig.isDebuggable(context)) {
            // dump only in debug mode.
            return;
        }
        if (OomUtils.dumpHprofIfNeeded(context, e)) {
            if (DebugConfig.isPackageDebuggable(context)) ToastUtils.show(context, "OOM occurs!!!");
        }
    }

    /**
     * Exception interceptor. This is used to intercept exception before handled by {@link ExceptionTracer}.
     */
    public interface ExceptionInterceptor {
        /**
         * Called when exception occurs.
         *
         * @param e exception.
         * @return true if this exception is handled, then {@link ExceptionTracer} will do nothing for this exception.
         */
        boolean onInterceptException(Throwable e);
    }

    // --------------- singleton ----------------
    static final class InstanceHolder {
        final static ExceptionTracer INSTANCE = new ExceptionTracer();
    }

    /**
     * Get the single instance of {@link ExceptionTracer}.
     */
    public static ExceptionTracer getInstance() {
        return InstanceHolder.INSTANCE;
    }
}
