package com.gabchmel.contextmusicplayer

import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PredictionWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(
        appContext, workerParams
    ) {

    companion object {
        const val Progress = "progress"
        private const val delayDuration = 2000L
    }

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {

            val firstUpdate = workDataOf(Progress to 0)
            val lastUpdate = workDataOf(Progress to 100)
            setProgress(firstUpdate)
            delay(delayDuration)
            Log.d("WorkManager", "Working")
            setProgress(lastUpdate)
            delay(delayDuration)

//            var isRunning = false
//
//            // Check if the service is running
//            val manager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//            for (serviceInfo in manager.getRunningServices(Integer.MAX_VALUE)) {
//                if (AutoPlaySongService::class.java.name.equals(serviceInfo.service.className)) {
//                    isRunning = true
//                }
//            }
//
//            if (!isRunning) {
//
////                Intent(appContext, AutoPlaySongService::class.java).also { intentService ->
////                    appContext.startService(intentService)
////                }
////                appContext.bindService(AutoPlaySongService::class.java)
//
//                val autoPlaySongService = async {
//                        val service = appContext.bindService(AutoPlaySongService::class.java)
//
//                        service
//                }
//            }

//            val service = CompletableDeferred<AutoPlaySongService>()
//
//            val serviceAuto = service.await()
//            serviceAuto.playSong()

            val connector = MediaBrowserConnector(ProcessLifecycleOwner.get(), appContext)
//            connector.play()

            // Do the work here
//        val service = appContext.bindService(SensorProcessService::class.java)
//
//        val sensorData = service.getSensorData()

            Log.d("Work", "Done Work")
//        Toast.makeText(appContext, "Done work", Toast.LENGTH_LONG).show()

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
        }
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}