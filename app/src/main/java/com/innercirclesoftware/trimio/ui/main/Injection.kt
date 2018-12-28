package com.innercirclesoftware.trimio.ui.main

import androidx.lifecycle.ViewModelProviders
import com.innercirclesoftware.trimio.ui.base.ActivityComponent
import dagger.Component
import dagger.Module
import dagger.Provides
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

@Module
class MainActivityModule(private val activity: MainActivity) {

    @PerMainActivity
    @Provides
    fun providesViewModel(): MainViewModel {
        return ViewModelProviders.of(activity).get(MainViewModel::class.java)
    }
}
