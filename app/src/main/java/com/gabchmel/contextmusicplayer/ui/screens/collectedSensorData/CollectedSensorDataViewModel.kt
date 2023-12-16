package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.common.utils.bindService
import com.gabchmel.sensorprocessor.data.model.SensorValues
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectedSensorDataViewModel : ViewModel() {

    var sensorData: StateFlow<SensorValues>? = null

    fun getSensorData(context: Context) {
        viewModelScope.launch {
            val sensorDataProcessingService =
                context.bindService(SensorDataProcessingService::class.java)

            sensorData = sensorDataProcessingService.sensorValues
        }
    }
}