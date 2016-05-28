package com.github.moduth.petlover.internal.di.components;

import com.github.moduth.petlover.MainActivity;
import com.github.moduth.petlover.internal.di.PerActivity;
import com.github.moduth.petlover.internal.di.modules.ActivityModule;
import com.github.moduth.petlover.internal.di.modules.UserModule;


import dagger.Component;

/**
 * Created by Abner on 16/5/27.
 * Email nimengbo@gmail.com
 * github https://github.com/nimengbo
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class},
        modules = {ActivityModule.class, UserModule.class})
public interface UserComponent extends ActivityComponent {

    void inject(MainActivity activity);

}
