package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/8
 * @version 1.0.0
 */
public class PerfectProfile extends UseCase<Void> {

    private final UserRepository userRepository;

    private String avatar;
    private String nickname;
    private int sex;

    @Inject
    public PerfectProfile(UserRepository userRepository,
                          ThreadExecutor threadExecutor,
                          PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setData(String avatar, String nickname, int sex) {
        this.avatar = avatar;
        this.nickname = nickname;
        this.sex = sex;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return userRepository.perfect(avatar, nickname, sex);
    }
}
