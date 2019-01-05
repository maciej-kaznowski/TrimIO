package com.innercirclesoftware.trimio.trim.periodic

import android.os.Build
import androidx.work.*
import com.github.ajalt.timberkt.Timber
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface TrimScheduler {

    fun schedule(): Flowable<Boolean>

}

class TrimSchedulerImpl @Inject constructor(
    private val workManager: Lazy<WorkManager>,
    private val periodicTrimPreferences: PeriodicTrimPreferences
) : TrimScheduler {

    private val constraints: Constraints
        get() = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    setRequiresDeviceIdle(true)
                }
            }
            .build()

    private fun getWorkRequest(frequency: Frequency): PeriodicWorkRequest {
        return frequency.repeat.let {
            PeriodicWorkRequestBuilder<TrimWork>(repeatInterval = it.interval, repeatIntervalTimeUnit = it.timeUnit)
                .setConstraints(constraints)
                .build()
        }

    }

    override fun schedule(): Flowable<Boolean> {
        return periodicTrimPreferences.getPeriodicTrimPreference()
            .switchMap {
                when (it.frequency) {
                    Frequency.NEVER -> unSchedule().andThen(Flowable.just(false))
                    else -> schedule(getWorkRequest(it.frequency)).andThen(Flowable.just(true))
                }
            }
    }

    private fun schedule(workRequest: PeriodicWorkRequest): Completable {
        return Completable
            .fromFuture(
                workManager.get().enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.REPLACE,
                    workRequest
                ).result
            )
            .doOnSubscribe { Timber.i { "Scheduling trim work" } }
    }

    private fun unSchedule(): Completable {
        return Completable
            .fromFuture(workManager.get().cancelUniqueWork(WORK_NAME).result)
            .doOnSubscribe { Timber.i { "UnScheduling trim work" } }
    }

    companion object {

        private const val WORK_NAME = "com.innercirclesoftware.trimio.trim.periodic.TrimScheduler"

    }
}

private val Frequency.repeat: Repeat
    get() {
        return when (this) {
            Frequency.NEVER -> Repeat(Long.MAX_VALUE, TimeUnit.DAYS)
            Frequency.DAILY -> Repeat(1, TimeUnit.DAYS)
            Frequency.WEEKLY -> Repeat(7, TimeUnit.DAYS)
            Frequency.FORTNIGHTLY -> Repeat(14, TimeUnit.DAYS)
            Frequency.MONTHLY -> Repeat(30, TimeUnit.DAYS)
        }
    }

data class Repeat(val interval: Long, val timeUnit: TimeUnit)
