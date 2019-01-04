package com.innercirclesoftware.trimio.trim.periodic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.format.Formatter
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import com.github.ajalt.timberkt.Timber
import com.innercirclesoftware.trimio.R
import com.innercirclesoftware.trimio.trim.Partition
import com.innercirclesoftware.trimio.trim.TrimResult
import com.innercirclesoftware.trimio.trim.Trimmer
import io.reactivex.*

class TrimWork constructor(
    private val trimmer: Trimmer,
    private val periodicTrimPreferences: PeriodicTrimPreferences,
    context: Context,
    workerParams: WorkerParameters
) :
    RxWorker(context, workerParams) {

    private val notificationManagerCompat = NotificationManagerCompat.from(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun createWork(): Single<Result> {
        return getPartitions()
            .flatMap { trimmer.trim(it).toFlowable() }
            .doOnNext { Timber.v { "Finished trimming. TrimResult=$it" } }
            .toList()
            .flatMap { showNotifications(it) }
            .doOnSubscribe { Timber.v { "Starting TrimWork" } }
            .doOnSuccess { Timber.v { "Finished work with result $it" } }
    }

    private fun getPartitions(): Flowable<Partition> {
        return periodicTrimPreferences.getPeriodicTrimPreference()
            .map {
                if (it.frequency == Frequency.NEVER) {
                    Timber.v { "Frequency is NEVER. Running work on empty partition list" }
                    return@map emptyList<Partition>()
                }

                Timber.v { "Frequency is ${it.frequency}. Running work on ${it.partitions.joinToString()}" }
                return@map it.partitions
            }
            .firstOrError()
            .flatMapObservable { Observable.fromIterable(it) }
            .toFlowable(BackpressureStrategy.BUFFER)
    }

    private fun showNotifications(results: List<TrimResult>): Single<Result> {
        if (results.isEmpty()) {
            //don't want to show a summary notification for nothing
            return Single.just(Result.success())
                .doOnSubscribe { Timber.v { "No results. Not showing notification" } }
        }


        val showNotifications = Flowable.fromIterable(results)
            .doOnNext { Timber.v { "Processing TrimResult=$it" } }
            .flatMapSingle { showNotifications(it) }
            .startWith(createSummaryNotification(results).toFlowable())
            .toList()
            .flatMap { showNotifications(it).toSingle { getWorkResult(results) } }

        return createNotificationChannel().andThen(showNotifications)
    }

    private fun createNotificationChannel(): Completable {
        return Completable.fromAction {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Timber.v { "Creating notification channel" }
                notificationManager.createNotificationChannel(getNotificationChannel())
            } else {
                Timber.v { "Not creating notification channel as current Build.Version.SDK_INT=${Build.VERSION.SDK_INT} does not support it" }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(): NotificationChannel {
        return NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_MIN
        )
    }

    private fun getWorkResult(results: List<TrimResult>): Result {
        if (results.any { it is TrimResult.Failure }) {
            Timber.i { "Failures in trim results: results=${results.joinToString()}" }
            //TODO it would be a good idea to recover from trim failures. E.g. if not rooted, then retry but show a notification?
            return Result.failure()
        }

        Timber.v { "All TrimResult's are successful" }
        return Result.success()
    }

    private fun createSummaryNotification(list: List<TrimResult>): Single<NotificationRequest> {
        return Single
            .fromCallable {
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(list.contentTitle)
                    .setContentText(list.contentText)
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .also {
                                list.map { result -> result.contentTitle }.forEach { title -> it.addLine(title) }
                                it.setBigContentTitle(list.contentTitle)
                                it.setSummaryText(list.contentText)
                            }
                    )
                    .setSmallIcon(R.drawable.memory)
                    .setGroupSummary(true)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
                    .setGroup(NOTIFICATION_GROUP_TRIM)
                    .build()
            }
            .map { NotificationRequest(NOTIFICATION_ID_SUMMARY, it) }
    }

    private fun showNotifications(result: TrimResult): Single<NotificationRequest> {
        return Single
            .fromCallable {
                NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(result.contentTitle)
                    .setContentText(result.contentText)
                    .setSmallIcon(R.drawable.memory)
                    .setGroup(NOTIFICATION_GROUP_TRIM)
                    .build()
            }
            .map { NotificationRequest(getId(result), it) }
    }

    private fun getId(result: TrimResult): Int {
        return when (result.partition) {
            Partition.Cache -> NOTIFICATION_ID_CACHE
            Partition.Data -> NOTIFICATION_ID_DATA
            Partition.System -> NOTIFICATION_ID_SYSTEM
        }
    }

    private fun showNotifications(requests: List<NotificationRequest>): Completable {
        return Flowable.fromIterable(requests)
            .doOnNext { Timber.v { "Showing notification=$it" } }
            .concatMapCompletable {
                Completable.fromAction {
                    notificationManagerCompat.notify(
                        it.id,
                        it.notification
                    )
                }
            }
    }

    companion object {

        private const val NOTIFICATION_ID_SUMMARY = 1
        private const val NOTIFICATION_ID_DATA = 2
        private const val NOTIFICATION_ID_CACHE = 3
        private const val NOTIFICATION_ID_SYSTEM = 4
        private const val NOTIFICATION_GROUP_TRIM = "NOTIFICATION_GROUP_TRIM"

        private const val NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CHANNEL_ID"
        private const val NOTIFICATION_CHANNEL_NAME = "Periodic Trim"
    }

    private val TrimResult.contentText: CharSequence
        get() = when (this) {
            //TODO localize
            is TrimResult.Success -> {
                val formattedTrimmedBytes = Formatter.formatShortFileSize(applicationContext, trimmedBytes)
                "$formattedTrimmedBytes trimmed"
            }
            is TrimResult.Failure -> {
                "${throwable.message}"
            }
        }
}

private val List<TrimResult>.contentTitle: CharSequence
    get() {
        val hasFailed = any { it is TrimResult.Failure }
        val hasSucceeded = any { it is TrimResult.Success }

        return when {
            //TODO localize
            hasFailed && hasSucceeded -> "Failed to trim some partitions"
            hasFailed -> "Failed to trim all partitions"
            else -> "Successfully trimmed all partitions"
        }
    }

private val List<TrimResult>.contentText: CharSequence
    get() {
        val hasFailed = any { it is TrimResult.Failure }
        val hasSucceeded = any { it is TrimResult.Success }

        return when {
            //TODO localize
            hasFailed && hasSucceeded -> {
                val failedPartitions = filter { it is TrimResult.Failure }.map { it.partition.directory }
                val successfulPartitions = filter { it is TrimResult.Success }.map { it.partition.directory }
                return "Failed to trim ${failedPartitions.joinToString(separator = ", ")}\n" +
                        "Successfully trimmed ${successfulPartitions.joinToString(separator = ", ")}"
            }
            hasFailed -> "Failed to trim ${joinToString(separator = ", ") { it.partition.directory }}"
            else -> "Successfully trimmed ${joinToString(separator = ", ") { it.partition.directory }}"
        }
    }

private val TrimResult.contentTitle: CharSequence
    get() = when (this) {
        //TODO localize
        is TrimResult.Success -> "Trimmed ${partition.directory}"
        is TrimResult.Failure -> "Failed to trim ${partition.directory}"
    }

data class NotificationRequest(val id: Int, val notification: Notification)