package com.innercirclesoftware.trimio.trim

import android.content.Context
import android.content.res.AssetManager
import com.github.ajalt.timberkt.Timber
import eu.chainfire.libsuperuser.Shell
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject


interface Trimmer {

    //TODO convert to Observable<Progress>
    fun trim(vararg partitions: Partition): Completable

    fun trimAll(): Completable {
        val trimCache = trim(Partition.Cache)
        val trimData = trim(Partition.Data)
        val trimSystem = trim(Partition.System)

        return Completable.concat(listOf(trimCache, trimData, trimSystem))
    }
}

internal class TrimmerImpl @Inject constructor(private val assetManager: AssetManager, val context: Context) : Trimmer {

    private val toPath = context.filesDir.path
    private val fstrimPath = "$toPath/$fstrimFileName"

    private val createFstrimFile: Completable by lazy {
        Completable.fromAction { copyFstrimAsset() }.andThen(makeFstrimFileExecutable).cache()
    }

    private val makeFstrimFileExecutable: Completable by lazy {
        Completable.fromAction {
            val chmodCmd = "chmod +x $fstrimPath"
            Timber.v { "Making fstrim file executable: \'$chmodCmd\'" }
            Shell.SU.run(chmodCmd)
        }
    }

    private fun copyFstrimAsset() {
        Timber.v { "Copying fstrim from APK assets to file=\'$fstrimPath\'" }
        val fileCreated = File(fstrimPath).createNewFile()
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

    override fun trim(vararg partitions: Partition): Completable {
        val trim = Observable.fromArray(*partitions).flatMapCompletable { trim(it) }

        return createFstrimFile.andThen(trim)
    }

    private fun trim(partition: Partition): Completable {
        return Single.just("$fstrimPath -v ${partition.directory}")
            .subscribeOn(Schedulers.io()) //TODO determine which Thread to use
            .doOnSuccess { Timber.v { "Executing \"$it\"" } }
            .map { exec(it) }
            .doOnSuccess { Timber.v { "Finished. Result=\"$it\"" } }
            .ignoreElement()
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