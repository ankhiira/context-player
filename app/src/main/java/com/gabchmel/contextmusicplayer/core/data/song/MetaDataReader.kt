package com.gabchmel.contextmusicplayer.core.data.song

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.MetadataRetriever
import androidx.media3.exoplayer.source.TrackGroupArray
import kotlinx.coroutines.guava.await
import java.util.concurrent.TimeUnit

interface MetaDataReader {
    suspend fun loadLocalStorageSongs(): List<Song>?
}

class MetaDataReaderImpl(
    private val context: Context
) : MetaDataReader {

    @OptIn(UnstableApi::class)
//    @RequiresPermission(anyOf = [READ_EXTERNAL_STORAGE, READ_MEDIA_AUDIO])
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

        context.contentResolver
            .query(
                contentUri,
                null,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)

                while (cursor.moveToNext()) {
                    val songId = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        songId
                    )

                    MediaMetadataRetriever().apply {
                        setDataSource(context, uri)
                    }

                    val mediaItem = MediaItem.fromUri(uri)
                    MetadataRetriever.Builder(context, mediaItem)
                        .build()
                        .use { metadataRetriever ->
                            val trackGroups = metadataRetriever.retrieveTrackGroups().await()
                            val metadata = handleMetadata(trackGroups)

                            val song = metadata?.toSong(uri)
                            song?.let { songs.add(it) }
                        }
                }
            }
        return songs
    }

    private fun MediaMetadata.toSong(uri: Uri) = Song(
        uri = uri,
        title = title.toString(),
        artist = artist.toString(),
        metaData = this
    )

    @OptIn(UnstableApi::class)
    private fun handleMetadata(trackGroups: TrackGroupArray): MediaMetadata? {
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
}