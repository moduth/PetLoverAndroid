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
public class ChangeMobile extends UseCase<Void> {

    private final UserRepository userRepository;
    private String mobile;
    private String verifyCode;

    @Inject
    public ChangeMobile(UserRepository userRepository,
                        ThreadExecutor threadExecutor,
                        PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String mobile, String verifyCode) {
        this.mobile = mobile;
        this.verifyCode = verifyCode;
    }

    @Override
    public Observable<Void> buildUseCaseObservable() {
        return userRepository.changeMobile(mobile, verifyCode);
    }
}
