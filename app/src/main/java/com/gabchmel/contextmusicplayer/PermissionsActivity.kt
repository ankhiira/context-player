package com.gabchmel.contextmusicplayer

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable


class PermissionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Toast.makeText(this, "again opened", Toast.LENGTH_SHORT).show()
        Log.d("opened", "opened again")

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

//            if (isGranted) {
                // Start the main activity
                startActivity(intent)
//            }

            // TODO if some permission not granted create screen informing about
            // the lack of functionality
        }

    @Composable
    fun PermissionsScreen() {

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACTIVITY_RECOGNITION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            Log.e("permission", "permission not granted")
//            return
//        }

        var array = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            array += Manifest.permission.ACTIVITY_RECOGNITION
        }

        requestMultiplePermissions.launch(
            array
        )
    }
}