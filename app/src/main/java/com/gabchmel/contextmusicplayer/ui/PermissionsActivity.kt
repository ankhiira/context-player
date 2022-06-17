package com.gabchmel.contextmusicplayer.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.gabchmel.contextmusicplayer.R


class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                startActivity(Intent(this, MainActivity::class.java))
            }

        setContent {
            PermissionsScreen(requestMultiplePermissions)
        }
    }

    // Composable function to show the permissions screen if some permission are not requested
    @Composable
    fun PermissionsScreen(requestMultiplePermissions : ActivityResultLauncher<Array<String>>) {
        // Request the permissions on application start


        var permissionsToRequest = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest += Manifest.permission.ACTIVITY_RECOGNITION
        }

        requestMultiplePermissions.launch(
            permissionsToRequest
        )
    }
}