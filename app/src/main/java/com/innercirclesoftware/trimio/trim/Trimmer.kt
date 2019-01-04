package com.innercirclesoftware.trimio.trim

import android.content.Context
import android.content.res.AssetManager
import com.github.ajalt.timberkt.Timber
import eu.chainfire.libsuperuser.Shell
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


interface Trimmer {

    fun trim(partition: Partition): Single<TrimResult>

}

sealed class TrimResult(open val partition: Partition) {

    data class Success(override val partition: Partition, val trimmedBytes: Long) : TrimResult(partition)
    data class Failure(override val partition: Partition, val throwable: Throwable) : TrimResult(partition)

}

internal class TrimmerImpl @Inject constructor(private val assetManager: AssetManager, val context: Context) : Trimmer {

    private val toPath = context.filesDir.path
    private val fstrimPath = "$toPath/$fstrimFileName"

    private val createFstrimFile: Completable by lazy {
        Completable.fromAction { copyFstrimAsset() }.andThen(makeFstrimFileExecutable).cache()
    }

    private val checkSuAvailable: Completable by lazy {
        Completable.fromAction {
            if (!Shell.SU.available()) {
                throw SuNotAvailableException("SuperUser is not available")
            }
        }
    }

    private val makeFstrimFileExecutable: Completable by lazy {
        Completable.fromAction {
            val chmodCmd = "chmod +x $fstrimPath"
            Timber.v { "Making fstrim file executable: \'$chmodCmd\'" }
            Shell.SU.run(chmodCmd)
        }
    }

    private fun copyFstrimAsset() {
        val fstrimFile = File(fstrimPath)
        if (fstrimFile.exists()) {
            Timber.v { "fstrim file already exists, not copying from APK assets" }
            return
        }

        Timber.v { "Copying fstrim from APK assets to file=\'$fstrimPath\'" }
        val fileCreated = fstrimFile.createNewFile()
        Timber.v { "fstrimPath=$fstrimPath created=$fileCreated" }

        val output: OutputStream = FileOutputStream(fstrimPath)
        val input: InputStream = assetManager.open(fstrimFileName)
        copyFile(input, output)

        input.close()
        output.flush()
        output.close()
    }

    private fun copyFile(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int = input.read(buffer)
        while ((read) != -1) {
            output.write(buffer, 0, read)
            read = input.read(buffer)
        }
    }

    override fun trim(partition: Partition): Single<TrimResult> {
        val trim = Single.just("$fstrimPath -v ${partition.directory}")
            .subscribeOn(Schedulers.io()) //TODO determine which Thread to use
            .doOnSuccess { Timber.v { "Executing \"$it\"" } }
            .map { exec(it) }
            .map { getTrimmedBytes(partition, it) }
            .map { TrimResult.Success(partition, it) }
            .cast(TrimResult::class.java)
            .onErrorReturn { TrimResult.Failure(partition, it) }
            .doOnSuccess { Timber.v { "Finished. Result=\"$it\"" } }

        return checkSuAvailable
            .andThen(createFstrimFile)
            .andThen(trim)
            .onErrorReturn { TrimResult.Failure(partition, it) }
    }

    private fun getTrimmedBytes(partition: Partition, output: List<String>): Long {
        if (output.size != 1) throw TrimException.UnexpectedOutput(output, partition)

        /*
        Example:
        /cache: 0 bytes trimmed
        */
        val text = output.first()
        try {
            return text.substringAfter("${partition.directory}: ").substringBefore(" bytes trimmed").toLong()
        } catch (exception: Exception) {
            throw TrimException.InvalidOutput(text, partition, exception)
        }
    }

    private fun exec(cmd: String): List<String> {
        val output = mutableListOf<String>()
        val errors = mutableListOf<String>()

        val shell = Shell.Builder().useSU()
            .addCommand(cmd)
            .setWantSTDERR(true)
            .setOnSTDERRLineListener { errors.add(it) }
            .setOnSTDOUTLineListener { output.add(it) }
            .open()

        shell.waitForIdle()

        if (errors.isNotEmpty()) {
            val message = "Errors executing \'$cmd\':\n" + errors.joinToString(separator = "\n")
            throw IllegalStateException(message)
        }

        return output
    }

    companion object {
        private const val fstrimFileName = "fstrim"
    }
}


sealed class TrimException(msg: String, partition: Partition) : RuntimeException(msg) {

    class UnexpectedOutput(val output: List<String>, partition: Partition) : TrimException(
        "Expected 1 line. Unexpected output \"${output.joinToString(separator = ",\n", prefix = "[", postfix = "]")}\"",
        partition
    )

    class InvalidOutput(val output: String, partition: Partition, cause: Exception) : TrimException(
        "Could not determine number of trimmed bytes in \"$output\": ${cause.message}",
        partition
    )
}

class SuNotAvailableException(msg: String) : RuntimeException(msg)