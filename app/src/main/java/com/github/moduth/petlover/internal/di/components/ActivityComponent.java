package com.github.moduth.petlover.internal.di.components;

import android.app.Activity;


import com.github.moduth.petlover.internal.di.PerActivity;
import com.github.moduth.petlover.internal.di.modules.ActivityModule;

import dagger.Component;

/**
 * A base component upon which fragment's components may depend.
 * Activity-level components should extend this component.
 * <p>
 * Subtypes of ActivityComponent should be decorated with annotation:
 * {@link PerActivity}
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {
    // Exposed to sub-graphs.
    Activity activity();
}
