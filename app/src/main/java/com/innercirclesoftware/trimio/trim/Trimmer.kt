package com.innercirclesoftware.trimio.trim

import io.reactivex.Completable
import java.util.concurrent.TimeUnit

interface Trimmer {

    //TODO convert to Observable<Progress>
    fun trim(partition: Partition): Completable

}

class TrimmerImpl : Trimmer {

    //TODO implement trimming
    override fun trim(partition: Partition): Completable {
        return Completable.timer(5, TimeUnit.SECONDS)
    }

}