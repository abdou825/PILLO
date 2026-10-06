package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pillo_settings")

class DataStoreManager(private val context: Context) {
    companion object {
        val KEY_EMERGENCY_NOTIFICATION = booleanPreferencesKey("emergency_notification_enabled")
        val KEY_PARENT_PIN = stringPreferencesKey("parent_pin")
        val KEY_LAST_LOW_STOCK_PROMPT = stringPreferencesKey("last_low_stock_prompt")
    }

    val isEmergencyNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_EMERGENCY_NOTIFICATION] ?: false
    }

    suspend fun setEmergencyNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_EMERGENCY_NOTIFICATION] = enabled
        }
    }

    val parentPin: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_PARENT_PIN] ?: "1234"
    }

    suspend fun setParentPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PARENT_PIN] = pin
        }
    }
}
