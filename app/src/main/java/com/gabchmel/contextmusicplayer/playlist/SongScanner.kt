package com.gabchmel.contextmusicplayer.playlist

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.core.database.getStringOrNull

object SongScanner {

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    fun loadSongs(context: Context) : List<Song> {

        val songList = mutableListOf<Song>()

        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )?.use { cursor ->

            val idColumn  = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn =  cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.

                val songID = cursor.getLong(idColumn)

                val title = cursor.getStringOrNull(titleColumn)
                val uri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    songID
                )

                val song = Song(title, uri)
                songList.add(song)
            }
        }
        return songList
    }
}