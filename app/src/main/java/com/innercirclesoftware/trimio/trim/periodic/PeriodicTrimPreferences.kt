package com.innercirclesoftware.trimio.trim.periodic

import com.innercirclesoftware.trimio.settings.PreferenceManager
import com.innercirclesoftware.trimio.trim.Partition
import io.reactivex.Flowable
import io.reactivex.rxkotlin.Flowables
import javax.inject.Inject

interface PeriodicTrimPreferences {

    fun getPeriodicTrimPreference(): Flowable<PeriodicTrimPreference>

}

class PeriodicTrimPreferencesImpl @Inject constructor(private val preferenceManager: PreferenceManager) :
    PeriodicTrimPreferences {

    override fun getPeriodicTrimPreference(): Flowable<PeriodicTrimPreference> {
        return Flowables
            .combineLatest(
                preferenceManager.getPeriodicTrimFrequency(),
                preferenceManager.getSelectedPartitions()
            ) { enabled, partitions -> PeriodicTrimPreference(enabled, partitions) }
    }
}

data class PeriodicTrimPreference(val frequency: Frequency, val partitions: List<Partition>)
