package com.github.moduth.domain.interactor.repo;

import com.github.moduth.domain.executor.PostExecutionThread;
import com.github.moduth.domain.executor.ThreadExecutor;
import com.github.moduth.domain.interactor.UseCase;
import com.github.moduth.domain.model.repos.ReposEntity;
import com.github.moduth.domain.repository.ReposRepository;

import java.util.List;

import rx.Observable;

/**
 * This class is an implementation of {@link com.github.moduth.domain.interactor.UseCase} that represents a use case for getReposList and
 * retrieve a {@link ReposEntity}.
 */
public class GetRepos extends UseCase<List<ReposEntity>> {

    private final ReposRepository mReposRepository;
    private String user;

    public GetRepos(ReposRepository reposRepository,
                    ThreadExecutor threadExecutor,
                    PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.mReposRepository = reposRepository;
    }

    public void setParam(String user) {
        this.user = user;
    }

    @Override
    public Observable<List<ReposEntity>> buildUseCaseObservable() {
        return this.mReposRepository.getReposList(user);
    }
}
