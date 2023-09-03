package com.gabchmel.contextmusicplayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gabchmel.contextmusicplayer.R

val bahnSchrift = FontFamily(
    Font(R.font.bahnschrift_14),
    Font(R.font.bahnschrift_14, FontWeight.W500),
    Font(R.font.bahnschrift_14, FontWeight.Bold)
)

val typography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 96.sp,
        fontFamily = bahnSchrift,
        color = Yel900
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        fontFamily = bahnSchrift,
        color = Yel900
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        fontFamily = bahnSchrift,
        color = Yel900
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        fontFamily = bahnSchrift,
        color = Yel900
    )
)