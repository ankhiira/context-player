package com.gabchmel.common.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gabchmel.common.data.SensorValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

object DataStore {

    private const val PREFERENCES_NAME = "localStorage"

    val Context.dataStore by preferencesDataStore(
        name = PREFERENCES_NAME
    )

    private val SENSOR_DATA = stringPreferencesKey("sensorData")

    fun getSensorDataFlow(context: Context): Flow<SensorValues> {
        val sensorData = context.dataStore.data
            .map { preferences ->
                preferences[SENSOR_DATA]
            }
            .map { sensorDataJson ->
                if (sensorDataJson != null) {
                    Json.decodeFromString<SensorValues>(sensorDataJson)
                } else {
                    SensorValues()
                }
            }
        return sensorData
    }

    suspend fun saveSensorData(
        context: Context,
        sensorData: SensorValues
    ) {
        context.dataStore.edit { preferences ->
            val sensorDataJson = preferences[SENSOR_DATA]
            if (sensorDataJson != null) {
                //todo update already saved value
                val sensorData = Json.decodeFromString<SensorValues>(sensorDataJson)
            }
            preferences[SENSOR_DATA] = Json.encodeToString(sensorData)
        }
    }

    suspend fun removeSensorData(
        context: Context
    ) {
        context.dataStore.edit { preferences ->
            preferences.remove(SENSOR_DATA)
        }
    }
}