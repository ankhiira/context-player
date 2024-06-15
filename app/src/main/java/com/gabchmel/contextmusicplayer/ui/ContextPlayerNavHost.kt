package com.gabchmel.contextmusicplayer.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gabchmel.common.data.SensorValues
import com.gabchmel.contextmusicplayer.playback.presentation.nowPlaying.NowPlayingScreen
import com.gabchmel.contextmusicplayer.playlist.presentation.SongListScreen
import com.gabchmel.contextmusicplayer.settings.presentation.general.SettingsScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensorData.CollectedSensorDataScreen
import com.gabchmel.contextmusicplayer.settings.presentation.sensorData.CollectedSensorDataViewModel
import com.gabchmel.contextmusicplayer.settings.presentation.sensors.OnDeviceSensorsScreen

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
            val viewModel: CollectedSensorDataViewModel = viewModel()
            val collectedSensorData = viewModel.getSensorData(LocalContext.current)

            CollectedSensorDataScreen(
                navController = navController,
                sensorData = collectedSensorData.collectAsStateWithLifecycle(initialValue = SensorValues()).value
            )
        }
        composable(
            "now_playing/{uri}",
            arguments = listOf(
                navArgument("uri") { type = NavType.StringType }
            )
        ) {
            NowPlayingScreen(
                navController = navController
            )
        }
    }
}