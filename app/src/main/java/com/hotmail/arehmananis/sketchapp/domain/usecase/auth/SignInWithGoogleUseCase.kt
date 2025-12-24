package com.hotmail.arehmananis.sketchapp.domain.usecase.auth

import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.domain.repository.AuthRepository

/**
 * Use case for signing in with Google
 */
class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<AuthUser> {
        return authRepository.signInWithGoogle(idToken)
    }
}
