package com.gabchmel.contextmusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat


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

            // TODO if some permission not granted create screen informing about
            // the lack of functionality
        }

    @Composable
    fun PermissionsScreen() {

        var array = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            array += Manifest.permission.ACTIVITY_RECOGNITION
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                // TODO redo to not deprecated version
//                startActivityForResult(
//                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
//                    0
//                )
//            }
            Log.d("Settings", "Settings not registered")
            // TODO else pro mensi API
        }

        requestMultiplePermissions.launch(
            array
        )
    }
}