package com.hotmail.arehmananis.sketchapp.domain.usecase.auth

import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the current authenticated user
 */
class GetCurrentAuthUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<AuthUser?> {
        return authRepository.getCurrentUser()
    }
}
