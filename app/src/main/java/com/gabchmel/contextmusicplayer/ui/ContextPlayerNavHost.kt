package com.gabchmel.contextmusicplayer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData.CollectedSensorDataScreen
import com.gabchmel.contextmusicplayer.ui.screens.nowPlayingScreen.NowPlayingScreen
import com.gabchmel.contextmusicplayer.ui.screens.onDeviceSensors.OnDeviceSensorsScreen
import com.gabchmel.contextmusicplayer.ui.screens.playlistScreen.SongListScreen
import com.gabchmel.contextmusicplayer.ui.screens.settingsScreen.SettingsScreen

@Composable
fun ContextPlayerNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            SongListScreen(
                navController = navController
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController
            )
        }
        composable("on_device_sensors") {
            OnDeviceSensorsScreen(
                navController = navController
            )
        }
        composable("collected_sensor_data") {
            CollectedSensorDataScreen(
                navController = navController,
                collectedSensorData = ConvertedData()
            )
        }
        composable("now_playing") {
            NowPlayingScreen(
                navController = navController
            )
        }
    }
}