package com.github.moduth.petlover.mapper;

import com.github.moduth.domain.model.user.Vuser;
import com.github.moduth.petlover.internal.di.PerActivity;
import com.github.moduth.petlover.model.UserModel;

import javax.inject.Inject;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@PerActivity
public class UserDataMapper {

    @Inject
    public  UserDataMapper(){
    }


    public UserModel transform(Vuser vuser){
        if (vuser == null) {
            throw new IllegalArgumentException("Cannot transform a null value");
        }
        UserModel userModel = new UserModel();
        userModel.setUid(vuser.vuser);
        return userModel;
    }
}
