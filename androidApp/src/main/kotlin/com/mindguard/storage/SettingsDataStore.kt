package com.mindguard.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SETTINGS_NAME = "mindguard_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_NAME)

class SettingsDataStore(private val context: Context) {

    companion object {
        private val PROTECTION_ENABLED_KEY = booleanPreferencesKey("protection_enabled")
    }

    val protectionEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PROTECTION_ENABLED_KEY] ?: true // Default: protection enabled
    }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PROTECTION_ENABLED_KEY] = enabled
        }
    }
}
