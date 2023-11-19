package com.gabchmel.contextmusicplayer.data.local

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_AUDIO
import android.app.Application
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import android.support.v4.media.MediaMetadataCompat
import androidx.annotation.OptIn
import androidx.annotation.RequiresPermission
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MetadataRetriever
import androidx.media3.exoplayer.source.TrackGroupArray
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.utils.getAlbum
import com.gabchmel.contextmusicplayer.utils.getAlbumArt
import com.gabchmel.contextmusicplayer.utils.getArtist
import com.gabchmel.contextmusicplayer.utils.getDuration
import com.gabchmel.contextmusicplayer.utils.getTitle
import kotlinx.coroutines.guava.asDeferred
import java.util.concurrent.TimeUnit

interface MetaDataReader {
    suspend fun loadLocalStorageSongs(): List<Song>?
}

class MetaDataReaderImpl(
    private val app: Application
) : MetaDataReader {

    private val metadataRetriever = MediaMetadataRetriever()

    @OptIn(UnstableApi::class) @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_AUDIO])
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

                while (cursor.moveToNext()) {
                    // Get the song metadata
                    val songID = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songID
                    )

                    val metadataRetriever = MediaMetadataRetriever()
                    metadataRetriever.setDataSource(
                        app,
                        Uri.parse(uri.toString())
                    )

                    val mediaItem = MediaItem.fromUri(uri)
                    val trackGroups = MetadataRetriever.retrieveMetadata(
                        app.applicationContext,
                        mediaItem
                    ).asDeferred().await()

                    val metadata = handleMetadata(trackGroups)

                    songs.add(
                        Song(
                            uri = uri,
                            title = metadata?.title?.toString(),
                            artist = metadata?.artist?.toString(),
                            metaData = metadata
                        )
                    )
                }
            }
        return songs
    }

    @OptIn(UnstableApi::class) private fun handleMetadata(trackGroups: TrackGroupArray): MediaMetadata? {
        for (index in 0..trackGroups.length) {
            val metadata = trackGroups[index].getFormat(0).metadata
            return if (metadata != null) {
                MediaMetadata.Builder()
                    .populateFromMetadata(metadata)
                    .build()
            } else {
                null
            }
        }

        return null
    }

    // Function to set the metadata for a current song from URI
    fun setMetadata(songUri: Uri): MediaMetadataRetriever {
        // Set source to current song to retrieve metadata
        metadataRetriever.setDataSource(app, songUri)

//        currentSongUri.value = songUri
//        getMetaData()

//        isSongPredicted = true

        return metadataRetriever
    }

    fun getMetaData() {
        val metadataBuilder = MediaMetadataCompat.Builder()
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