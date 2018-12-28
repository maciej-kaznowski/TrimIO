package com.innercirclesoftware.trimio.app

import com.innercirclesoftware.trimio.ui.base.ActivityComponent
import dagger.Component
import dagger.Module
import javax.inject.Scope

@Retention(AnnotationRetention.RUNTIME)
@Scope
annotation class PerApp

@PerApp
@Component(
    modules = [
        AppModule::class
    ]
)
interface AppComponent {

    abstract fun newActivityComponent(): ActivityComponent.Builder

    fun inject(app: App)

}

@Module
class AppModule(val app: App)