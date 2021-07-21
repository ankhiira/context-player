package com.gabchmel.contextmusicplayer.settingsScreen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gabchmel.common.ConvertedData
import com.gabchmel.common.LocalBinder
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.flow.MutableStateFlow

class CollectedSensorDataFragment : Fragment() {

    companion object {
        var convertedData = ConvertedData()
        fun updateUI(input: ConvertedData) {
            convertedData = input
        }
    }

    private var sensorProcessService = MutableStateFlow<SensorProcessService?>(null)

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to SensorProcessService, cast the IBinder and get SensorProcessService instance
            val binder = service as LocalBinder<SensorProcessService>
            sensorProcessService.value = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {

                    val materialYel400 = MaterialTheme.colors.onPrimary

                    val scaffoldState =
                        rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

                    val sensorProcessService by sensorProcessService.collectAsState()

                    Scaffold(
                        scaffoldState = scaffoldState,
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Collected sensor values",
                                        color = materialYel400,
                                        fontSize = 20.sp,
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { findNavController().navigateUp() }) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_back),
                                            contentDescription = "Back",
                                            modifier = Modifier.fillMaxHeight(0.4f),
                                            tint = materialYel400
                                        )
                                    }
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                sensorProcessService?.let { sensorProcessService ->
                                    val sensorData by
                                    sensorProcessService.sensorData.collectAsState(null)

                                    convertedData.let { value ->
                                        SensorRow("Time", "No")
                                        SensorRow("Current Activity", value.state!!)
                                        SensorRow("Ambient light", value.lightSensorValue!!)
                                        SensorRow(
                                            "Is device lying",
                                            if (value.deviceLying == 0.0f)
                                                "No"
                                            else {
                                                "Yes"
                                            }
                                        )
                                        SensorRow(
                                            "Are cable headphones connected",
                                            if (value.headphonesPluggedIn == 0.0f)
                                                "No"
                                            else {
                                                "Yes"
                                            }
                                        )
                                        SensorRow(
                                            "Are bluetooth headphones connected",
                                            if (value.BTdeviceConnected == 0.0f)
                                                "No"
                                            else {
                                                "Yes"
                                            }
                                        )
                                        SensorRow("Pressure", value.pressure!!)
                                        SensorRow("Temperature", value.temperature!!)
                                        SensorRow("Hashed WiFi name", value.wifi!!)
                                        SensorRow("Connection type", value.connection!!)
                                        SensorRow("Battery status", value.batteryStatus!!)
                                        SensorRow("Type of charger", value.chargingType)
                                        SensorRow("Proximity", value.proximity!!)
                                        SensorRow("Humidity", value.humidity!!)
                                        SensorRow("Heart Beat", value.heartBeat!!)
                                        SensorRow("Heart Rate", value.heartRate!!)
                                        SensorRow("Location cluster", value.locationCluster)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SensorRow(sensor: String, value: Any) {
        val materialYel400 = MaterialTheme.colors.onPrimary
        Row() {
            Text(
                text = "$sensor: ",
                color = materialYel400,
            )
            Text(
                text = "$value",
            )
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to SensorProcessService
        val intent = Intent(requireActivity(), SensorProcessService::class.java)
        requireActivity().applicationContext.bindService(
            intent, connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().applicationContext.unbindService(connection)
    }
}