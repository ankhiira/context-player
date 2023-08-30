package com.gabchmel.contextmusicplayer

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

fun checkPermissionLocal(
    context: Context,
    permission: String
): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) != PackageManager.PERMISSION_GRANTED
}