package com.morecruit.domain.interactor;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.model.user.UserInfoEntity;
import com.morecruit.domain.repository.UserRepository;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for
 * retrieving a collection of all {@link UserInfoEntity}.
 *
 * @author markzhai
 * @version 1.0.0
 */
public class GetUserList extends UseCase<List<UserInfoEntity>> {

    private final UserRepository userRepository;
    private final String tag;

    @Inject
    public GetUserList(String tag,
                       UserRepository userRepository,
                       ThreadExecutor threadExecutor,
                       PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.tag = tag;
        this.userRepository = userRepository;
    }

    @Override
    public Observable<List<UserInfoEntity>> buildUseCaseObservable() {
        return this.userRepository.users(tag);
    }
}

