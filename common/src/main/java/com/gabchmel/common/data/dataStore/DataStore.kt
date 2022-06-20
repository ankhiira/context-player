package com.gabchmel.common.data.dataStore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore


object DataStore {

    private const val PREFERENCES_NAME = "preferences"
    val Context.dataStore by preferencesDataStore(
        name = PREFERENCES_NAME
    )
}