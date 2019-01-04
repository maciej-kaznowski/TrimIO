package com.innercirclesoftware.trimio.settings

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.periodic.Frequency
import com.innercirclesoftware.trimio.trim.toPartition
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

interface PreferenceManager {

    fun setPeriodicTrimFrequency(frequency: Frequency): Completable

    fun getPeriodicTrimFrequency(): Flowable<Frequency>

    fun setSelectedPartitions(partitions: List<Partition>): Completable

    fun getSelectedPartitions(): Flowable<List<Partition>>

}

class PreferenceManagerImpl @Inject constructor(private val preferences: RxSharedPreferences) : PreferenceManager {

    private val trimFrequency: Preference<Frequency>
        get() = preferences.getEnum(PERIODIC_TRIM_FREQUENCY_KEY, PERIODIC_TRIM_FREQUENCY_DEFAULT, Frequency::class.java)

    private val selectedPartitions: Preference<Set<String>>
        get() = preferences.getStringSet(
            PERIODIC_TRIM_PARTITIONS_KEY,
            PERIODIC_TRIM_PARTITIONS_DEFAULT.map { it.directory }.toSet()
        )

    override fun getPeriodicTrimFrequency(): Flowable<Frequency> {
        return trimFrequency.asObservable().toFlowable(BackpressureStrategy.LATEST)
    }

    override fun setPeriodicTrimFrequency(frequency: Frequency): Completable {
        return Completable.fromAction { trimFrequency.set(frequency) }
    }


    override fun getSelectedPartitions(): Flowable<List<Partition>> {
        return selectedPartitions.asObservable().toFlowable(BackpressureStrategy.LATEST)
            .map { it.toList().map { partitionString -> partitionString.toPartition()!! } }
    }

    override fun setSelectedPartitions(partitions: List<Partition>): Completable {
        return Completable.fromAction { selectedPartitions.set(partitions.map { it.directory }.toSet()) }
    }

    companion object {

        const val PERIODIC_TRIM_FREQUENCY_KEY = "PERIODIC_TRIM_FREQUENCY_KEY"
        val PERIODIC_TRIM_FREQUENCY_DEFAULT = Frequency.WEEKLY

        const val PERIODIC_TRIM_PARTITIONS_KEY = "PERIODIC_TRIM_PARTITIONS_KEY"
        val PERIODIC_TRIM_PARTITIONS_DEFAULT = setOf(Partition.Cache, Partition.Data)
    }
}
