package com.github.moduth.petlover.internal.di.components;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */

import android.content.Context;

import com.github.moduth.domain.executor.PostExecutionThread;
import com.github.moduth.domain.executor.ThreadExecutor;
import com.github.moduth.domain.repository.ReposRepository;
import com.github.moduth.petlover.PLActivity;
import com.github.moduth.petlover.internal.di.modules.ApiModule;
import com.github.moduth.petlover.internal.di.modules.ApplicationModule;
import com.github.moduth.petlover.internal.di.modules.RepositoryModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class,ApiModule.class, RepositoryModule.class})
public interface ApplicationComponent {

    void inject(PLActivity baseActivity);

    // Exposed to sub-graphs.
    Context context();

    ThreadExecutor threadExecutor();

    PostExecutionThread postExecutionThread();

    ReposRepository reposRepository();

}
