package com.innercirclesoftware.trimio.trim

sealed class Partition(val directory: String) {

    object Cache : Partition("/cache")
    object Data : Partition("/data")
    object System : Partition("/system") //Usually read only so not needed

}


fun String.toPartition(): Partition? {
    return when (this) {
        Partition.Cache.directory -> Partition.Cache
        Partition.Data.directory -> Partition.Data
        Partition.System.directory -> Partition.System
        else -> null
    }
}
