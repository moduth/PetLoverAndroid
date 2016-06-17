package com.github.moduth.petlover.internal.di.modules;

import com.github.moduth.domain.executor.PostExecutionThread;
import com.github.moduth.domain.executor.ThreadExecutor;
import com.github.moduth.domain.interactor.user.GetRepos;
import com.github.moduth.domain.repository.ReposRepository;
import com.github.moduth.petlover.internal.di.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Module
public class ReposModule {

    @Provides
    @PerActivity
    GetRepos provideLoginUseCase(ReposRepository reposRepository,
                                 ThreadExecutor threadExecutor,
                                 PostExecutionThread postExecutionThread) {
        return new GetRepos(reposRepository, threadExecutor, postExecutionThread);
    }

}
