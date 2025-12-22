package com.hotmail.arehmananis.sketchapp.data.repository

import com.hotmail.arehmananis.sketchapp.data.local.PreferencesDataStore
import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import com.hotmail.arehmananis.sketchapp.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class PreferencesRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : PreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return preferencesDataStore.userPreferencesFlow
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        preferencesDataStore.updateUserPreferences(preferences)
    }
}
