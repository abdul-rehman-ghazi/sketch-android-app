package com.hotmail.arehmananis.sketchapp.presentation.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.SignInWithGoogleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for login screen
 */
class LoginViewModel(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            Log.d(TAG, "Starting Google Sign-In with Firebase")
            _uiState.value = LoginUiState.Loading

            signInWithGoogleUseCase(idToken).fold(
                onSuccess = { user ->
                    Log.d(TAG, "Sign-in successful for user: ${user.uid}")
                    _uiState.value = LoginUiState.Success(user)
                },
                onFailure = { error ->
                    Log.e(TAG, "Sign-in failed", error)
                    val errorMessage = getUserFriendlyErrorMessage(error)
                    _uiState.value = LoginUiState.Error(errorMessage)
                }
            )
        }
    }

    /**
     * Convert technical error messages to user-friendly messages
     */
    private fun getUserFriendlyErrorMessage(error: Throwable): String {
        return when {
            error.message?.contains("cancelled", ignoreCase = true) == true ->
                "Sign-in was cancelled. Please try again."

            error.message?.contains("network", ignoreCase = true) == true ||
            error.message?.contains("timeout", ignoreCase = true) == true ->
                "Network error. Please check your internet connection and try again."

            error.message?.contains("No Google account", ignoreCase = true) == true ->
                error.message ?: "No Google account found"

            error.message?.contains("invalid", ignoreCase = true) == true ->
                "Authentication failed. Please try signing in again."

            else -> error.message ?: "Sign in failed. Please try again."
        }
    }

    fun resetToIdle() {
        _uiState.value = LoginUiState.Idle
    }
}

/**
 * UI state for login screen
 */
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val user: AuthUser) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
