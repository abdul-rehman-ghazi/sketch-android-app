package com.hotmail.arehmananis.sketchapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.domain.usecase.auth.GetCurrentAuthUserUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for observing authentication state in MainActivity
 */
class AuthViewModel(
    getCurrentAuthUserUseCase: GetCurrentAuthUserUseCase
) : ViewModel() {

    val currentUser: StateFlow<AuthUser?> = getCurrentAuthUserUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
}
