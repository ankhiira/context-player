package com.gabchmel.contextmusicplayer.settings.presentation.sensorData

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gabchmel.common.data.SensorValues
import com.gabchmel.common.data.dataStore.DataStore
import kotlinx.coroutines.flow.Flow

class CollectedSensorDataViewModel : ViewModel() {
    fun getSensorData(context: Context): Flow<SensorValues> {
        return DataStore.getSensorDataFlow(context)
    }
}