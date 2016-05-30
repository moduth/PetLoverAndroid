package com.github.moduth.petlover;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.moduth.domain.interactor.user.LoginUseCase;
import com.github.moduth.domain.model.user.Vuser;
import com.github.moduth.petlover.databinding.ActivityMainBinding;
import com.github.moduth.petlover.internal.di.components.ApplicationComponent;
import com.github.moduth.petlover.internal.di.components.DaggerUserComponent;
import com.github.moduth.petlover.internal.di.modules.ActivityModule;
import com.github.moduth.petlover.internal.di.modules.UserModule;
import com.jakewharton.rxbinding.view.RxView;
import com.morecruit.ext.component.logger.Logger;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Subscriber;

public class MainActivity extends PLActivity {

    private ActivityMainBinding mBinding;
    private rx.functions.Action1<Void> mLoginAction = aVoid -> login();


    @Inject
    LoginUseCase mUserCase;

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

    private void login() {

        mUserCase.setParam("moduth", "password");
        mUserCase.execute(new Subscriber<Vuser>() {
            @Override
            public void onCompleted() {
                Logger.d("onCompleted", "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("onError", "onError");
            }

            @Override
            public void onNext(Vuser vuser) {
                Logger.d("onNext", "onNext");
            }
        });
    }


}
