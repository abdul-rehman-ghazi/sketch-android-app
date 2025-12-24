package com.hotmail.arehmananis.sketchapp.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hotmail.arehmananis.sketchapp.domain.model.AuthUser
import com.hotmail.arehmananis.sketchapp.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of AuthRepository using Firebase Authentication
 */
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override fun getCurrentUser(): Flow<AuthUser?> = callbackFlow {
        Log.d(TAG, "Setting up auth state listener")

        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let {
                Log.d(TAG, "Auth state changed: User signed in (uid: ${it.uid})")
                AuthUser(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString(),
                    isAnonymous = it.isAnonymous
                )
            } ?: run {
                Log.d(TAG, "Auth state changed: User signed out")
                null
            }
            trySend(user)
        }

        firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            Log.d(TAG, "Removing auth state listener")
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> {
        return try {
            Log.d(TAG, "Attempting to sign in with Google credential")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            val user = result.user?.let {
                Log.d(TAG, "Firebase authentication successful for user: ${it.uid}")
                AuthUser(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString(),
                    isAnonymous = it.isAnonymous
                )
            } ?: run {
                Log.e(TAG, "Authentication succeeded but user is null")
                return Result.failure(Exception("Authentication succeeded but user is null"))
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Firebase authentication failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Attempting to sign out user")
            firebaseAuth.signOut()
            Log.d(TAG, "User signed out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            Result.failure(e)
        }
    }
}
