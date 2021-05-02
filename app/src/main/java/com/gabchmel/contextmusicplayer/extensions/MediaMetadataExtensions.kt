package com.gabchmel.contextmusicplayer

import android.support.v4.media.MediaMetadataCompat

fun MediaMetadataCompat.getTitle() = getString(MediaMetadataCompat.METADATA_KEY_TITLE)
fun MediaMetadataCompat.getAlbum() = getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
fun MediaMetadataCompat.getArtist() = getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
fun MediaMetadataCompat.getDuration() = getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
fun MediaMetadataCompat.getAlbumArt() = getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)