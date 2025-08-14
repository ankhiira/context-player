package com.gabchmel.contextmusicplayer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gabchmel.contextmusicplayer.ui.navigation.ContextPlayerNavDisplay
import com.gabchmel.contextmusicplayer.ui.theme.ContextPlayerTheme

@Composable
fun ContextPlayerApp() {
    ContextPlayerTheme {
//        Scaffold { padding ->
            Box(
                modifier = Modifier
//                    .safeDrawingPadding()
            ) {
                ContextPlayerNavDisplay()
            }
//        }
    }
}