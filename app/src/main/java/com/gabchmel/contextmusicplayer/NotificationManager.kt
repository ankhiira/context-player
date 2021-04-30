package com.gabchmel.contextmusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.session.MediaButtonReceiver

object NotificationManager {

    const val CHANNEL_ID = "channel"
    const val notificationID = 1234

    fun createNotification(
        context: Context,
        sessionToken: MediaSessionCompat.Token,
        title: String,
        artist: String,
        albumArt: Bitmap,
        isPlaying: Boolean

        ): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var description = "Test notification"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, description, importance).apply {
                    description = descriptionText
                }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Specification of activity that will be executed after click on the notification will be performed
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Definition of the intent execution that execute the according activity
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        // Definition of notification layout
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_headset_24)
            .setContentTitle(title)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText(artist)
            .addAction(
                R.drawable.ic_skip_previous_black_24dp,
                "Prev",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
            )
            .addAction(
                if (isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp,
                "Play",
                // notification act as media button
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            )
            .addAction(
                R.drawable.ic_skip_next_black_24dp,
                "Next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setLargeIcon(albumArt)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            // Stop the service when the notification is swiped away
            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                context,
                PlaybackStateCompat.ACTION_STOP
            ))

        return builder.build()
    }

    fun displayNotification(context: Context,notification: Notification ) {
        // Creates notification object with set parameters
        with(NotificationManagerCompat.from(context)) {
            notify(notificationID, notification)
        }
    }

}