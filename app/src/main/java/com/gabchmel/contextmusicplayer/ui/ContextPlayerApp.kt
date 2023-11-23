package com.gabchmel.contextmusicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gabchmel.contextmusicplayer.ui.theme.ContextPlayerTheme

@Composable
fun ContextPlayerApp() {

    val navController = rememberNavController()

    ContextPlayerTheme {
//        Scaffold { padding ->
            Box(
                modifier = Modifier
//                    .safeDrawingPadding()
            ) {
                ContextPlayerNavHost(
                    navController = navController
                )
            }
//        }
    }
}