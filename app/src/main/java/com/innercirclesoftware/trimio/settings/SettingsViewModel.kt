package com.innercirclesoftware.trimio.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.periodic.Frequency
import com.innercirclesoftware.trimio.trim.periodic.TrimScheduler
import com.innercirclesoftware.trimio.ui.base.BaseViewModel
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SettingsViewModel @Inject constructor(preferenceManager: PreferenceManager, trimScheduler: TrimScheduler) :
    BaseViewModel() {

    private val _frequency = MutableLiveData<Frequency>()
    val frequency: LiveData<Frequency>
        get() = _frequency

    private val _partitions = MutableLiveData<List<Partition>>()
    val partitions: LiveData<List<Partition>>
        get() = _partitions

    private val selectedFrequenciesQueue = PublishSubject.create<Frequency>()
    private val selectedPartitionsQueue = PublishSubject.create<List<Partition>>()

    init {
        preferenceManager.getPeriodicTrimFrequency()
            .subscribeOn(Schedulers.io())
            .subscribeBy { _frequency.postValue(it) }
            .disposeOnCleared()

        preferenceManager.getSelectedPartitions()
            .subscribeOn(Schedulers.io())
            .subscribeBy { _partitions.postValue(it) }
            .disposeOnCleared()

        selectedFrequenciesQueue
            .observeOn(Schedulers.io())
            .concatMapCompletable { preferenceManager.setPeriodicTrimFrequency(it) }
            .subscribe()
            .disposeOnCleared()

        selectedPartitionsQueue
            .observeOn(Schedulers.io())
            .concatMapCompletable { preferenceManager.setSelectedPartitions(it) }
            .subscribe()
            .disposeOnCleared()

        trimScheduler.schedule()
            .subscribeOn(Schedulers.io())
            .subscribe()
            .disposeOnCleared()
    }

    fun onFrequencySelected(frequency: Frequency) {
        selectedFrequenciesQueue.onNext(frequency)
    }

    fun onPartitionsSelected(partitions: List<Partition>) {
        selectedPartitionsQueue.onNext(partitions)
    }
}