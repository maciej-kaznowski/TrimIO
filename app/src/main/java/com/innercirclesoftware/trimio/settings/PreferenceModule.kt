package com.innercirclesoftware.trimio.settings

import android.content.Context
import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class PreferenceModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesSharedPreferences(context: Context): SharedPreferences {
            return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        }

        @Provides
        @JvmStatic
        fun providesRxPreferences(preferences: SharedPreferences): RxSharedPreferences {
            return RxSharedPreferences.create(preferences)
        }
    }

    @Binds
    abstract fun bindsPreferenceManager(preferenceManager: PreferenceManagerImpl): PreferenceManager

}