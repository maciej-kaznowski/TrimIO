package com.innercirclesoftware.trimio.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.innercirclesoftware.trimio.trim.TrimModule
import com.innercirclesoftware.trimio.ui.base.ActivityComponent
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Provider
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class PerMainActivity

@PerMainActivity
@Component(
    dependencies = [ActivityComponent::class],
    modules = [MainActivityModule::class]
)
interface MainActivityComponent {

    fun inject(activity: MainActivity)

}

@Module(includes = [TrimModule::class])
class MainActivityModule(private val activity: MainActivity) {

    @PerMainActivity
    @Provides
    fun providesViewModelFactory(mainViewModelProvider: Provider<MainViewModel>): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return when (modelClass) {
                    MainViewModel::class.java -> mainViewModelProvider.get() as T
                    else -> {
                        throw IllegalArgumentException("Missing ViewModel provider for class=$modelClass")
                    }
                }
            }
        }
    }
}
