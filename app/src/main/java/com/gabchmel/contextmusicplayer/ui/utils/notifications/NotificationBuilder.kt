package com.gabchmel.contextmusicplayer.ui.utils.notifications

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import com.gabchmel.contextmusicplayer.data.model.Song

open class NotificationBuilder {

    val CHANNEL_ID = "channel"
    val playbackNotificationID = 1234

    open fun buildNotification(
        context: Context,
        song: Song,
        sessionToken: MediaSessionCompat.Token?,
        isPlaying: Boolean
    ) {

    }
}