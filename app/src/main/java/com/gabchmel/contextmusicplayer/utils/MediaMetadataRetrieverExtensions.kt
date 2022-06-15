package com.gabchmel.contextmusicplayer.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.Log

// Extension function to work better with metadataRetriever
fun MediaMetadataRetriever.getTitle() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
fun MediaMetadataRetriever.getAlbum() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
fun MediaMetadataRetriever.getArtist() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
fun MediaMetadataRetriever.getDuration() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

// Bitmap for album art on notification
fun MediaMetadataRetriever.getAlbumArt(): Bitmap? {
    return try {
        this.embeddedPicture?.let { data -> BitmapFactory.decodeByteArray(data, 0, data.size) }
    } catch (e: Exception) {
        Log.e("Album Art:", e.toString())
        return null
    }
}