package com.gabchmel.contextmusicplayer.ui.utils.notifications

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.model.Song
import com.gabchmel.contextmusicplayer.data.service.MediaBrowserConnector
import com.gabchmel.contextmusicplayer.ui.MainActivity

object PredictionNotificationCreator : NotificationBuilder() {

    fun createNotification(context: Context, song: Song) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var description = "Test notification"
            val descriptionText = "description"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    description,
                    importance
                ).apply {
                    description = descriptionText
                }

            val notificationManager: android.app.NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Specification of activity that will be executed after click on the notification will be performed
        val onNotificationClickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                onNotificationClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val intentPlay = Intent(context, MediaBrowserConnector.ActionReceiver::class.java).apply {
            this.putExtra("action", "actionPlay")
        }

        val pendingIntentPlay =
            PendingIntent.getBroadcast(
                context,
                0,
                intentPlay,
                PendingIntent.FLAG_IMMUTABLE
            )

        val intentSkip = Intent(context, MediaBrowserConnector.ActionReceiver::class.java).apply {
            this.putExtra("action", "actionSkip")
        }

        val pendingIntentSkip =
            PendingIntent.getBroadcast(
                context,
                0,
                intentSkip,
                PendingIntent.FLAG_IMMUTABLE
            )

        val notificationBuilder =
            NotificationCompat.Builder(context, PlaybackNotificationCreator.CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSilent(true)
                .setSmallIcon(R.drawable.ic_baseline_headset_24)
                .setContentTitle("Play this song?")
                .setContentText(song.title + " - " + song.author)
                .addAction(
                    R.drawable.ic_play_arrow_black_24dp,
                    "Play",
                    pendingIntentPlay
                )
                .addAction(
                    R.drawable.ic_skip_next_black_24dp,
                    "Skip for now",
                    pendingIntentSkip
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        NotificationManagerCompat.from(context).run {
            notify(678, notificationBuilder.build())
        }
    }
}