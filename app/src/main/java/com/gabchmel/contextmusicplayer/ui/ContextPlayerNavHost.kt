package com.gabchmel.contextmusicplayer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gabchmel.contextmusicplayer.ui.screens.playlistScreen.SongListScreen

@Composable
fun ContextPlayerNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            SongListScreen()
        }
        composable("settings") {

        }
    }
}