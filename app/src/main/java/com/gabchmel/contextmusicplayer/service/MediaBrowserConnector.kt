//package com.gabchmel.contextmusicplayer.service
//
//import android.content.BroadcastReceiver
//import android.content.ComponentName
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.support.v4.media.MediaBrowserCompat
//import android.support.v4.media.session.MediaControllerCompat
//import androidx.core.os.bundleOf
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.lifecycleScope
//import com.gabchmel.contextmusicplayer.data.local.model.Song
//import kotlinx.coroutines.CompletableDeferred
//import kotlinx.coroutines.launch
//
//class MediaBrowserConnector(
//    val lifecycleOwner: LifecycleOwner,
//    val context: Context
//) : LifecycleObserver {
//
////    // Broadcast Receiver listening to action performed by click on prediction notification buttons
////    class ActionReceiver : BroadcastReceiver() {
////        override fun onReceive(context: Context, intent: Intent) {
//////            val action = intent.getStringExtra("action")
//////            lifecycleOwnerNew.lifecycleScope.launch {
//////                when {
//////                    action.equals("actionPlay") -> play(predictedSong.uri)
//////                    action.equals("actionSkip") -> skip(predictedSong.uri)
//////                }
//////            }
////
//////            // After clicking on the notification button, dismiss the notification
//////            NotificationManagerCompat.from(context).cancel(678)
//////
//////            // Close the notification tray
//////            context.sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
////        }
////
////        // Send action to MediaPlaybackService to set predicted song for play
////        private suspend fun play(songUri: Uri) {
////            lifecycleOwnerNew.lifecycleScope.launch {
////                mediaControllerNew.await().transportControls.playFromUri(songUri, null)
////            }.join()
////        }
////
////        // Send action to MediaPlaybackService to set predicted song for play
////        private suspend fun skip(songUri: Uri) {
////            lifecycleOwnerNew.lifecycleScope.launch {
////                mediaControllerNew.await().transportControls.sendCustomAction(
////                    "skip",
////                    bundleOf("songUri" to songUri)
////                )
////            }.join()
////        }
////    }
////
////    private lateinit var mediaBrowser: MediaBrowserCompat
//
//    init {
////        val songPredictor = SongPredictor(
////            context,
////            lifecycleOwner
////        )
////
////        lifecycleOwner.lifecycleScope.launch {
////            lifecycleOwner.whenCreated {
////                lifecycleOwnerNew = lifecycleOwner
////
////                // Register receiver of the notification button action
////                context.registerReceiver(ActionReceiver(), IntentFilter("action"))
////
//////                val sensorProcessService = sensorProcessService.await()
////
////                when {
////                    !BuildConfig.IS_DEBUG -> {
//////                        if (sensorProcessService.hasContextChanged()) {
//////                            createMediaBrowser()
//////                            mediaBrowser.connect()
//////                            songPredictor.identifyPredictedSong()
//////                        }
////                        // Save current sensor values to later detect if the context changed
//////                        sensorProcessService.saveSensorValuesToSharedPrefs()
////                    }
////
////                    else -> {
////                        createMediaBrowser()
////                        mediaBrowser.connect()
////                        songPredictor.identifyPredictedSong()
////                    }
////                }
////            }
////        }
//    }
//
//
//    private fun createMediaBrowser() {
//        val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
//            override fun onConnected() {
//                val mediaController = MediaControllerCompat(
//                    context,
//                    mediaBrowser.sessionToken
//                )
//
//                mediaControllerNew.complete(mediaController)
//
//                mediaController.registerCallback(object : MediaControllerCompat.Callback() {})
//
//                lifecycleOwner.lifecycleScope.launch {
//                    val intent = Intent(context, MusicService::class.java)
//                    intent.putExtra("is_binding", true)
////                    boundService.complete(
////                        bindServiceAndWait(
////                            context,
////                            intent,
////                            Context.BIND_AUTO_CREATE
////                        )
////                    )
//                }
//            }
//        }
//
//        // Setting MediaBrowser for connecting to the MediaBrowserService
//        mediaBrowser = MediaBrowserCompat(
//            context,
//            ComponentName(context, MusicService::class.java),
//            connectionCallbacks,
//            null
//        )
//    }
//
//    companion object {
//        lateinit var lifecycleOwnerNew: LifecycleOwner
//        lateinit var predictedSong: Song
//        val mediaControllerNew = CompletableDeferred<MediaControllerCompat>()
//    }
//}