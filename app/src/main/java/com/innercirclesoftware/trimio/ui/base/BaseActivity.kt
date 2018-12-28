package com.innercirclesoftware.trimio.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.innercirclesoftware.trimio.app.App
import com.innercirclesoftware.trimio.app.AppComponent

abstract class BaseActivity : AppCompatActivity() {

    private val app: App
        get() = application as App

    private val appComponent: AppComponent
        get() = app.component

    val activityComponent: ActivityComponent by lazy {
        appComponent
            .newActivityComponent()
            .setActivityModule(ActivityModule(this))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject()
    }

    abstract fun inject()

}