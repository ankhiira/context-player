package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.theme.JetnewsTheme

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                JetnewsTheme {

                    val materialYel400 = MaterialTheme.colors.onPrimary

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Settings",
                                        color = materialYel400,
                                        fontSize = 18.sp,
                                    )
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent
                            )
                        },
                        content = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Available sensors",
                                    color = materialYel400,
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToSensorScreen()
                                            )
                                        })
                                )
                                Text(
                                    text = "Sensor values",
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToSensorValuesFragment()
                                            )
                                        })
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}