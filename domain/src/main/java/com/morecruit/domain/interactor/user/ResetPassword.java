package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/29
 * @version 1.0.0
 */
public class ResetPassword extends UseCase<Void> {

    private final UserRepository userRepository;
    private String mobile;
    private String password;
    private String rePassword;
    private String verifyCode;

    @Inject
    public ResetPassword(UserRepository userRepository,
                         ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String mobile,
                         String password,
                         String rePassword,
                         String verifyCode) {
        this.mobile = mobile;
        this.password = password;
        this.rePassword = rePassword;
        this.verifyCode = verifyCode;
    }

    @Override
    public Observable<Void> buildUseCaseObservable() {
        return userRepository.resetPassword(mobile, password, rePassword, verifyCode);
    }
}
