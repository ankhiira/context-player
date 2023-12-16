package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.gabchmel.common.data.SensorValues
import com.gabchmel.common.data.dataStore.DataStore
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.components.NavigationTopAppBar
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService

@Composable
fun CollectedSensorDataScreen(
    navController: NavHostController,
    data: SensorValues
) {
    val context = LocalContext.current
    var sensorDataProcessingService: SensorDataProcessingService? by remember {
        mutableStateOf(null)
    }
//    val sensorData: SensorValues =
//        sensorDataProcessingService?.sensorValues?.collectAsStateWithLifecycle()?.value
//        ?: SensorValues()

    val sensorData by DataStore.getSensorDataFlow(context).collectAsStateWithLifecycle(initialValue = SensorValues())

    LaunchedEffect(key1 = Unit) {
        sensorDataProcessingService =
            context.bindService(SensorDataProcessingService::class.java)
    }

    Scaffold(
        topBar = {
            NavigationTopAppBar(
                title = stringResource(id = R.string.settings_item_data),
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_result_category)
                )
//                SensorRow(
//                    "Result category",
//                    collectedSensorData.locationCluster
//                )

                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_activity)
                )
//                SensorRow(
//                    "Current Activity",
//                    collectedSensorData.currentActivity
//                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_physical_values)
                )
                SensorRow(
                    "Ambient light",
                    sensorData.lightSensorValue
                )
                SensorRow(
                    "Temperature",
                    sensorData.temperature
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_device_position)
                )
                SensorRow(
                    "Is device lying",
                    if (sensorData.isDeviceLying == true) "Yes" else "No"
                )
                SensorRow(
                    "Proximity",
                    sensorData.proximity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_conected_devices)
                )
                SensorRow(
                    "Cable headphones connected",
                    if (sensorData.isHeadphonesPluggedIn == true) "Yes" else "No"
                )
                SensorRow(
                    "Bluetooth headphones connected",
                    if (sensorData.isBluetoothDeviceConnected == true) "Yes" else "No"
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_network)
                )
                SensorRow(
                    "Hashed WiFi name",
                    sensorData.wifiSsid
                )
                SensorRow(
                    "Connection type",
                    sensorData.networkConnectionType
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_battery)
                )
                //TODO convert data to string elsewhere
                SensorRow(
                    "Is device charging",
                    if (sensorData.isDeviceCharging == true) "Yes" else "No"
                )
                SensorRow(
                    "Type of charger",
                    sensorData.chargingType.toString()
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_health)
                )
                SensorRow(
                    "Heart Rate",
                    sensorData.heartRate
                )
            }
        }
    )
}