package com.github.moduth.petlover.internal.di.modules;

import com.github.moduth.data.net.PLService;
import com.github.moduth.data.net.api.ReposApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@Module
public class ApiModule {


    @Provides
    @Singleton
    ReposApi getUserApi() {
        return createApi(ReposApi.class);
    }


    private <T> T createApi(Class<T> clazz) {
        return PLService.getInstance().createApi(clazz);
    }
}
