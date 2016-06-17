package com.github.moduth.petlover.presenter;


import com.github.moduth.domain.interactor.DefaultSubscriber;
import com.github.moduth.domain.interactor.user.GetRepos;
import com.github.moduth.domain.model.repos.ReposEntity;
import com.github.moduth.petlover.mapper.ReposDataMapper;
import com.github.moduth.petlover.model.ReposModel;
import com.morecruit.ext.component.logger.Logger;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by Abner on 16/6/16.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
public class ReposListPresenter extends MvpBasePresenter<ReposListView> {

    private final GetRepos mGetRepos;
    private final ReposDataMapper mUserModelDataMapper;

    @Inject
    public ReposListPresenter(GetRepos getRepos,
                              ReposDataMapper userModelDataMapper) {
        mGetRepos = getRepos;
        mUserModelDataMapper = userModelDataMapper;
    }

    @Override
    public void detachView(boolean retainInstance) {
        mGetRepos.unsubscribe();
        super.detachView(retainInstance);
    }

    /**
     * Initializes the presenter by start retrieving the user
     */
    @Override
    public void initialize() {
        getUserList();
    }



    private void navigateAfterLogin(List<ReposEntity> reposEntity) {
        final List<ReposModel> reposModels = mUserModelDataMapper.transform(reposEntity);
        getView().userList(reposModels);
    }

    @SuppressWarnings("unchecked")
    private void getUserList() {
        mGetRepos.setParam("moduth");
        mGetRepos.execute(new UserSubscriber());
    }

    private class UserSubscriber extends DefaultSubscriber<List<ReposEntity>> {
        @Override
        public void onCompleted() {
            Logger.d("onCompleted", "onCompleted");
        }

        @Override
        public void onError(Throwable e) {
            Logger.d("onError", e.getMessage());
        }

        @Override
        public void onNext(List<ReposEntity> reposEntity) {
            Logger.d("onNext", "onNext");
            navigateAfterLogin(reposEntity);
        }
    }
}
