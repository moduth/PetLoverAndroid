package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.VerifyCode;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * This class is an implementation of {@link UseCase} that represents a use case for
 * getting verify code.
 */
public class GetVerifyCode extends UseCase<VerifyCode> {

    private final UserRepository userRepository;

    private String mobile;
    private String captureCode;
    private String scene;
    private String sessionId = null;

    @Inject
    public GetVerifyCode(UserRepository userRepository,
                         ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String mobile, String captureCode, String scene) {
        this.captureCode = captureCode;
        this.mobile = mobile;
        this.scene = scene;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public Observable<VerifyCode> buildUseCaseObservable() {
        return userRepository.sendVerifyCode(mobile, captureCode, scene, sessionId);
    }
}
