package com.github.moduth.petlover.presenter;


import com.github.moduth.domain.interactor.DefaultSubscriber;
import com.github.moduth.domain.interactor.user.LoginUseCase;
import com.github.moduth.domain.model.user.Vuser;
import com.github.moduth.petlover.mapper.UserDataMapper;
import com.github.moduth.petlover.model.UserModel;
import com.morecruit.ext.component.logger.Logger;

import javax.inject.Inject;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class UserPresenter extends MvpBasePresenter<UserLoginView> {

    private final LoginUseCase mLoginUseCase;
    private final UserDataMapper mUserModelDataMapper;

    @Inject
    public UserPresenter(LoginUseCase getUserListUserCase,
                         UserDataMapper userModelDataMapper) {
        mLoginUseCase = getUserListUserCase;
        mUserModelDataMapper = userModelDataMapper;
    }

    @Override
    public void detachView(boolean retainInstance) {
        mLoginUseCase.unsubscribe();
        super.detachView(retainInstance);
    }

    /**
     * Initializes the presenter by start retrieving the user
     */
    @Override
    public void initialize() {
        login();
    }



    private void navigateAfterLogin(Vuser vuser) {
        final UserModel userModel = mUserModelDataMapper.transform(vuser);
        getView().userLogin(userModel);
    }

    @SuppressWarnings("unchecked")
    private void login() {
        mLoginUseCase.setParam("hehe","hhe");
        mLoginUseCase.execute(new UserSubscriber());
    }

    private class UserSubscriber extends DefaultSubscriber<Vuser> {
        @Override
        public void onCompleted() {
            Logger.d("onCompleted", "onCompleted");
        }

        @Override
        public void onError(Throwable e) {
            Logger.d("onError", e.getMessage());
        }

        @Override
        public void onNext(Vuser vuser) {
            Logger.d("onNext", "onNext");
            navigateAfterLogin(vuser);
        }
    }
}
