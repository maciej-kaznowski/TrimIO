package com.innercirclesoftware.trimio.app

import android.content.Context
import androidx.work.*
import com.innercirclesoftware.trimio.settings.PreferenceModule
import com.innercirclesoftware.trimio.trim.TrimModule
import com.innercirclesoftware.trimio.trim.Trimmer
import com.innercirclesoftware.trimio.trim.periodic.PeriodicTrimPreferences
import com.innercirclesoftware.trimio.trim.periodic.TrimWork
import dagger.Module
import dagger.Provides
import javax.inject.Provider

@Module(includes = [TrimModule::class, PreferenceModule::class])
class WorkModule {

    @Provides
    fun providesWorkerFactory(
        trimmer: Provider<Trimmer>,
        periodicTrimPreferences: Provider<PeriodicTrimPreferences>
    ): WorkerFactory {
        return object : WorkerFactory() {

            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return when (workerClassName) {
                    TrimWork::class.java.canonicalName -> TrimWork(
                        trimmer.get(),
                        periodicTrimPreferences.get(),
                        appContext,
                        workerParameters
                    )
                    else -> throw IllegalArgumentException("Could not create worker for class=$workerClassName")
                }
            }
        }
    }

    @Provides
    fun providesWorkManager() = WorkManager.getInstance()

    @Provides
    fun providesWorkManagerConfiguration(workerFactory: WorkerFactory): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}