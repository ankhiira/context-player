package com.gabchmel.contextmusicplayer.service

import android.net.Uri
import android.os.Looper
import androidx.media3.common.SimpleBasePlayer
import androidx.media3.common.util.UnstableApi
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@UnstableApi class CustomPlayer: SimpleBasePlayer(Looper.myLooper()!!) {
    override fun getState(): State {
        TODO("Not yet implemented")
    }

//    val audioManager =
//        applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val songs = MutableStateFlow(emptyList<Song>())

    // URI of current song played
    val currentSongUri = MutableStateFlow<Uri?>(null)

    // retrieve index of currently played song
    private val currSongIndex = currentSongUri.filterNotNull().map { uri ->
        songs.value.indexOfFirst { song ->
            song.uri == uri
        }
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    private val currentSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    val nextSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index + 1)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)
    val prevSong = currSongIndex.filterNotNull().map { index ->
        songs.value.getOrNull(index - 1)
    }.stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.Eagerly, null)

    private var isSongPredicted = false

    private val sensorDataProcessingService = MutableStateFlow<SensorDataProcessingService?>(null)

//    override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
//        preparePlayer(uri)
//        currentSongUri.value = uri
//        updateMetadata()
//        onPlay()
//    }

//    override fun onPlay() {
//        if (isSongPredicted) {
//            currentSongUri.value?.let { preparePlayer(it) }
//        }

//        isPlaying = true

//        audioFocusGranted = when {
//            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
//                // Request audio focus so only on app is playing audio at a time
//                audioManager.requestAudioFocus(audioFocusRequest)
//            }
//
//            else -> {
//                // Request audio focus for playback
//                audioManager.requestAudioFocus(
//                    afChangeListener,
//                    // Use the music stream.
//                    AudioManager.STREAM_MUSIC,
//                    // Request permanent focus.
//                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
//                )
//            }
//        }
//
//        if (audioFocusGranted == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            startService(
//                Intent(
//                    applicationContext,
//                    MediaBrowserServiceCompat::class.java
//                )
//            )
//        }

        // Set session active, set to use media buttons now
//        isActive = true

//        player.setOnCompletionListener {
//            updateState()
//        }
//
//        // Start the player
//        player.start()

//        updateMetadata()
//        updateState()
//        updateNotification(isPlaying)
//        startForeground(
//            PlaybackNotificationCreator.playbackNotificationID,
//            notification
//        )
//    }

//    override fun onStop() {
//        isPlaying = false
//
//        // Check if the audio focus was requested
//        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocusGranted) {
//            when {
//                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
//                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
//
//                else ->
//                    audioManager.abandonAudioFocus(afChangeListener)
//            }
//        }
//
//        stopForeground(true)
//
//        isActive = false
//    }
//
//    override fun onPause() {
//
//        isPlaying = false
//        player.pause()
//        updateState()
//        updateNotification(false)
//
//        // Take the service out of foreground, keep the notification
//        stopForeground(false)
//    }
//
//    override fun onSeekTo(position: Long) {
//        player.seekTo(position.toInt())
//        updateState()
//    }
//
//    override fun onSkipToNext() {
//        nextSong.value?.let { nextSong ->
//            onPlayFromUri(nextSong.uri, null)
//        }
//    }
//
//    override fun onSkipToPrevious() {
//        prevSong.value?.let { prevSong ->
//            onPlayFromUri(prevSong.uri, null)
//        }
//    }
//
//    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
//        return super.onMediaButtonEvent(mediaButtonEvent)
//    }
//
//    override fun onCustomAction(action: String?, extras: Bundle?) {
//        if (action.equals("skip")) {
//            extras?.let { setMetadata(extras.get("songUri") as Uri) }
//            onSkipToNext()
//        }
//    }

    // Prepare the player for the song play
    fun preparePlayer(uri: Uri) {
        val songUri = Uri.parse(uri.toString())

//        player.run {
//            reset()
//            setDataSource(baseContext, songUri)
//            prepare()
//        }
//
//        // Set source to current song to retrieve metadata
//        metadataRetriever.setDataSource(applicationContext, songUri)
    }
}