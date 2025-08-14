package com.gabchmel.contextmusicplayer.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation.NowPlayingScreen
import com.gabchmel.contextmusicplayer.playback.nowPlaying.presentation.NowPlayingViewModel.Factory
import com.gabchmel.contextmusicplayer.playlist.presentation.SongListScreen
import com.gabchmel.contextmusicplayer.settings.presentation.general.SettingsScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensorData.CollectedSensorDataScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensors.OnDeviceSensorsScreen
import com.gabchmel.contextmusicplayer.ui.navigation.ContextPlayerDestination.NowPlaying

@Composable
fun ContextPlayerNavDisplay() {
    val backstack = rememberNavBackStack(ContextPlayerDestination.SongList)

    fun NavBackStack.popBackStackSafe(): NavKey? {
        return if (size > 1) backstack.removeLastOrNull() else null
    }

    NavDisplay(
        backStack = backstack,
        onBack = { backstack.popBackStackSafe() },
        entryProvider = { key ->
            val key =  key as ContextPlayerDestination
            when (key) {
                ContextPlayerDestination.SongList -> NavEntry(key = key) {
                    SongListScreen(
                        navigateToNowPlaying = { songUri ->
                            backstack.add(NowPlaying(songUri))
                        },
                        navigateToSettings = {
                            backstack.add(ContextPlayerDestination.Settings)
                        }
                    )
                }

                is NowPlaying -> NavEntry(key = key) {
                    val app = LocalContext.current as Application

                    NowPlayingScreen(
                        viewModel = viewModel(factory = Factory(key.songUri, app)),
                        navigateToSettings = {
                            backstack.add(ContextPlayerDestination.Settings)
                        },
                        popBackStack = {
                            backstack.removeLastOrNull()
                        }
                    )
                }

                 ContextPlayerDestination.Settings -> NavEntry(key = key) {
                    SettingsScreen(
                        navigateToOnDeviceSensors = {
                            backstack.add(ContextPlayerDestination.OnDeviceSensors)
                        },
                        navigateToCollectedSensorData = {
                            backstack.add(ContextPlayerDestination.CollectedSensorData)
                        },
                        popBackStack = {
                            backstack.popBackStackSafe()
                        }
                    )
                }

                 ContextPlayerDestination.OnDeviceSensors -> NavEntry(key = key) {
                    OnDeviceSensorsScreen(
                        popBackStack = {
                            backstack.popBackStackSafe()
                        }
                    )
                }

                 ContextPlayerDestination.CollectedSensorData -> NavEntry(key = key) {
                    CollectedSensorDataScreen(
                        viewModel = viewModel(),
                        popBackStack = {
                            backstack.popBackStackSafe()
                        }
                    )
                }
            }
        }
    )
}