package com.innercirclesoftware.trimio.trim

import com.github.ajalt.timberkt.Timber
import eu.chainfire.libsuperuser.Shell
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface Trimmer {

    //TODO convert to Observable<Progress>
    fun trim(partition: Partition): Completable

    fun trimAll(): Completable {
        val trimCache = trim(Partition.Cache)
        val trimData = trim(Partition.Data)
        val trimSystem = trim(Partition.System)

        return Completable.concat(listOf(trimCache, trimData, trimSystem))
    }
}

class TrimmerImpl : Trimmer {

    override fun trim(partition: Partition): Completable {
        return Single
            .fromCallable { "fstrim -v ${partition.directory}" }
            .subscribeOn(Schedulers.io()) //TODO determine which Thread to use
            .doOnSuccess { Timber.v { "Executing \"$it\"" } }
            .map { Shell.SU.run(it) }
            .doOnSuccess { Timber.v { "Finished. Result=\"$it\"" } }
            .ignoreElement()
    }
}