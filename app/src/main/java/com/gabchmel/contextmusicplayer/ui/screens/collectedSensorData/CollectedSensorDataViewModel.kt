package com.gabchmel.contextmusicplayer.ui.screens.collectedSensorData

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gabchmel.common.utils.bindService
import com.gabchmel.sensorprocessor.data.model.MeasuredSensorValues
import com.gabchmel.sensorprocessor.data.service.SensorDataProcessingService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CollectedSensorDataViewModel : ViewModel() {

    var sensorData: StateFlow<MeasuredSensorValues>? = null

    fun getSensorData(context: Context) {
        viewModelScope.launch {
            val sensorDataProcessingService =
                context.bindService(SensorDataProcessingService::class.java)

            sensorData = sensorDataProcessingService.measuredSensorValues
        }
    }
}