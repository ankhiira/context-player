package com.gabchmel.contextmusicplayer.settingsScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
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
                                        "Settings",
                                        color = materialYel400,
                                        fontSize = 18.sp,
                                    )
                                },
                                elevation = 0.dp,
                                backgroundColor = Color.Transparent)
                        },
                        content = {
                            Column {
                                Text(
                                    text = "Available sensors",
                                    modifier = Modifier
                                        .clickable(onClick = {
                                            val play = true
                                            findNavController().navigate(
                                                SettingsFragmentDirections
                                                    .actionSettingsFragmentToSensorScreen()
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