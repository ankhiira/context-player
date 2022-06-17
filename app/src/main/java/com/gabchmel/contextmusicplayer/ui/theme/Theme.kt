package com.gabchmel.contextmusicplayer.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColors = darkColors(
    primary = Pink500,
    secondary = Grey400,
    // On objets - text color
    onBackground = Yel900,
    onPrimary = Yel900,
    onSecondary = Yel900
)

@Composable
fun JetnewsTheme(
    content: @Composable () -> Unit
) {

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colors = DarkColors,
            content = content,
            typography = MyTypography
        )
    }
}
