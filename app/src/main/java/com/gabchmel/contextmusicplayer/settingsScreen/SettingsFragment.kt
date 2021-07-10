package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gabchmel.contextmusicplayer.R
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
                                verticalArrangement = Arrangement.spacedBy(28.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToSensorScreen()
                                            )
                                        }),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "On device sensors",
                                        color = materialYel400,
                                        fontSize = 18.sp
                                        )
                                    Icon(
                                        imageVector = Icons.Filled.NavigateNext,
                                        contentDescription = "Settings",
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToSensorValuesFragment()
                                            )
                                        }),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Collected sensor data",
                                        color = materialYel400,
                                        fontSize = 18.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.NavigateNext,
                                        contentDescription = "Settings",
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToPredictionModelSettingsFragment()
                                            )
                                        }),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Prediction model",
                                        color = materialYel400,
                                        fontSize = 18.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.NavigateNext,
                                        contentDescription = "Settings",
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}