package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.components.NavigationTopAppBar
import com.gabchmel.sensorprocessor.data.model.MeasuredSensorValues
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectedSensorDataScreen(
    navController: NavHostController,
    data: MeasuredSensorValues
) {
    val context = LocalContext.current
    var sensorDataProcessingService: SensorDataProcessingService? by remember {
        mutableStateOf(null)
    }
    val collectedSensorData: MeasuredSensorValues = sensorDataProcessingService?.measuredSensorValues?.collectAsStateWithLifecycle()?.value
        ?: MeasuredSensorValues()

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
                    collectedSensorData.lightSensorValue
                )
                SensorRow(
                    "Pressure",
                    collectedSensorData.pressure
                )
                SensorRow(
                    "Temperature",
                    collectedSensorData.temperature
                )
                SensorRow(
                    "Humidity",
                    collectedSensorData.humidity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_device_position)
                )
                SensorRow(
                    "Is device lying",
                    if (collectedSensorData.isDeviceLying == 0.0f) "No" else "Yes"
                )
                SensorRow(
                    "Proximity",
                    collectedSensorData.proximity
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_conected_devices)
                )
                SensorRow(
                    "Cable headphones connected",
                    if (collectedSensorData.isHeadphonesPluggedIn == true) "Yes" else "No"
                )
                SensorRow(
                    "Bluetooth headphones connected",
                    if (collectedSensorData.isBluetoothDeviceConnected == true) "Yes" else "No"
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_network)
                )
                SensorRow(
                    "Hashed WiFi name",
                    collectedSensorData.wifiSsid
                )
                SensorRow(
                    "Connection type",
                    collectedSensorData.networkConnectionType
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_battery)
                )
                SensorRow(
                    "Battery status",
                    collectedSensorData.batteryStatus.toString()
                )
                SensorRow(
                    "Type of charger",
                    collectedSensorData.chargingType.toString()
                )
                CategoryTitle(
                    text = stringResource(id = R.string.collected_data_title_health)
                )
                SensorRow(
                    "Heart Beat",
                    collectedSensorData.heartBeat
                )
                SensorRow(
                    "Heart Rate",
                    collectedSensorData.heartRate
                )
            }
        }
    )
}