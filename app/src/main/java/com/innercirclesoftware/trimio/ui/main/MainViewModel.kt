package com.innercirclesoftware.trimio.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.ui.base.BaseViewModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainViewModel : BaseViewModel() {

    private var trimState = MutableLiveData<TrimStatus>()

    init {
        trimState.value = TrimStatus.Sleeping

        val postTrimmingCacheState = Completable.fromAction {
            val current = Partition.Cache
            val queue = listOf(Partition.Data, Partition.Cache, Partition.System)

            trimState.postValue(TrimStatus.Trimming(queue, current))
        }

        Completable.timer(5, TimeUnit.SECONDS).andThen(postTrimmingCacheState).subscribeOn(Schedulers.io()).subscribe()
    }

    fun getTrimStatus(): LiveData<TrimStatus> = trimState

}

sealed class TrimStatus {

    object Sleeping : TrimStatus()
    class Trimming(val partitions: List<Partition>, val current: Partition) : TrimStatus()

}