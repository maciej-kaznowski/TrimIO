package com.innercirclesoftware.trimio.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.TrimResult
import com.innercirclesoftware.trimio.trim.Trimmer
import com.innercirclesoftware.trimio.ui.base.BaseViewModel
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MainViewModel @Inject constructor(trimmer: Trimmer) : BaseViewModel() {

    private val _cacheState = MutableLiveData<TrimStatus>()
    val cacheState: LiveData<TrimStatus>
        get() = _cacheState

    private val _dataState = MutableLiveData<TrimStatus>()
    val dataState: LiveData<TrimStatus>
        get() = _dataState

    private val _systemState = MutableLiveData<TrimStatus>()
    val systemState: LiveData<TrimStatus>
        get() = _systemState

    private val _trimming = MutableLiveData<Boolean>()
    private val activeTrimCounter = AtomicInteger()
    val trimming: LiveData<Boolean>
        get() = _trimming


    private val trimQueue = PublishSubject.create<TrimRequest>()

    init {
        _cacheState.value = TrimStatus.Sleeping
        _dataState.value = TrimStatus.Sleeping
        _systemState.value = TrimStatus.Sleeping
        _trimming.value = false

        trimQueue.observeOn(Schedulers.io())
            .doOnNext { Timber.i { "Received TrimRequest=$it" } }
            .concatMap { request ->
                Observable.fromIterable(request.partitions)
                    .doOnSubscribe { onTrimStarted(request) }
                    .flatMap { partition ->
                        trimmer.trim(partition)
                            .doOnSubscribe { onStartedTrimmingPartition(partition) }
                            .doOnSuccess { onFinishedTrimmingPartition(it) }
                            .toObservable()
                    }
                    .toList()
                    .doOnSuccess { onTrimFinished(request, it) }
                    .toObservable()
            }
            .subscribe()
            .disposeOnCleared()
    }

    private fun onTrimStarted(request: TrimRequest) {
        Timber.v { "onTrimStarted: request=$request" }
        activeTrimCounter.incrementAndGet()
        _cacheState.postValue(if (request.trimCache) TrimStatus.Trimming else TrimStatus.Sleeping)
        _dataState.postValue(if (request.trimData) TrimStatus.Trimming else TrimStatus.Sleeping)
        _systemState.postValue(if (request.trimSystem) TrimStatus.Trimming else TrimStatus.Sleeping)
        _trimming.postValue(true)
    }

    private fun onStartedTrimmingPartition(partition: Partition) {
        Timber.v { "onStartedTrimmingPartition: partition=$partition" }
        getState(partition).postValue(TrimStatus.Trimming)
    }

    private fun onFinishedTrimmingPartition(result: TrimResult) {
        Timber.v { "onFinishedTrimmingPartition: result=$result" }
        getState(result.partition).postValue(TrimStatus.Completed(result))
    }

    private fun onTrimFinished(request: TrimRequest, results: List<TrimResult>) {
        Timber.v { "onTrimFinished: request=$request, results=${results.joinToString()}" }
        _trimming.postValue(activeTrimCounter.decrementAndGet() != 0)
    }

    private fun getState(partition: Partition): MutableLiveData<TrimStatus> {
        return when (partition) {
            is Partition.Cache -> _cacheState
            is Partition.Data -> _dataState
            is Partition.System -> _systemState
        }
    }

    fun trim(request: TrimRequest) = trimQueue.onNext(request)

}

sealed class TrimStatus {

    object Sleeping : TrimStatus()
    object Trimming : TrimStatus()
    data class Completed(val result: TrimResult) : TrimStatus()

}

data class TrimRequest(
    val trimCache: Boolean = true,
    val trimData: Boolean = true,
    val trimSystem: Boolean = true
) {

    inline val partitions: List<Partition>
        get() {
            return mutableListOf<Partition>().apply {
                if (trimCache) add(Partition.Cache)
                if (trimData) add(Partition.Data)
                if (trimSystem) add(Partition.System)
            }
        }
}