package com.hotmail.arehmananis.sketchapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hotmail.arehmananis.sketchapp.domain.model.ThemeMode
import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesDataStore(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                themeMode = ThemeMode.valueOf(
                    preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
                ),
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                analyticsEnabled = preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true
            )
        }

    suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = userPreferences.themeMode.name
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = userPreferences.notificationsEnabled
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = userPreferences.analyticsEnabled
        }
    }
}
