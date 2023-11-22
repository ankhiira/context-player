package com.gabchmel.contextmusicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = Pink500,
    onPrimary = Yel900,
    secondary = Pink500,
    background = DarkGray,
    surface = DarkestGray,
    onSecondary = LightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink500,
    secondary = Grey400,
    // On objets - text color
    onBackground = Yel900,
    onPrimary = Yel900,
    onSecondary = LightGray
)

@Composable
fun ContextPlayerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}