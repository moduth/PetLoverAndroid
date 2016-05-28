package com.github.moduth.data.repository;

import com.github.moduth.data.net.api.UserApi;
import com.github.moduth.domain.model.user.Vuser;
import com.github.moduth.domain.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Singleton
public class UserDataRepository implements UserRepository{

    @Inject
    UserApi mUserApi;

    @Inject
    public UserDataRepository() {
    }

    @Override
    public Observable<Vuser> login(String mobile, String password) {
        return RepositoryUtils.extractData(mUserApi.login(mobile,password),Vuser.class);
    }
}
