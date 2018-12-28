package com.innercirclesoftware.trimio.app

import android.app.Application
import com.github.ajalt.timberkt.Timber

class App : Application() {

    val component: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        initTimber()
        inject()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        Timber.v { "Planted Timber.DebugTree" }
    }

    private fun inject() {
        component.inject(this)
        Timber.v { "Injected App" }
    }
}