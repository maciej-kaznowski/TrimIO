package com.innercirclesoftware.trimio

import android.app.Application
import com.github.ajalt.timberkt.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
        Timber.v { "Planted Timber.DebugTree" }
    }
}