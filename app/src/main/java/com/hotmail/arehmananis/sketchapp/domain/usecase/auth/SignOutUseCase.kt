package com.hotmail.arehmananis.sketchapp.domain.usecase.auth

import com.hotmail.arehmananis.sketchapp.domain.repository.AuthRepository

/**
 * Use case for signing out
 */
class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.signOut()
    }
}
