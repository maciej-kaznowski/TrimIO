package com.innercirclesoftware.trimio.trim

import com.innercirclesoftware.trimio.trim.periodic.PeriodicTrimPreferences
import com.innercirclesoftware.trimio.trim.periodic.PeriodicTrimPreferencesImpl
import com.innercirclesoftware.trimio.trim.periodic.TrimScheduler
import com.innercirclesoftware.trimio.trim.periodic.TrimSchedulerImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TrimModule {

    @Binds
    internal abstract fun bindsTrimmer(trimmerImpl: TrimmerImpl): Trimmer

    @Binds
    internal abstract fun bindsPeriodicTrimPreferences(periodicTrimPreferences: PeriodicTrimPreferencesImpl): PeriodicTrimPreferences

    @Binds
    internal abstract fun bindsTrimScheduler(trimSchedulerImpl: TrimSchedulerImpl): TrimScheduler

}