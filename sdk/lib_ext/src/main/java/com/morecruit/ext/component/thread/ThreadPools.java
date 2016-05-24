package com.morecruit.ext.component.thread;

import android.annotation.TargetApi;
import android.os.Build;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * 线程池
 * <p/>
 * Created by zhaiyifan on 2015/8/3.
 */
public final class ThreadPools {

    private ThreadPools() {
        // static usage.
    }

    private static class DefaultHolder {
        public static final PriorityThreadPool INSTANCE = new PriorityThreadPool();
    }

    /**
     * Returns the default thread pool ({@link PriorityThreadPool}),
     * whose pool size is {@link ThreadPool#DEFAULT_POOL_SIZE}.
     *
     * @return Default thread pool.
     */
    public static PriorityThreadPool defaultThreadPool() {
        return DefaultHolder.INSTANCE;
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPool} constructors.
     *
     * @return the newly created thread pool
     */
    public static ThreadPool newCachedThreadPool(String name) {
        return newCachedThreadPool(name, 60L, TimeUnit.SECONDS);
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPool} constructors.
     *
     * @return the newly created thread pool
     */
    public static ThreadPool newCachedThreadPool(String name, long timeout, TimeUnit timeunit) {
        return new ThreadPool(name, 0, Integer.MAX_VALUE,
                timeout, timeunit,
                new SynchronousQueue<Runnable>());
    }

    /**
     * <p>Support in API level 9 and above.</p>
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPool} constructors.
     *
     * @return the newly created thread pool
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static ThreadPool newCachedThreadPool(String name, int maxThreads) {
        return newCachedThreadPool(name, maxThreads, 60L, TimeUnit.SECONDS);
    }

    /**
     * <p>Support in API level 9 and above.</p>
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for corresponding timeout are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPool} constructors.
     *
     * @return the newly created thread pool
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static ThreadPool newCachedThreadPool(String name, int maxThreads, long timeout, TimeUnit timeunit) {
        return new ThreadPool(name, maxThreads,
                timeout, timeunit);
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue.  At any point, at most
     * {@code nThreads} threads will be active processing tasks.
     * If additional tasks are submitted when all threads are active,
     * they will wait in the queue until a thread is available.
     * If any thread terminates due to a failure during execution
     * prior to shutdown, a new one will take its place if needed to
     * execute subsequent tasks.  The threads in the pool will exist
     * until it is explicitly {@link ThreadPool#shutdown shutdown}.
     *
     * @param nThreads the number of threads in the pool
     * @return the newly created thread pool
     * @throws IllegalArgumentException if {@code nThreads <= 0}
     */
    public static ThreadPool newFixedThreadPool(String name, int nThreads) {
        return new ThreadPool(name, nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
    }
}
