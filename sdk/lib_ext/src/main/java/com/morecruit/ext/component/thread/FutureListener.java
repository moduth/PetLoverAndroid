package com.morecruit.ext.component.thread;

/**
 * Future listener for job submitted into {@link ThreadPool}.
 *
 * @param <T>
 */
public interface FutureListener<T> {
    void onFutureDone(Future<T> future);
}