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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme
import com.gabchmel.sensorprocessor.SensorProcessService
import kotlinx.coroutines.flow.MutableStateFlow

class SensorValuesFragment : Fragment() {

    private var sensorProcessService = MutableStateFlow<SensorProcessService?>(null)

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get SensorProcessService instance
            val binder = service as SensorProcessService.LocalBinder
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
                                        "Sensor values",
                                        color = materialYel400,
                                        fontSize = 18.sp,
                                    )
                                })
                        },
                        content = {
                            Column {

                                sensorProcessService?.let { sensorProcessService ->
                                    val location by sensorProcessService.location.collectAsState(
                                        null
                                    )
                                    val time by sensorProcessService.time.collectAsState(null)

                                    Column {
                                        Text(text = "Current location: ")

                                        // Get the current location
                                        val text = location?.let { location ->
                                            "${location.latitude}, ${location.longitude}"
                                        } ?: "Null"

                                        Text(text = text)
                                    }

                                    Column {
                                        Text(text = "Current time: ")

                                        // Get the current location
                                        val text = time?.toString() ?: "Null"

                                        Text(text = text)
                                    }
                                }
                            }
                        }
                    )
                }
            }
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