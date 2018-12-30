package com.innercirclesoftware.trimio.settings

import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

interface PreferenceManager {

    fun setPeriodicTrimEnabled(enabled: Boolean): Completable

    fun getPeriodicTrimEnabled(): Flowable<Boolean>

}

class PreferenceManagerImpl @Inject constructor() : PreferenceManager {

    override fun setPeriodicTrimEnabled(enabled: Boolean): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPeriodicTrimEnabled(): Flowable<Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}