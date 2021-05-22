package com.gabchmel.contextmusicplayer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
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

    private fun hasPermissions(context: Context, vararg permissions: Array<String>): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(
                context,
                it.toString()
            ) == PackageManager.PERMISSION_GRANTED
        }

    @Composable
    fun PermissionsScreen() {

        val requestCode = 1
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val intent = Intent(this, MainActivity::class.java)

        // Check if the permissions have been granted
        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions(this, permissions, requestCode)

            // After granting the permissions check if they are granted all
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {

                // Start the main activity
                startActivity(intent)
            }
//            else {
//                Column {
//                    Text(text = "Permissions needs to be granted")
//                    Button(onClick = { /*TODO*/ }) {
//                        Text("Grant Permissions")
//                    }
//                }
//            }
        // Permissions granted
        } else {
            startActivity(intent)
        }
    }
}