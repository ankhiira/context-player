package com.gabchmel.common.data

enum class NetworkType {
    NONE,
    TRANSPORT_WIFI,
    CELLULAR
    ;
    companion object {
        fun getEntriesString(): String {
            return entries.toTypedArray().joinToString()
        }
    }
}