package com.github.moduth.petlover.view;

import android.content.Context;
import android.os.Bundle;

import com.github.moduth.petlover.PLActivity;
import com.github.moduth.petlover.presenter.MvpPresenter;


/**
 * @author markzhai on 16/3/4
 * @version 1.0.0
 */
public abstract class MvpActivity<V extends MvpView, P extends MvpPresenter<V>>
        extends PLActivity implements MvpView {

    public abstract P getPresenter();

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPresenter().attachView((V) this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPresenter().detachView(isRetainingInstance());
        getPresenter().destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPresenter().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPresenter().resume();
    }

    public boolean isRetainingInstance() {
        return false;
    }

    @Override
    public Context context() {
        return getApplicationContext();
    }
}
