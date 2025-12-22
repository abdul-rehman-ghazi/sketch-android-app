package com.hotmail.arehmananis.sketchapp.domain.usecase

import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import com.hotmail.arehmananis.sketchapp.domain.repository.PreferencesRepository

class UpdateUserPreferencesUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(preferences: UserPreferences) {
        preferencesRepository.updateUserPreferences(preferences)
    }
}
