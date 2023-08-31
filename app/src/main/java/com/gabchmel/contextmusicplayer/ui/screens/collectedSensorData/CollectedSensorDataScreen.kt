package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DrawerValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.data.LocalBinder
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.ui.theme.appFontFamily
import com.gabchmel.sensorprocessor.data.service.SensorProcessService


@Composable
fun CollectedSensorDataScreen(
    navController: NavHostController
) {
    var sensorProcessService: SensorProcessService? by remember { mutableStateOf(null) }
    // Saved new collected sensor data
    var convertedData = ConvertedData()

    // Callbacks for service binding
    val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to SensorProcessService, cast the IBinder and get SensorProcessService instance
            val binder = service as LocalBinder<SensorProcessService>
            sensorProcessService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

    Scaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Collected sensor values",
                        fontFamily = appFontFamily
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                            contentDescription = "Back",
                            modifier = Modifier.fillMaxHeight(0.4f),
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                },
                elevation = 0.dp,
                backgroundColor = Color.Transparent
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sensorProcessService?.let { sensorProcessService ->
                    val sensorData by
                    sensorProcessService.measuredSensorValues.collectAsState(null)

                    convertedData.let { value ->
                        SensorRow(
                            "Current Activity",
                            value.state
                        )
                        SensorRow(
                            "Ambient light",
                            value.lightSensorValue
                        )
                        SensorRow(
                            "Is device lying",
                            if (value.deviceLying == 0.0f) "No" else "Yes"
                        )
                        SensorRow(
                            "Are cable headphones connected",
                            if (value.headphonesPluggedIn == 0.0f) "No" else "Yes"
                        )
                        SensorRow(
                            "Are bluetooth headphones connected",
                            if (value.BTDeviceConnected == 0.0f) "No" else "Yes"
                        )
                        SensorRow(
                            "Pressure",
                            value.pressure
                        )
                        SensorRow(
                            "Temperature",
                            value.temperature
                        )
                        SensorRow(
                            "Hashed WiFi name",
                            value.wifi
                        )
                        SensorRow(
                            "Connection type",
                            value.connection
                        )
                        SensorRow(
                            "Battery status",
                            value.batteryStatus
                        )
                        SensorRow(
                            "Type of charger",
                            value.chargingType
                        )
                        SensorRow(
                            "Proximity",
                            value.proximity
                        )
                        SensorRow(
                            "Humidity",
                            value.humidity
                        )
                        SensorRow(
                            "Heart Beat",
                            value.heartBeat
                        )
                        SensorRow(
                            "Heart Rate",
                            value.heartRate
                        )
                        SensorRow(
                            "Location cluster",
                            value.locationCluster
                        )
                    }
                }
            }
        }
    )
}