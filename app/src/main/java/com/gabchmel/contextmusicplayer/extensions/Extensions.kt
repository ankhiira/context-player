package com.gabchmel.contextmusicplayer

import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat

fun MediaPlayer.toPlaybackStateBuilder(): PlaybackStateCompat.Builder {
    return PlaybackStateCompat.Builder()
        .setState(
            if (this.isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED,
            this.currentPosition.toLong(), 1.0F
        )
}