package com.github.moduth.petlover.presenter;


import com.github.moduth.petlover.view.MvpView;

import java.lang.ref.WeakReference;

/**
 * A base implementation of {@link MvpPresenter} with view weak reference.
 *
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public abstract class MvpBasePresenter<V extends MvpView> implements MvpPresenter<V> {

    private WeakReference<V> viewRef;

    /**
     * 将view attach到presenter
     */
    @Override
    public void attachView(V view) {
        viewRef = new WeakReference<>(view);
    }

    /**
     * 会在view被destroyed的时候被调用. 通常该方法会发生在
     * <code>Activity.detachView()</code> 或 <code>Fragment.onDestroyView()</code>
     */
    @Override
    public void detachView(boolean retainInstance) {
        if (viewRef != null) {
            viewRef.clear();
            viewRef = null;
        }
    }

    @Override
    public V getView() {
        return viewRef == null ? null : viewRef.get();
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {

    }

    /**
     * 检查一个view是否被attached到该presenter，须在调用 {@link #getView()} 获得view前调用本方法.
     */
    protected boolean isViewAttached() {
        return viewRef != null && viewRef.get() != null;
    }
}
