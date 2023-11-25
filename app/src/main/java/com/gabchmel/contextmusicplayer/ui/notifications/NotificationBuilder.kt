//package com.gabchmel.contextmusicplayer.ui.notifications
//
//import android.content.Context
//import android.support.v4.media.session.MediaSessionCompat
//import com.gabchmel.contextmusicplayer.data.local.model.Song
//
//open class NotificationBuilder {
//
//    val CHANNEL_ID = "channel"
//    val playbackNotificationID = 1234
//
//    open fun buildNotification(
//        context: Context,
//        song: Song,
//        sessionToken: MediaSessionCompat.Token?,
//        isPlaying: Boolean
//    ) {
//
//    }
//
//    fun updateNotification(isPlaying: Boolean) {
////        notification = PlaybackNotificationCreator.createNotification(
////            baseContext,
////            null,
////            Song(
////                metadataRetriever.getTitle() ?: "unknown",
////                metadataRetriever.getArtist() ?: "unknown",
////                metadataRetriever.getAlbumArt() ?: BitmapFactory.decodeResource(
////                    resources,
////                    R.raw.album_cover_clipart
////                ),
////                Uri.EMPTY
////            ),
////            isPlaying
////        )
////
////        PlaybackNotificationCreator.displayNotification(baseContext, notification)
//    }
//}