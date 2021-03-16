package com.gabchmel.contextmusicplayer

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.graphics.BitmapFactory

import android.graphics.Bitmap





fun MediaMetadataRetriever.getTitle() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
fun MediaMetadataRetriever.getAlbum() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
fun MediaMetadataRetriever.getArtist() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
fun MediaMetadataRetriever.getDuration() = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

// Bitmap for album art on notification
fun MediaMetadataRetriever.getAlbumArt(): Bitmap? {
    return this.embeddedPicture?.let { data -> BitmapFactory.decodeByteArray(data, 0, data.size) }
}