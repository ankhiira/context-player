package com.gabchmel.contextmusicplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri

data class MusicMetadata(
    val title: String?,
    val artist: String?,
    val album: String?
)

object MusicMetadataExtractor {
    fun extract(metadataRetriever: MediaMetadataRetriever): MusicMetadata {

        return MusicMetadata(
         title=    metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
         album=   metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
         artist=   metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        )
    }
}

fun MediaMetadataRetriever.toMusicMetadata(): MusicMetadata {

    return MusicMetadata(
        title=    extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
        album=   extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
        artist=   extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    )
}