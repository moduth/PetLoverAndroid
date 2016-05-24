package com.morecruit.domain.interactor;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.model.user.UserInfoEntity;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for
 * retrieving data related to an specific {@link UserInfoEntity}.
 *
 * @author markzhai
 * @version 1.0.0
 */
public class GetUserDetails extends UseCase<UserInfoEntity> {

    private final String userId;
    private final UserRepository userRepository;

    @Inject
    public GetUserDetails(String userId,
                          UserRepository userRepository,
                          ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userId = userId;
        this.userRepository = userRepository;
    }

    @Override
    protected Observable<UserInfoEntity> buildUseCaseObservable() {
        return this.userRepository.user(this.userId);
    }
}

