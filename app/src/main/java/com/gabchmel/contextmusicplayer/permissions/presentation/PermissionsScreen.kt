package com.gabchmel.contextmusicplayer.permissions.presentation

import android.Manifest
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable

@Composable
fun PermissionsScreen(requestMultiplePermissions : ActivityResultLauncher<Array<String>>) {
    var permissionsToRequest = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        permissionsToRequest += Manifest.permission.ACTIVITY_RECOGNITION
    }

    requestMultiplePermissions.launch(permissionsToRequest)
}