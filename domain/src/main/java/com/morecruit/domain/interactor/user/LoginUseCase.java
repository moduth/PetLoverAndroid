package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.Vuser;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for login and
 * retrieve a {@link Vuser}.
 */
public class LoginUseCase extends UseCase<Vuser> {

    private final UserRepository userRepository;
    private String username;
    private String password;

    @Inject
    public LoginUseCase(UserRepository userRepository,
                        ThreadExecutor threadExecutor,
                        PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public Observable<Vuser> buildUseCaseObservable() {
        return this.userRepository.login(username, password);
    }
}
