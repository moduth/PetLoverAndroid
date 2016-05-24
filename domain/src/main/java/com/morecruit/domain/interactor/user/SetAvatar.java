package com.morecruit.domain.interactor.user;

import com.morecruit.domain.executor.PostExecutionThread;
import com.morecruit.domain.executor.ThreadExecutor;
import com.morecruit.domain.interactor.UseCase;
import com.morecruit.domain.repository.AlbumRepository;
import com.morecruit.domain.repository.UserRepository;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author markzhai on 16/3/25
 * @version 1.0.0
 */
public class SetAvatar extends UseCase<Void> {

    private final UserRepository repository;
    private String photoId;

    @Inject
    public SetAvatar(UserRepository repository,
                     ThreadExecutor threadExecutor,
                     PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.repository = repository;
    }

    public void setParam(String photoId) {
        this.photoId = photoId;
    }

    @Override
    protected Observable<Void> buildUseCaseObservable() {
        return repository.setAvatar(photoId);
    }
}