package com.gabchmel.contextmusicplayer.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gabchmel.contextmusicplayer.R

val Rubik = FontFamily(
    Font(R.font.notosansjp_regular),
    Font(R.font.notosansjp_medium, FontWeight.W500),
    Font(R.font.notosansjp_bold, FontWeight.Bold)
)

val MyTypography = Typography(
    h1 = TextStyle(
        fontFamily = Rubik,
        fontWeight = FontWeight.W300,
        fontSize = 96.sp
    ),
    body1 = TextStyle(
        fontFamily = Rubik,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp
    )
    /*...*/
)