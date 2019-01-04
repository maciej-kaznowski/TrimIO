package com.innercirclesoftware.trimio.app

import android.content.Context
import android.content.res.AssetManager
import com.innercirclesoftware.trimio.ui.base.ActivityComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Scope

@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class PerApp

@PerApp
@Component(
    modules = [
        AppModule::class,
        WorkModule::class
    ]
)
interface AppComponent {

    fun newActivityComponent(): ActivityComponent.Builder

    fun inject(app: App)

    fun getContext(): Context

    fun getAssetManager(): AssetManager

}

@Module
class AppModule(private val app: App) {

    @Provides
    @PerApp
    fun providesAppContext(): Context {
        return app
    }

    @Provides
    @PerApp
    fun providesAssetManager(): AssetManager {
        return app.assets
    }
}