package com.gabchmel.contextmusicplayer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PredictionWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {
            // Run prediction only if there are input data
            val inputFile = File(appContext.filesDir, "data.csv")
            if (inputFile.exists()) {
                // Connect to the MediaBrowserService, run prediction and create notification
                //TODO connect via Media3
//                MediaBrowserConnector(ProcessLifecycleOwner.get(), appContext)
            }

            val tenMinutesRequest = OneTimeWorkRequestBuilder<PredictionWorker>()
                .setInitialDelay(1, java.util.concurrent.TimeUnit.MINUTES)
                .addTag("WIFI_JOB1")
                .build()

            WorkManager.getInstance(appContext)
                .enqueueUniqueWork(
                    "work2",
                    ExistingWorkPolicy.REPLACE,
                    tenMinutesRequest
                )
        }

        return Result.success()
    }
}