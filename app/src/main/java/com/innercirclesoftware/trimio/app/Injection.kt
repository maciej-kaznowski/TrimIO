package com.innercirclesoftware.trimio.app

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

    fun inject(app: App)

}

@Module
class AppModule(val app: App)