package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.SettingEntity;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/29
 * @version 1.0.0
 */
public class GetSetting extends UseCase<SettingEntity> {

    private final UserRepository userRepository;

    @Inject
    public GetSetting(UserRepository userRepository,
                      ThreadExecutor threadExecutor,
                      PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    @Override
    public Observable<SettingEntity> buildUseCaseObservable() {
        return userRepository.getSetting();
    }
}
