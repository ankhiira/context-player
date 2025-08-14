package com.gabchmel.contextmusicplayer.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class ContextPlayerDestination: NavKey {
    @Serializable
    data object SongList : ContextPlayerDestination()

    @Serializable
    data class NowPlaying(val songUri: String) : ContextPlayerDestination()

    @Serializable
    data object Settings : ContextPlayerDestination()

    @Serializable
    data object OnDeviceSensors : ContextPlayerDestination()

    @Serializable
    data object CollectedSensorData : ContextPlayerDestination()
}