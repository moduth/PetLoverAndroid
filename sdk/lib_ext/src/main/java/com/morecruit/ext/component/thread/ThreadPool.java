package com.morecruit.ext.component.thread;

import android.annotation.TargetApi;
import android.os.Build;

import com.morecruit.ext.component.logger.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A convenient thread pool whose backend is {@link Executor}.
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public class ThreadPool {
    private static final String TAG = "ThreadPool";

    /**
     * Default thread priority. See constant defined in {@link android.os.Process}.
     */
    public static final int DEFAULT_THREAD_PRIORITY = android.os.Process.THREAD_PRIORITY_BACKGROUND;
    /**
     * Default pool size.
     */
    public static final int DEFAULT_POOL_SIZE = 4;

    // Resource type
    /**
     * None mode for {@link JobContext#setMode(int)}.
     */
    public static final int MODE_NONE = 0;
    /**
     * Cpu resource mode for {@link JobContext#setMode(int)}.
     */
    public static final int MODE_CPU = 1;
    /**
     * Network resource mode for {@link JobContext#setMode(int)}.
     */
    public static final int MODE_NETWORK = 2;

    public static final JobContext JOB_CONTEXT_STUB = new JobContextStub();

    ResourceCounter mCpuCounter = new ResourceCounter(2);
    ResourceCounter mNetworkCounter = new ResourceCounter(2);

    /**
     * A job for {@link ThreadPool}. It is like a Callable, but it has an addition JobContext parameter.
     *
     * @param <T>
     */
    public interface Job<T> {
        public T run(JobContext jc);
    }

    /**
     * Context for a job.
     */
    public interface JobContext {
        /**
         * Whether the job is cancelled.
         */
        boolean isCancelled();

        /**
         * Set the cancel listener for the job.
         *
         * @param listener Job cancel listener.
         */
        void setCancelListener(CancelListener listener);

        /**
         * Set the resource mode of the job.
         *
         * @param mode Resource mode if the job, see {@link #MODE_CPU}, {@link #MODE_NETWORK}, and {@link #MODE_NONE}.
         * @return Whether mode setting succeed.
         */
        boolean setMode(int mode);
    }

    private static class JobContextStub implements JobContext {
        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setCancelListener(CancelListener listener) {
        }

        @Override
        public boolean setMode(int mode) {
            return true;
        }
    }

    /**
     * Cancel listener for a job.
     */
    public interface CancelListener {
        public void onCancel();
    }

    private static class ResourceCounter {
        public int value;

        public ResourceCounter(int v) {
            value = v;
        }
    }

    private final ThreadPoolExecutor mExecutor;

    private final PriorityThreadFactory mThreadFactory;

    /**
     * Construct a ThreadPool with default parameter.
     */
    public ThreadPool() {
        this("thread-pool", DEFAULT_POOL_SIZE);
    }

    /**
     * Construct a ThreadPool with corresponding name and pool size.
     *
     * @param name     Name of this thread pool.
     * @param poolSize Pool size of this thread pool.
     */
    public ThreadPool(String name, int poolSize) {
        this(name, poolSize, poolSize, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * <p>Support in API level 9 and above.</p>
     * Construct a ThreadPool with corresponding name, pool size and keep alive time.
     *
     * @param name          Name of this thread pool.
     * @param poolSize      Pool size of this thread pool.
     * @param keepAliveTime this is the maximum time that idle threads will wait for new
     *                      tasks before terminating. The time unit is milliseconds.
     * @param unit          the time unit for the {@code keepAliveTime} argument
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public ThreadPool(String name, int poolSize, long keepAliveTime, TimeUnit unit) {
        this(name, poolSize, poolSize, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());
        if (keepAliveTime > 0) {
            mExecutor.allowCoreThreadTimeOut(true);
        }
    }

    /**
     * Construct a ThreadPool with corresponding name, core size, max size and queue for jobs.
     *
     * @param name     Name of this thread pool.
     * @param coreSize Core pool size of this thread pool.
     * @param maxSize  Maximum pool size of this thread pool.
     * @param queue    Blocking queue for jobs.
     */
    public ThreadPool(String name, int coreSize, int maxSize, BlockingQueue<Runnable> queue) {
        this(name, coreSize, maxSize, 10L, TimeUnit.SECONDS, queue);
    }

    /**
     * Construct a ThreadPool with corresponding name, core size, max size, keep alive time and queue for jobs.
     *
     * @param name          Name of this thread pool.
     * @param coreSize      Core pool size of this thread pool.
     * @param maxSize       Maximum pool size of this thread pool.
     * @param keepAliveTime when the number of threads is greater than
     *                      the core, this is the maximum time that excess idle threads
     *                      will wait for new tasks before terminating. The time unit is milliseconds.
     * @param unit          the time unit for the {@code keepAliveTime} argument
     * @param queue         Blocking queue for jobs.
     */
    public ThreadPool(String name, int coreSize, int maxSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> queue) {
        if (coreSize <= 0) coreSize = 1;
        if (maxSize <= coreSize) maxSize = coreSize;

        mThreadFactory = new PriorityThreadFactory(name, DEFAULT_THREAD_PRIORITY);
        mExecutor = new ThreadPoolExecutor(coreSize, maxSize,
                keepAliveTime, unit, queue, mThreadFactory);
    }

    /**
     * Submit a job to the thread pool. The listener will be called when the job is finished (or cancelled).
     *
     * @param job      Job to submit.
     * @param listener Future listener.
     * @return Future for this job.
     */
    public <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        Worker<T> w = new Worker<T>(job, listener);
        mExecutor.execute(w);
        return w;
    }

    /**
     * Submit a job to the thread pool.
     *
     * @param job Job to submit.
     * @return Future for this job.
     */
    public <T> Future<T> submit(Job<T> job) {
        return submit(job, null);
    }

    /**
     * Set the priority of this thread pool. Default is {@link #DEFAULT_THREAD_PRIORITY}.
     *
     * @param priority Thread priority, see constant defined in {@link android.os.Process}.
     */
    public void setPriority(int priority) {
        mThreadFactory.setPriority(priority);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     */
    public void shutdown() {
        mExecutor.shutdown();
    }

    /**
     * Returns {@code true} if this executor has been shut down.
     *
     * @return {@code true} if this executor has been shut down
     */
    public boolean isShutdown() {
        return mExecutor.isShutdown();
    }

    /*package*/
    final Executor getExecutor() {
        return mExecutor;
    }

    private class Worker<T> implements Runnable, Future<T>, JobContext, Comparable<Worker> {
        private static final String TAG = "Worker";
        private final Job<T> mJob;
        private final FutureListener<T> mListener;
        private CancelListener mCancelListener;
        private ResourceCounter mWaitOnResource;
        private volatile boolean mIsCancelled;
        private boolean mIsDone;
        private T mResult;
        private int mMode;

        public Worker(Job<T> job, FutureListener<T> listener) {
            mJob = job;
            mListener = listener;
        }

        // This is called by a thread in the thread pool.
        public void run() {
            T result = null;

            // A job is in CPU mode by default. setMode returns false
            // if the job is cancelled.
            if (setMode(MODE_CPU)) {
                result = mJob.run(this);
            }

            synchronized (this) {
                setMode(MODE_NONE);
                mResult = result;
                mIsDone = true;
                notifyAll();
            }
            if (mListener != null) mListener.onFutureDone(this);
        }

        // Below are the methods for Future.
        public synchronized void cancel() {
            if (mIsCancelled) return;
            mIsCancelled = true;
            if (mWaitOnResource != null) {
                synchronized (mWaitOnResource) {
                    mWaitOnResource.notifyAll();
                }
            }
            if (mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }

        public boolean isCancelled() {
            return mIsCancelled;
        }

        public synchronized boolean isDone() {
            return mIsDone;
        }

        public synchronized T get() {
            while (!mIsDone) {
                try {
                    wait();
                } catch (Exception ex) {
                    Logger.w(TAG, "ignore exception", ex);
                }
            }
            return mResult;
        }

        public void waitDone() {
            get();
        }

        // Below are the methods for JobContext (only called from the
        // thread running the job)
        public synchronized void setCancelListener(CancelListener listener) {
            mCancelListener = listener;
            if (mIsCancelled && mCancelListener != null) {
                mCancelListener.onCancel();
            }
        }

        public boolean setMode(int mode) {
            // Release old resource
            ResourceCounter rc = modeToCounter(mMode);
            if (rc != null) releaseResource(rc);
            mMode = MODE_NONE;

            // Acquire new resource
            rc = modeToCounter(mode);
            if (rc != null) {
                if (!acquireResource(rc)) {
                    return false;
                }
                mMode = mode;
            }

            return true;
        }

        private ResourceCounter modeToCounter(int mode) {
            if (mode == MODE_CPU) {
                return mCpuCounter;
            } else if (mode == MODE_NETWORK) {
                return mNetworkCounter;
            } else {
                return null;
            }
        }

        private boolean acquireResource(ResourceCounter counter) {
            while (true) {
                synchronized (this) {
                    if (mIsCancelled) {
                        mWaitOnResource = null;
                        return false;
                    }
                    mWaitOnResource = counter;
                }

                synchronized (counter) {
                    if (counter.value > 0) {
                        counter.value--;
                        break;
                    } else {
                        try {
                            counter.wait();
                        } catch (InterruptedException ex) {
                            // ignore.
                        }
                    }
                }
            }

            synchronized (this) {
                mWaitOnResource = null;
            }

            return true;
        }

        private void releaseResource(ResourceCounter counter) {
            synchronized (counter) {
                counter.value++;
                counter.notifyAll();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(Worker another) {
            Comparable<? super Job> myJob = (Comparable<? super Job>) mJob;
            return myJob.compareTo(another.mJob);
        }
    }

    /**
     * Inner priority thread factory for {@link ThreadPool}.
     */
    final static class PriorityThreadFactory implements ThreadFactory {

        private int mPriority;
        private final AtomicInteger mNumber = new AtomicInteger();
        private final String mName;

        public PriorityThreadFactory(String name, int priority) {
            mName = name;
            mPriority = priority;
        }

        public void setPriority(int priority) {
            mPriority = priority;
        }

        public Thread newThread(Runnable r) {
            return new Thread(r, mName + '-' + mNumber.getAndIncrement()) {
                @Override
                public void run() {
                    android.os.Process.setThreadPriority(mPriority);
                    super.run();
                }
            };
        }
    }
}