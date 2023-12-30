package com.gabchmel.common.data

enum class UserActivity {
    STILL,
    WALKING,
    RUNNING,
    ON_BICYCLE,
    IN_VEHICLE,
    UNKNOWN
    ;

    companion object {
        fun getEntriesString(): String {
            return entries.toTypedArray().joinToString()
        }
    }
}