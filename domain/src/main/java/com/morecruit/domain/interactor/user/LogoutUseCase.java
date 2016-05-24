package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for logout.
 */
public class LogoutUseCase extends UseCase<Void> {

    private final UserRepository userRepository;

    @Inject
    public LogoutUseCase(UserRepository userRepository,
                         ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    @Override
    public Observable<Void> buildUseCaseObservable() {
        return userRepository.logout();
    }
}
