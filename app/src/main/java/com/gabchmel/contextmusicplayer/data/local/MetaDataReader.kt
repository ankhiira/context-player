package com.gabchmel.contextmusicplayer.data.local

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.app.Application
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.database.getStringOrNull
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.utils.getAlbum
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getDuration
import com.gabchmel.contextmusicplayer.utils.getTitle
import java.util.concurrent.TimeUnit

interface MetaDataReader {
    suspend fun loadLocalStorageSongs(): List<Song>?
}

class MetaDataReaderImpl(
    private val app: Application
) : MetaDataReader {

    private val metadataRetriever = MediaMetadataRetriever()

    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_AUDIO])
    override suspend fun loadLocalStorageSongs(): List<Song>? {

        val contentUri =
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        if (contentUri.scheme != "content") {
            return null
        }

        val songs = mutableListOf<Song>()

        // Show only audio files that are at least 1 minute in duration. Requires api Q.
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString()
        )

        app.contentResolver
            .query(
                contentUri,
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
                        app,
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
        return songs
    }

    // Function to set the metadata for a current song from URI
    fun setMetadata(songUri: Uri): MediaMetadataRetriever {
        // Set source to current song to retrieve metadata
        metadataRetriever.setDataSource(app, songUri)

//        currentSongUri.value = songUri
        getMetaData()

//        isSongPredicted = true

        return metadataRetriever
    }

    fun getMetaData() {
        // provide metadata
        val metadataBuilder: MediaMetadataCompat.Builder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataRetriever.getTitle())
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadataRetriever.getArtist())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadataRetriever.getAlbum())
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, metadataRetriever.getAlbumArt())

        metadataRetriever.getDuration()?.let { duration ->
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        }

//        mediaSession.setMetadata(metadataBuilder.build())
    }
}