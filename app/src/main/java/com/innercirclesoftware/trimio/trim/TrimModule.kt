package com.innercirclesoftware.trimio.trim

import dagger.Binds
import dagger.Module

@Module
abstract class TrimModule {

    @Binds
    internal abstract fun bindsTrimmer(trimmerImpl: TrimmerImpl): Trimmer

}