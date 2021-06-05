package com.gabchmel.contextmusicplayer

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable

class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PermissionsScreen()
        }
    }

    // request multiple permissions on application start
    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            var isGranted = true

            permissions.entries.forEach {
//                Log.e("DEBUG", "${it.key} = ${it.value}")
                if (it.value == false) {
                    isGranted = false
                }
            }
            val intent = Intent(this, MainActivity::class.java)

            if (isGranted) {
                // Start the main activity
                startActivity(intent)
            }

            // TODO if some permission not granted what to do
        }

    @Composable
    fun PermissionsScreen() {
        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
}