package com.gabchmel.contextmusicplayer.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation.NowPlayingScreen
import com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation.NowPlayingViewModel
import com.gabchmel.contextmusicplayer.playlist.presentation.SongListScreen
import com.gabchmel.contextmusicplayer.settings.presentation.general.SettingsScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensorData.CollectedSensorDataScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensors.OnDeviceSensorsScreen
import kotlinx.serialization.Serializable

@Serializable
data object SongList : NavKey

@Serializable
data class NowPlaying(val songUri: String) : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object OnDeviceSensors : NavKey

@Serializable
data object CollectedSensorData : NavKey

@Composable
fun ContextPlayerNavDisplay() {
    val backstack = rememberNavBackStack(SongList)

    NavDisplay(
        backStack = backstack,
        onBack = { backstack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is SongList -> NavEntry(key = key) {
                    SongListScreen(
                        navigateToNowPlaying = { route ->
                            backstack.add(route)
                        },
                        navigateToSettings = { route ->
                            backstack.add(route)
                        }
                    )
                }

                is NowPlaying -> NavEntry(key = key) {
                    val app = LocalContext.current as Application

                    NowPlayingScreen(
                        viewModel = viewModel(factory = NowPlayingViewModel.Factory(key, app)),
                        navigateToSettings = { route ->
                            backstack.add(route)
                        },
                        popBackStack = {
                            backstack.removeLastOrNull()
                        }
                    )
                }

                is Settings -> NavEntry(key = key) {
                    SettingsScreen(
                        navigateToOnDeviceSensors = { route ->
                            backstack.add(route)
                        },
                        navigateToCollectedSensorData = { route ->
                            backstack.add(route)
                        },
                        popBackStack = {
                            backstack.removeLastOrNull()
                        }
                    )
                }

                is OnDeviceSensors -> NavEntry(key = key) {
                    OnDeviceSensorsScreen(
                        popBackStack = {
                            backstack.removeLastOrNull()
                        }
                    )
                }

                is CollectedSensorData -> NavEntry(key = key) {
                    CollectedSensorDataScreen(
                        viewModel = viewModel(),
                        popBackStack = {
                            backstack.removeLastOrNull()
                        }
                    )
                }

                else -> throw IllegalArgumentException("Unknown key: $key")
            }
        }
    )
}