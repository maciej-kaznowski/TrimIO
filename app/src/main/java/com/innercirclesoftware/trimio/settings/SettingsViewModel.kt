package com.innercirclesoftware.trimio.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.periodic.Frequency
import com.innercirclesoftware.trimio.ui.base.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(preferenceManager: PreferenceManager) : BaseViewModel() {

    private val _frequency = MutableLiveData<Frequency>()
    val frequency: LiveData<Frequency>
        get() = _frequency

    private val _partitions = MutableLiveData<List<Partition>>()
    val partitions: LiveData<List<Partition>>
        get() = _partitions

    init {
        //TODO
        _frequency.value = Frequency.DAILY
        _partitions.value = listOf(Partition.Cache, Partition.Data, Partition.System)
    }
}