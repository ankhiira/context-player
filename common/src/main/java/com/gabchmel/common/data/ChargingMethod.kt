package com.gabchmel.common.data

enum class ChargingMethod {
    USB,
    AC,
    WIRELESS,
    NONE
    ;
    companion object {
        fun getEntriesString(): String {
            return entries.toTypedArray().joinToString()
        }
    }
}