package com.github.moduth.petlover.internal.di.modules;

import android.content.Context;


import com.github.moduth.data.repository.UserDataRepository;
import com.github.moduth.domain.repository.UserRepository;

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
    UserRepository provideUserRepository(UserDataRepository userDataRepository) {
        return userDataRepository;
    }


}
