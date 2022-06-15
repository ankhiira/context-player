package com.gabchmel.contextmusicplayer.utils

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat

// Extension functions to work better with metadata
fun MediaMetadataCompat.getTitle(): String? = getString(MediaMetadataCompat.METADATA_KEY_TITLE)
fun MediaMetadataCompat.getAlbum() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
fun MediaMetadataCompat.getArtist(): String? = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
fun MediaMetadataCompat.getDuration() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
fun MediaMetadataCompat.getAlbumArt(): Bitmap? = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)