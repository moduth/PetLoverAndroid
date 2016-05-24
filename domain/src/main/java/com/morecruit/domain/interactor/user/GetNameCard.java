package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.model.user.NameCard;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/29
 * @version 1.0.0
 */
public class GetNameCard extends UseCase<NameCard> {

    private final UserRepository userRepository;
    private String userId;

    @Inject
    public GetNameCard(UserRepository userRepository,
                       ThreadExecutor threadExecutor,
                       PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setParam(String userId) {
        this.userId = userId;
    }

    @Override
    public Observable<NameCard> buildUseCaseObservable() {
        return userRepository.getNameCard(userId);
    }
}