package com.github.moduth.petlover;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.github.moduth.domain.interactor.user.LoginUseCase;
import com.github.moduth.petlover.databinding.ActivityMainBinding;
import com.github.moduth.petlover.internal.di.components.DaggerUserComponent;
import com.github.moduth.petlover.internal.di.modules.UserModule;
import com.github.moduth.petlover.model.UserModel;
import com.github.moduth.petlover.presenter.UserPresenter;
import com.github.moduth.petlover.presenter.UserLoginView;
import com.github.moduth.petlover.view.MvpActivity;
import com.jakewharton.rxbinding.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MainActivity extends MvpActivity<UserLoginView, UserPresenter> implements UserLoginView {

    private ActivityMainBinding mBinding;
    private rx.functions.Action1<Void> mLoginAction = aVoid -> login();

    @Inject
    UserPresenter mUserPresenter;

    @Inject
    LoginUseCase mUserCase;

    @Override
    public UserPresenter getPresenter() {
        return mUserPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .userModule(new UserModule())
                .build()
                .inject(this);
        initView();
    }

    private void initView() {
        RxView.clicks(mBinding.actionLogin)
                .throttleFirst(PLConstants.ON_CLICK_DURATION, TimeUnit.MILLISECONDS)
                .subscribe(mLoginAction);
    }

    /**
     * 这里只是为了mvp演示,其实这种登录单向操作的不需要用mvp
     */
    private void login() {

        mUserPresenter.initialize();

    }


    @Override
    public void userLogin(UserModel userModel) {
        // TODO navigate to main page
    }
}
