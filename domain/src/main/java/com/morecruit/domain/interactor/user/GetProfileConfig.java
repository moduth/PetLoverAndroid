package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.ProfileConfig;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/29
 * @version 1.0.0
 */
public class GetProfileConfig extends UseCase<ProfileConfig> {

    private final UserRepository userRepository;

    @Inject
    public GetProfileConfig(UserRepository userRepository,
                            ThreadExecutor threadExecutor,
                            PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    @Override
    public Observable<ProfileConfig> buildUseCaseObservable() {
        return userRepository.getProfileConfig();
    }
}
