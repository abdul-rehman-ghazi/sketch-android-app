package com.hotmail.arehmananis.sketchapp.domain.usecase

import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import com.hotmail.arehmananis.sketchapp.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetUserPreferencesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    operator fun invoke(): Flow<UserPreferences> {
        return preferencesRepository.getUserPreferences()
    }
}
