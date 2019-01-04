package com.innercirclesoftware.trimio.app

import android.annotation.SuppressLint
import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.github.ajalt.timberkt.Timber
import com.innercirclesoftware.trimio.trim.periodic.TrimScheduler
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class App : Application() {

    @Inject
    lateinit var workConfiguration: Configuration

    @Inject
    lateinit var trimScheduler: TrimScheduler

    val component: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        initTimber()
        inject()
        initWorkManager()
        scheduleTrimmer()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        Timber.v { "Planted Timber.DebugTree" }
    }

    private fun inject() {
        component.inject(this)
        Timber.v { "Injected App" }
    }

    private fun initWorkManager() {
        Timber.v { "Initializing WorkManager" }
        WorkManager.initialize(this, workConfiguration)
    }

    @SuppressLint("CheckResult")
    private fun scheduleTrimmer() {
        trimScheduler.schedule()
            .subscribeOn(Schedulers.io())
            .firstOrError()
            .subscribeBy { subscribed ->
                if (subscribed) Timber.i { "Scheduled trimmer job" }
                else Timber.i { "Unscheduled trimmer job" }
            }
    }
}