package com.innercirclesoftware.trimio.ui.base

import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseViewModel : ViewModel() {

    private val disposables = CompositeDisposable()

    fun Disposable.disposeOnCleared(): Disposable {
        //check if onCleared has already been called?
        disposables.add(this)
        return this
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}