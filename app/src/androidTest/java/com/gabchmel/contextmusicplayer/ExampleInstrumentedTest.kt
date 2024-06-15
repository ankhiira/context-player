package com.gabchmel.contextmusicplayer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.gabchmel.contextmusicplayer.songPrediction.domain.worker.PredictionWorker
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.gabchmel.contextmusicplayer", appContext.packageName)
    }
}

@RunWith(AndroidJUnit4::class)
class SleepWorkerTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testSleepWorker() {
        val worker = TestListenableWorkerBuilder<PredictionWorker>(context).build()
        runBlocking {
            val result = worker.doWork()
            assertThat(result, Is.`is`(ListenableWorker.Result.success()))
        }
    }
}