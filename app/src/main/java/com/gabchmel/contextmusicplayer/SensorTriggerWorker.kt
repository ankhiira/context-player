package com.gabchmel.contextmusicplayer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gabchmel.sensorprocessor.SensorProcessService
import com.gabchmel.sensorprocessor.startService

class SensorTriggerWorker(private val appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(
    appContext, workerParams
) {
    override suspend fun doWork(): Result {
        // Do the work here--in this case, upload the images.
//        Intent(appContext, com.gabchmel.common.AutoPlaySongService::class.java).also { intent ->
//            appContext.startService(intent)
//        }

        val service = appContext.startService(SensorProcessService::class.java)

        service.getSensorData()

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}