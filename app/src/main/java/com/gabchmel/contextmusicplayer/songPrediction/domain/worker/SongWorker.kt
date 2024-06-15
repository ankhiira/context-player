package com.gabchmel.contextmusicplayer.songPrediction.domain.worker

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gabchmel.common.utils.dataCsvFileName
import com.gabchmel.contextmusicplayer.songPrediction.domain.PredictionCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

// TODO play after music service is on - or maybe every time
class SongWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            // Load list of songs from local storage
//        loadSongs()
//
//        // Bind to SensorProcessService to later write to the file
//        this.bindService(
//            Intent(this, SensorDataProcessingService::class.java),
//            connection,
//            Context.BIND_AUTO_CREATE
//        )
//
//        // Every 10 seconds write to file sensor measurements with the song ID
//            if (isPlaying)
//                currentSong.value?.title?.let { title ->
//                    currentSong.value?.author?.let { author ->
//                        // Create a hashCode to use it as ID of the song
//                        val titleAuthor = "$title,$author".hashCode().toUInt()
//                        sensorDataProcessingService.value?.writeToFile(titleAuthor.toString())
//                    }
//                }

            // Run prediction only if there are input data
            val inputFile = File(appContext.filesDir, dataCsvFileName)
            if (inputFile.exists()) {
                // Connect to the MediaBrowserService, run prediction and create notification
                PredictionCreator(ProcessLifecycleOwner.get(), appContext)
            }

            val request = OneTimeWorkRequestBuilder<SongWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .addTag("SONG_WORKER")
                .build()

            WorkManager.getInstance(appContext)
                .enqueueUniqueWork(
                    "songWork",
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }

        return Result.success()
    }
}