package com.gabchmel.common.data.dataStore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gabchmel.common.data.SensorDataPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object DataStore {

    private const val PREFERENCES_NAME = "localStorage"

    val Context.dataStore by preferencesDataStore(
        name = PREFERENCES_NAME
    )

    private val SENSOR_DATA = stringPreferencesKey("sensorData")

    fun getSensorDataFlow(context: Context): Flow<SensorDataPreferences?> {
        val sensorData = context.dataStore.data
            .map { preferences ->
                preferences[SENSOR_DATA]
            }
            .map { sensorDataJson ->
                if (sensorDataJson != null) {
                    Json.decodeFromString<SensorDataPreferences>(sensorDataJson)
                } else {
                    null
                }
            }
        return sensorData
    }

    suspend fun saveSensorData(
        context: Context,
        sensorData: SensorDataPreferences
    ) {
        context.dataStore.edit { preferences ->
            val sensorDataJson = preferences[SENSOR_DATA]
            if (sensorDataJson != null) {
                //todo update already saved value
                val sensorData = Json.decodeFromString<SensorDataPreferences>(sensorDataJson)
            }
            preferences[SENSOR_DATA] = Json.encodeToString(sensorData)
        }
    }
}