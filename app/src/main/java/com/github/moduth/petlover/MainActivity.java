package com.github.moduth.petlover;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.github.moduth.domain.interactor.repo.GetRepos;
import com.github.moduth.petlover.databinding.ActivityMainBinding;
import com.github.moduth.petlover.internal.di.components.DaggerReposComponent;
import com.github.moduth.petlover.internal.di.modules.ReposModule;
import com.github.moduth.petlover.model.ReposModel;
import com.github.moduth.petlover.presenter.ReposListPresenter;
import com.github.moduth.petlover.presenter.ReposListView;
import com.github.moduth.petlover.view.MvpActivity;
import com.jakewharton.rxbinding.view.RxView;
import com.morecruit.ext.component.logger.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MainActivity extends MvpActivity<ReposListView, ReposListPresenter> implements ReposListView {

    private ActivityMainBinding mBinding;
    private rx.functions.Action1<Void> mLoginAction = aVoid -> login();

    @Inject
    ReposListPresenter mReposListPresenter;

    @Inject
    GetRepos mGetRepos;

    @Override
    public ReposListPresenter getPresenter() {
        return mReposListPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DaggerReposComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .reposModule(new ReposModule())
                .build()
                .inject(this);
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        RxView.clicks(mBinding.actionLogin)
                .throttleFirst(PLConstants.ON_CLICK_DURATION, TimeUnit.MILLISECONDS)
                .subscribe(mLoginAction);
    }

    private void login() {

        mReposListPresenter.initialize();

    }


    @Override
    public void userList(List<ReposModel> userModels) {
        // TODO navigate to main page
        Logger.d("git",userModels.toArray());
    }

}
