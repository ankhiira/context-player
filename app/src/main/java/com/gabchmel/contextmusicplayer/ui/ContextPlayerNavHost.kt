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
import com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData.CollectedSensorDataScreen
import com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData.CollectedSensorDataViewModel
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
            val viewModel: CollectedSensorDataViewModel = viewModel()
            viewModel.getSensorData(LocalContext.current)
            val collectedSensorData = viewModel.sensorData?.collectAsStateWithLifecycle()

            CollectedSensorDataScreen(
                navController = navController,
                data = collectedSensorData?.value ?: SensorValues()
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