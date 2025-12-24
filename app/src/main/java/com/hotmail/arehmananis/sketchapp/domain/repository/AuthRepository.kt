package com.hotmail.arehmananis.sketchapp.domain.repository

import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 * Pure Kotlin - KMP Ready
 */
interface AuthRepository {
    /**
     * Observe the current authenticated user
     * Emits null if no user is authenticated
     */
    fun getCurrentUser(): Flow<AuthUser?>

    /**
     * Sign in with Google using an ID token
     * @param idToken The ID token from Google Sign-In
     * @return Result with AuthUser on success, Exception on failure
     */
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit>
}
