package com.innercirclesoftware.trimio.trim

sealed class Partition(val directory: String) {

    object Cache : Partition("/cache")
    object Data : Partition("/data")
    object System : Partition("/system")

}