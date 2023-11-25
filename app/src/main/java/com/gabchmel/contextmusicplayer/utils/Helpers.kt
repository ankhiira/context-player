package com.gabchmel.contextmusicplayer.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.gabchmel.contextmusicplayer.BuildConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun isPermissionNotGranted(
    context: Context,
    permission: String
): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) != PackageManager.PERMISSION_GRANTED
}

// Copy the file to temp file
fun convertFileForSend(
    context: Context,
    filename: String,
    ext: String,
    origFile: File
): Uri {
    val tempFile = File.createTempFile(
        filename,
        ext,
        context.cacheDir
    )

    val inputStream = FileInputStream(origFile)
    inputStream.use { inStream ->
        val outputStream = FileOutputStream(tempFile)
        outputStream.use { outStream ->
            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int
            while (inStream.read(buf)
                    .also { len = it } > 0
            ) {
                outStream.write(buf, 0, len)
            }
        }
    }

    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.fileProvider",
        tempFile
    )
}