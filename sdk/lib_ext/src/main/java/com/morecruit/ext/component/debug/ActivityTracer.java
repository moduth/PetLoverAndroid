package com.morecruit.ext.component.debug;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.os.Build;
import android.os.Bundle;

import com.morecruit.ext.utils.ReflectUtils;
import com.morecruit.ext.utils.Singleton;
import com.morecruit.ext.utils.ToastUtils;

import java.util.ArrayList;

/**
 * Tracer of Activity's lifecycle. This should be installed through {@link #install} before any usage.
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class ActivityTracer extends Tracer {

    // ~~~~~~~~~~~~~~~~~~~~ activity lifecycle ~~~~~~~~~~~~~~~~~~~~

    /**
     * Callback of Activity's lifecycle.
     */
    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity activity, Bundle savedInstanceState);

        void onActivityStarted(Activity activity);

        void onActivityResumed(Activity activity);

        void onActivityPaused(Activity activity);

        void onActivityStopped(Activity activity);

        void onActivitySaveInstanceState(Activity activity, Bundle outState);

        void onActivityDestroyed(Activity activity);
    }

    private volatile boolean mInstalled;

    // use Object instead of new Application.ActivityLifecycleCallbacks
    // to prevent api issue when initiate this class.
    private Object mApplicationCallbacks;

    private Instrumentation mTraceInstrumentation;
    private Instrumentation mOrigInstrumentation;

    private final ArrayList<ActivityLifecycleCallbacks> mActivityLifecycleCallbacks =
            new ArrayList<ActivityLifecycleCallbacks>();

    private ActivityTracer() {
    }

    /**
     * Install this activity tracer. <em>below Honeycomb</em>, installation will
     * replace the default {@link Instrumentation} for this application.
     *
     * @param application Application.
     * @return true if this tracer is successfully installed, false otherwise.
     */
    @SuppressLint("NewApi")
    public boolean install(Application application) {
        if (!mInstalled) {
            synchronized (this) {
                if (!mInstalled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        mInstalled = installWithApplication(application);
                    } else {
                        mInstalled = installWithInstrumentation(application);
                    }
                }
            }
        }
        return mInstalled;
    }

    /**
     * Uninstall this activity tracer. This api is hide for now.
     *
     * @param application Application.
     */
    @SuppressLint("NewApi")
    private void uninstall(Application application) {
        if (mInstalled) {
            synchronized (this) {
                if (mInstalled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        uninstallWithApplication(application);
                    } else {
                        uninstallWithInstrumentation(application);
                    }
                    mInstalled = false;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private boolean installWithApplication(Application application) {
        if (mApplicationCallbacks == null) {
            mApplicationCallbacks = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    dispatchActivityCreated(activity, savedInstanceState);
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    dispatchActivityStarted(activity);
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    dispatchActivityResumed(activity);
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    dispatchActivityPaused(activity);
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    dispatchActivityStopped(activity);
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    dispatchActivityDestroyed(activity);
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    dispatchActivitySaveInstanceState(activity, outState);
                }
            };
        }
        application.registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks) mApplicationCallbacks);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void uninstallWithApplication(Application application) {
        Application.ActivityLifecycleCallbacks callbacks = (Application.ActivityLifecycleCallbacks) mApplicationCallbacks;
        if (callbacks != null) {
            application.unregisterActivityLifecycleCallbacks(callbacks);
        }
    }

    private boolean installWithInstrumentation(Application application) {
        Object activityThread = ReflectUtils.invokeMethod("android.app.ActivityThread",
                "currentActivityThread", null, null);
        if (activityThread == null) {
            return false;
        }

        if (mOrigInstrumentation == null) {
            mOrigInstrumentation = (Instrumentation) ReflectUtils.getField(
                    "android.app.ActivityThread", "mInstrumentation", activityThread);
        }
        if (mOrigInstrumentation == null) {
            return false;
        }

        if (!Instrumentation.class.equals(mOrigInstrumentation.getClass())) {
            // instrumentation has been mocked.
            if (DebugConfig.isPackageDebuggable(application)) {
                ToastUtils.show(application, "Instrumentation has been mocked, activity tracer cannot replace it.");
            }
            return false;
        }

        if (mTraceInstrumentation == null) {
            mTraceInstrumentation = new Instrumentation() {
                @Override
                public void callActivityOnCreate(Activity activity, Bundle icicle) {
                    super.callActivityOnCreate(activity, icicle);
                    dispatchActivityCreated(activity, icicle);
                }

                @Override
                public void callActivityOnStart(Activity activity) {
                    super.callActivityOnStart(activity);
                    dispatchActivityStarted(activity);
                }

                @Override
                public void callActivityOnResume(Activity activity) {
                    super.callActivityOnResume(activity);
                    dispatchActivityResumed(activity);
                }

                @Override
                public void callActivityOnPause(Activity activity) {
                    super.callActivityOnPause(activity);
                    dispatchActivityPaused(activity);
                }

                @Override
                public void callActivityOnStop(Activity activity) {
                    super.callActivityOnStop(activity);
                    dispatchActivityStopped(activity);
                }

                @Override
                public void callActivityOnDestroy(Activity activity) {
                    super.callActivityOnDestroy(activity);
                    dispatchActivityDestroyed(activity);
                }

                @Override
                public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
                    super.callActivityOnSaveInstanceState(activity, outState);
                    dispatchActivitySaveInstanceState(activity, outState);
                }
            };
        }

        return ReflectUtils.setField("android.app.ActivityThread", "mInstrumentation",
                activityThread, mTraceInstrumentation);
    }

    private void uninstallWithInstrumentation(Application application) {
        if (mOrigInstrumentation != null) {
            Object activityThread = ReflectUtils.invokeMethod("android.app.ActivityThread",
                    "currentActivityThread", null, null);
            if (activityThread != null) {
                ReflectUtils.setField("android.app.ActivityThread", "mInstrumentation",
                        activityThread, mOrigInstrumentation);
            }
        }
    }

    /**
     * Register an activity lifecycle callback.
     *
     * @param callback Activity lifecycle callback.
     * @return true if callback is successfully registered, false otherwise.
     */
    public boolean registerActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        if (mInstalled) {
            synchronized (mActivityLifecycleCallbacks) {
                mActivityLifecycleCallbacks.add(callback);
            }
            return true;
        }
        return false;
    }

    /**
     * Unregister an activity lifecycle callback.
     *
     * @param callback Activity lifecycle callback previously registered.
     */
    public void unregisterActivityLifecycleCallbacks(ActivityLifecycleCallbacks callback) {
        synchronized (mActivityLifecycleCallbacks) {
            mActivityLifecycleCallbacks.remove(callback);
        }
    }

    private void dispatchActivityCreated(Activity activity, Bundle savedInstanceState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityCreated(activity,
                        savedInstanceState);
            }
        }
    }

    private void dispatchActivityStarted(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityStarted(activity);
            }
        }
    }

    private void dispatchActivityResumed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityResumed(activity);
            }
        }
    }

    private void dispatchActivityPaused(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityPaused(activity);
            }
        }
    }

    private void dispatchActivityStopped(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityStopped(activity);
            }
        }
    }

    private void dispatchActivitySaveInstanceState(Activity activity, Bundle outState) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivitySaveInstanceState(activity,
                        outState);
            }
        }
    }

    private void dispatchActivityDestroyed(Activity activity) {
        Object[] callbacks = collectActivityLifecycleCallbacks();
        if (callbacks != null) {
            for (int i = 0; i < callbacks.length; i++) {
                ((ActivityLifecycleCallbacks) callbacks[i]).onActivityDestroyed(activity);
            }
        }
    }

    private Object[] collectActivityLifecycleCallbacks() {
        Object[] callbacks = null;
        synchronized (mActivityLifecycleCallbacks) {
            if (mActivityLifecycleCallbacks.size() > 0) {
                callbacks = mActivityLifecycleCallbacks.toArray();
            }
        }
        return callbacks;
    }

    // --------------------- singleton ----------------------
    private final static Singleton<ActivityTracer, Void> INSTANCE = new Singleton<ActivityTracer, Void>() {
        @Override
        protected ActivityTracer create(Void aVoid) {
            return new ActivityTracer();
        }
    };

    /**
     * Get the singleton instance of {@link ActivityTracer}.
     *
     * @return Instance of {@link ActivityTracer}.
     */
    public static ActivityTracer getInstance() {
        return INSTANCE.get(null);
    }
}
