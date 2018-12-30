package com.innercirclesoftware.trimio.settings

import dagger.Binds
import dagger.Module

@Module
abstract class PreferenceModule {

    @Binds
    abstract fun bindsPreferenceManager(preferenceManager: PreferenceManagerImpl): PreferenceManager

}