package com.innercirclesoftware.trimio.settings

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.ui.base.BaseActivity
import javax.inject.Inject

class SettingsActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel::class.java)
    }

    override val layout: Int = R.layout.activity_settings

    override fun inject() {
        DaggerSettingsActivityComponent.builder()
            .settingsActivityModule(SettingsActivityModule(this))
            .activityComponent(activityComponent)
            .build()
            .inject(this)
    }
}