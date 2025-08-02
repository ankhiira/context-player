package com.gabchmel.contextmusicplayer.permissions.presentation

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.gabchmel.contextmusicplayer.MainActivity
import com.gabchmel.contextmusicplayer.R


class PermissionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                darkScrim = Color.TRANSPARENT,
                scrim = Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)

        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                startActivity(Intent(this, MainActivity::class.java))
            }

        setContent {
            PermissionsScreen(requestMultiplePermissions)
        }
    }
}