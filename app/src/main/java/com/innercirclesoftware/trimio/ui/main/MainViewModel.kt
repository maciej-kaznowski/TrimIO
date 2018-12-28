package com.innercirclesoftware.trimio.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ajalt.timberkt.Timber
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.Trimmer
import com.innercirclesoftware.trimio.ui.base.BaseViewModel
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class MainViewModel @Inject constructor(trimmer: Trimmer) : BaseViewModel() {

    private var trimState = MutableLiveData<TrimStatus>()
    private val trimQueue = PublishSubject.create<TrimRequest>()
    private val trimProgress = AtomicInteger()

    init {
        trimState.value = TrimStatus.Sleeping

        trimQueue.observeOn(Schedulers.io())
            .map { it.partitions }
            .map { it.toTypedArray() }
            .doOnNext { Timber.i { "Received TrimRequest=$it" } }
            .doOnNext {
                val trimCounter = trimProgress.incrementAndGet()
                Timber.v { "Trim counter is at $trimCounter" }
            }
            .concatMap {
                trimState.postValue(TrimStatus.Trimming(it.toList(), it.first()))
                trimmer.trim(*it)
                    .doOnComplete{onTrimComplete(it)}
                    .toObservable<Any>()
            }
            .subscribe()
            .disposeOnCleared()
    }

    private fun onTrimComplete(partitions: Array<Partition>) {
        val trimCounter = trimProgress.decrementAndGet()
        Timber.v { "Trim counter is at $trimCounter" }

        if (trimCounter == 0) {
            trimState.postValue(TrimStatus.Sleeping)
        }
    }

    fun getTrimStatus(): LiveData<TrimStatus> = trimState
    fun trim(request: TrimRequest) = trimQueue.onNext(request)

}

sealed class TrimStatus {

    object Sleeping : TrimStatus()
    class Trimming(val partitions: List<Partition>, val current: Partition) : TrimStatus()

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