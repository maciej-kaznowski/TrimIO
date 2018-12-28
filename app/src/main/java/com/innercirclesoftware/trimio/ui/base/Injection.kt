package com.innercirclesoftware.trimio.ui.base

import android.content.Context
import android.content.res.AssetManager
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

    fun getContext(): Context

    fun getAssetManager(): AssetManager

    @Subcomponent.Builder
    interface Builder {

        fun setActivityModule(module: ActivityModule): Builder

        fun build(): ActivityComponent

    }
}