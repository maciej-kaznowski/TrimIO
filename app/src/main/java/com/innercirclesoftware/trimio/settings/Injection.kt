package com.innercirclesoftware.trimio.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innercirclesoftware.trimio.ui.base.ActivityComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Provider
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerSettingsActivity

@PerSettingsActivity
@Component(dependencies = [ActivityComponent::class], modules = [SettingsActivityModule::class])
interface SettingsActivityComponent {

    fun inject(activity: SettingsActivity)

}

@Module(includes = [PreferenceModule::class])
class SettingsActivityModule(activity: SettingsActivity) {

    @PerSettingsActivity
    @Provides
    fun providesViewModelFactory(settingsViewModel: Provider<SettingsViewModel>): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (modelClass) {
                    SettingsViewModel::class.java -> settingsViewModel.get() as T
                    else -> throw IllegalArgumentException("Missing ViewModel provider for class=$modelClass")
                }
            }
        }
    }
}