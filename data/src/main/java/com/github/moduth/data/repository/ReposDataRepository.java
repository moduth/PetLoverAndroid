package com.github.moduth.data.repository;

import com.github.moduth.data.net.api.ReposApi;
import com.github.moduth.domain.model.user.ReposEntity;
import com.github.moduth.domain.repository.ReposRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Singleton
public class ReposDataRepository implements ReposRepository {

    @Inject
    ReposApi mUserApi;

    @Inject
    public ReposDataRepository() {
    }

    @Override
    public Observable<List<ReposEntity>> getReposList(String user) {
        return mUserApi.login(user);
    }
}
