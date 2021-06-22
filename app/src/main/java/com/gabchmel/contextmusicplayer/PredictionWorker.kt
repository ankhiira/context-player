package com.gabchmel.contextmusicplayer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gabchmel.sensorprocessor.SensorProcessService
import com.gabchmel.sensorprocessor.startService

class PredictionWorker(private val appContext: Context, workerParams: WorkerParameters)
    : CoroutineWorker(
    appContext, workerParams
) {
    override suspend fun doWork(): Result {
        // Do the work here
        val service = appContext.startService(SensorProcessService::class.java)

        val sensorData = service.getSensorData()

        // TODO udelat when na vsechny moznosti
//        if(sensorData.headphonesPluggedIn == 1.0f) {
//            Intent(appContext, com.gabchmel.common.AutoPlaySongService::class.java).also { intent ->
//                appContext.startService(intent)
//            }
//        } else if (sensorData.lightSensorValue == 100.0f){
//            Intent(appContext, com.gabchmel.common.AutoPlaySongService::class.java).also { intent ->
//                appContext.startService(intent)
//            }
//        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}