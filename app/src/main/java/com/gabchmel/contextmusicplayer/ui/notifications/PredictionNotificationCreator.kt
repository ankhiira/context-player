package com.gabchmel.contextmusicplayer.ui.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import com.gabchmel.contextmusicplayer.R
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.service.MediaBrowserConnector
import com.gabchmel.contextmusicplayer.ui.MainActivity

object PredictionNotificationCreator {

    val CHANNEL_ID = "1234"
    val NOTIFICATION_ID = 678

    fun showNewNotification(context: Context, song: Song) {
        val notificationBuilder = createNotificationBuilder(context, song)

        showNotification(
            context,
            NOTIFICATION_ID,
            notificationBuilder
        )
    }

    private fun createNotificationBuilder(
        context: Context,
        song: Song
    ): NotificationCompat.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var description = "Test notification"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    description,
                    importance
                ).apply {
                    description = descriptionText
                }

            val notificationManager: NotificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_headphones)
            .setContentTitle(context.resources.getString(R.string.prediction_notification_title))
            .setContentText(song.title + " - " + song.artist)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSilent(true)
            .addAction(
                R.drawable.ic_play_arrow,
                "Play",
                pendingIntentPlay
            )
            .addAction(
                R.drawable.ic_skip_next,
                "Skip for now",
                pendingIntentSkip
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.resources.getString(R.string.channel_name)
            val descriptionText = context.resources.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(context, NotificationManager::class.java) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        builder: NotificationCompat.Builder
    ) {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(notificationId, builder.build())
        }
    }

    fun updateNotification(
        isPlaying: Boolean,
        context: Context,
        song: Song
    ) {
        val notification = showNewNotification(
            context,
            song
        )
    }
}