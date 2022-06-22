package com.gabchmel.contextmusicplayer.data.local

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.database.getStringOrNull
import com.gabchmel.contextmusicplayer.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


// Song retriever from local storage
object LocalSongsRetriever {

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    suspend fun loadLocalStorageSongs(context: Context)=
        withContext(Dispatchers.Default) {
            val songs = mutableListOf<Song>()

            // Show only audio files that are at least 1 minute in duration. Requires api Q.
            val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
            val selectionArgs = arrayOf(
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()
            )

            // Query the local media folder
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val authorColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

                while (cursor.moveToNext()) {
                    // Get the song metadata
                    val songID = cursor.getLong(idColumn)
                    val title = cursor.getStringOrNull(titleColumn)
                    val author = cursor.getStringOrNull(authorColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songID
                    )

                    val metadataRetriever = MediaMetadataRetriever()
                    metadataRetriever.setDataSource(
                        context,
                        Uri.parse(uri.toString())
                    )
                    val albumArt =
                        try {
                            metadataRetriever.embeddedPicture?.let { data ->
                                BitmapFactory.decodeByteArray(data, 0, data.size)
                            }
                        } catch (e: Exception) {
                            Log.e("Album Art:", e.toString())
                            null
                        }

                    songs.add(Song(title, author, albumArt, uri))
                }
            }
            return@withContext songs
        }
}