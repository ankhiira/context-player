package com.gabchmel.contextmusicplayer.utils

import android.media.MediaPlayer
import android.support.v4.media.session.PlaybackStateCompat

// Extension function to declare player state
fun MediaPlayer.toPlaybackStateBuilder(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder()
        .setState(
            if (this.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
            this.currentPosition.toLong(), 1.0F
        )
}