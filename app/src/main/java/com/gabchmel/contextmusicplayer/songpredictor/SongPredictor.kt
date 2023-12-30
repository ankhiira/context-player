package com.gabchmel.contextmusicplayer.songpredictor

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.data.local.MetaDataReaderImpl
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.contextmusicplayer.ui.notifications.PredictionNotificationCreator
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class SongPredictor(
    private val context: Context,
    lifecycleOwner: LifecycleOwner
) {
    private val songs = flow {
        val songs = MetaDataReaderImpl(context).loadLocalStorageSongs() ?: listOf()
        emitAll(flowOf(songs))
    }.stateIn(lifecycleOwner.lifecycleScope, SharingStarted.Lazily, null)

    suspend fun identifyPredictedSong(): Song? {
        var predictedSongId: String? = null

        val service = context.bindService(SensorDataProcessingService::class.java)
        if (service.createModel()) {
            predictedSongId = service.triggerPrediction()
        }

        if (predictedSongId == null) {
            return null
        }

        // Find song that matches the prediction hash
        songs.first()?.find { song ->
            "${song.title},${song.artist}".hashCode().toUInt().toString() == predictedSongId
        }?.let { song ->
            PredictionNotificationCreator.showNewNotification(
                context,
                song
            )

            return song
        }

        return null
    }
}