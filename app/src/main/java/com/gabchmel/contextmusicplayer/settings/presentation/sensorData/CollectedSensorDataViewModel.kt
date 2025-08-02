package com.gabchmel.contextmusicplayer.settings.presentation.sensorData

import android.content.Context
import androidx.lifecycle.ViewModel
import com.gabchmel.common.data.SensorValues
import com.gabchmel.common.data.dataStore.DataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class CollectedSensorDataViewModel : ViewModel() {

    private val _sensorData = MutableStateFlow<SensorValues>(SensorValues())
    val sensorData = _sensorData.asStateFlow()

    suspend fun getSensorData(context: Context) {
        val sensorData = DataStore.getSensorDataFlow(context)

        _sensorData.value = sensorData.first()
    }
}