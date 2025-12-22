package com.hotmail.arehmananis.sketchapp.presentation.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.ThemeMode
import com.hotmail.arehmananis.sketchapp.domain.model.UserPreferences
import com.hotmail.arehmananis.sketchapp.domain.usecase.GetUserPreferencesUseCase
import com.hotmail.arehmananis.sketchapp.domain.usecase.UpdateUserPreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updateUserPreferencesUseCase: UpdateUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { preferences ->
                _uiState.value = SettingsUiState.Success(preferences)
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                val updatedPreferences = currentState.preferences.copy(themeMode = themeMode)
                updateUserPreferencesUseCase(updatedPreferences)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                val updatedPreferences = currentState.preferences.copy(notificationsEnabled = enabled)
                updateUserPreferencesUseCase(updatedPreferences)
            }
        }
    }

    fun toggleAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                val updatedPreferences = currentState.preferences.copy(analyticsEnabled = enabled)
                updateUserPreferencesUseCase(updatedPreferences)
            }
        }
    }
}

sealed interface SettingsUiState {
    object Loading : SettingsUiState
    data class Success(val preferences: UserPreferences) : SettingsUiState
}
