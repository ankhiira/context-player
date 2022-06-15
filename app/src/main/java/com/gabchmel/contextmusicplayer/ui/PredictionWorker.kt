package com.gabchmel.contextmusicplayer.ui

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import com.gabchmel.contextmusicplayer.data.service.MediaBrowserConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File


class PredictionWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(
        appContext, workerParams
    ) {

    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {

            // Run prediction only if there are input data
            val inputFile = File(appContext.filesDir, "data.csv")
            if (inputFile.exists()) {
                // Connect to the MediaBrowserService, run prediction and create notification
                MediaBrowserConnector(ProcessLifecycleOwner.get(), appContext)
            }

            // Enqueue this unique work again so it achieves periodicity
            val tenMinutesRequest = OneTimeWorkRequestBuilder<PredictionWorker>()
                .setInitialDelay(1, java.util.concurrent.TimeUnit.MINUTES)
                .addTag("WIFIJOB1")
                .build()
            WorkManager.getInstance(appContext)
                .enqueueUniqueWork(
                    "work2",
                    ExistingWorkPolicy.REPLACE,
                    tenMinutesRequest
                )

            // Log.d("Work", "Done Work")
        }
        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}