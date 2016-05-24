package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.Vuser;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for register.
 */
public class RegisterUseCase extends UseCase<Vuser> {

    private final UserRepository userRepository;
    private String mobile;
    private String password;
    private String verifyCode;

    @Inject
    public RegisterUseCase(UserRepository userRepository,
                           ThreadExecutor threadExecutor,
                           PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String mobile,
                         String password,
                         String verifyCode) {
        this.mobile = mobile;
        this.password = password;
        this.verifyCode = verifyCode;
    }

    @Override
    public Observable<Vuser> buildUseCaseObservable() {
        return userRepository.register(mobile, password, verifyCode);
    }
}
