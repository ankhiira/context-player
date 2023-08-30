package com.gabchmel.contextmusicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gabchmel.contextmusicplayer.ui.theme.JetnewsTheme

@Composable
fun ContextPlayerApp() {

    val navController = rememberNavController()

    JetnewsTheme {
        Scaffold { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                ContextPlayerNavHost(
                    navController = navController
                )
            }
        }
    }
}