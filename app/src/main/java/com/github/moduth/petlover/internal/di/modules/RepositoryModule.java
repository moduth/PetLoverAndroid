package com.github.moduth.petlover.internal.di.modules;


import com.github.moduth.data.repository.ReposDataRepository;
import com.github.moduth.domain.repository.ReposRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Abner on 16/5/18.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Module
public class RepositoryModule {

    @Provides
    @Singleton
    ReposRepository provideUserRepository(ReposDataRepository userDataRepository) {
        return userDataRepository;
    }


}
