package com.hotmail.arehmananis.sketchapp.domain.repository

import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences)
}
