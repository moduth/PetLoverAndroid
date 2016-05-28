package com.github.moduth.petlover.internal.di.modules;

import com.github.moduth.domain.interactor.UseCase;
import com.github.moduth.domain.interactor.user.LoginUseCase;
import com.github.moduth.petlover.internal.di.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Module
public class UserModule {

    @Provides
    @PerActivity
    @Named("login")
    UseCase provideLoginUseCase(LoginUseCase login) {
        return login;
    }

}
