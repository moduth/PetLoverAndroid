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
public class UpdateSetting extends UseCase<Void> {

    private final UserRepository userRepository;

    private String field;
    private int value;

    public static final String FIELD_RECV_MSG = "recv_msg_switch";

    @Inject
    public UpdateSetting(UserRepository userRepository,
                         ThreadExecutor threadExecutor,
                         PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String field, int value) {
        this.field = field;
        this.value = value;
    }

    @Override
    public Observable<Void> buildUseCaseObservable() {
        return userRepository.updateSetting(field, value);
    }
}
