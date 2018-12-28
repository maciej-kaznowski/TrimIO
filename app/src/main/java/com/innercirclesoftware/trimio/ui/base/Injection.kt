package com.innercirclesoftware.trimio.ui.base

import dagger.Module
import dagger.Subcomponent
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerActivity

@Module
class ActivityModule(activity: BaseActivity)

@PerActivity
@Subcomponent(
    modules = [
        ActivityModule::class
    ]
)
interface ActivityComponent {

    @Subcomponent.Builder
    interface Builder {

        fun setActivityModule(module: ActivityModule): Builder

        fun build(): ActivityComponent

    }
}