package com.gabchmel.contextmusicplayer.service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.data.local.MetaDataReaderImpl
import com.gabchmel.contextmusicplayer.ui.notifications.PredictionNotificationCreator
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SongPredictor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private val songs = flow {
        val songs = MetaDataReaderImpl(context).loadLocalStorageSongs() ?: listOf()
        emitAll(flowOf(songs))
    }.stateIn(lifecycleOwner.lifecycleScope, SharingStarted.Lazily, null)

    fun identifyPredictedSong() {
        lifecycleOwner.lifecycleScope.launch {
            var predictedSongId: String? = null

            val service = context.bindService(SensorDataProcessingService::class.java)
            if (service.createModel()) {
                predictedSongId = service.triggerPrediction()
            }

            if (predictedSongId != null) {
                // Find song that matches the prediction hash
                for (song in songs.filterNotNull().first()) {
                    if ("${song.title},${song.artist}".hashCode().toUInt()
                            .toString() == predictedSongId
                    ) {
                        PredictionCreator.predictedSong = song
                        PredictionNotificationCreator.showNewNotification(
                            context,
                            PredictionCreator.predictedSong
                        )
                    }
                }
            }
        }
    }
}