package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
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
import com.gabchmel.sensorprocessor.SensorProcessor

class SensorValuesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sensorProcessor = SensorProcessor(requireContext())

        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {

                    val materialYel400 = MaterialTheme.colors.onPrimary

                    val scaffoldState =
                        rememberScaffoldState(rememberDrawerState(DrawerValue.Open))

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

                            val location by sensorProcessor.location.collectAsState(null)

                            Column {
                                Text(text = "Current location: ")

                                // Get the current location
                                val text = location?.let { location ->
                                    "${location.latitude}, ${location.longitude}"
                                } ?: "Null"

                                Text(text = text)
                            }
                        }
                    )
                }
            }
        }
    }
}