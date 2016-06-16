package com.github.moduth.petlover.presenter;

import com.github.moduth.petlover.model.UserModel;
import com.github.moduth.petlover.view.MvpView;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public interface UserLoginView extends MvpView {

    void userLogin(UserModel userModel);

}
