package com.gabchmel.contextmusicplayer.service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.gabchmel.common.data.ConvertedData
import com.gabchmel.common.utils.bindService
import com.gabchmel.contextmusicplayer.data.local.MetaDataReaderImpl
import com.gabchmel.contextmusicplayer.data.local.model.Song
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor

class SongPredictor(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var contextData = ConvertedData()

    private val sensorProcessService =
        lifecycleOwner.lifecycleScope.async {
            lifecycleOwner.whenCreated {
                val service = context.bindService(SensorDataProcessingService::class.java)
                if (service.createModel()) {
                    contextData = service.triggerPrediction()
//                    CollectedSensorDataScreen.updateUI(contextData)
                }
                service
            }
        }

    private val prediction = flow {
        emitAll(sensorProcessService.await().prediction)
    }.filterNotNull()

    private val songs = flow<List<Song>> {
        val songs = MetaDataReaderImpl(context).loadLocalStorageSongs() ?: listOf()
        emitAll(flowOf(songs))
    }.stateIn(lifecycleOwner.lifecycleScope, SharingStarted.Lazily, null)

    fun identifyPredictedSong() {
        // Check predictions
        lifecycleOwner.lifecycleScope.launch {
            @Suppress("UNCHECKED_CAST")
            launch {
                prediction.collectLatest { prediction ->
                    // Save predictions with their input to CSV file
                    val predictionFile = File(context.filesDir, "predictions.csv")
                    var predictionString = "$prediction,"
                    ConvertedData::class.primaryConstructor?.parameters?.let { parameters ->
                        for (property in parameters) {
                            val propertyNew = contextData::class.members
                                .first { it.name == property.name } as KProperty1<Any, *>
                            predictionString += when (property.name) {
                                "wifi" -> "${contextData.wifi},"
                                else -> "${propertyNew.get(contextData)},"
                            }
                        }
                    }

                    predictionString = predictionString.dropLast(1).apply { this + "\n" }
                    predictionFile.appendText(predictionString)

                    // Find song that matches the prediction hash
                    for (song in songs.filterNotNull().first()) {
                        if ("${song.title},${song.artist}"
                                .hashCode().toUInt().toString() == prediction
                        ) {
                            MediaBrowserConnector.predictedSong = song
//                            PredictionNotificationCreator.createNotification(
//                                context,
//                                MediaBrowserConnector.predictedSong
//                            )
                        }
                    }
                }
            }
        }
    }
}