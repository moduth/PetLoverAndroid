package com.morecruit.ext.component.thread;

/**
 * Future for job submitted into {@link ThreadPool}.
 *
 * @param <T>
 */
public interface Future<T> {
    void cancel();

    boolean isCancelled();

    boolean isDone();

    T get();

    void waitDone();
}