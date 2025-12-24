package com.hotmail.arehmananis.sketchapp.presentation.feature.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.hotmail.arehmananis.sketchapp.BuildConfig

/**
 * Helper class for Google Sign-In using Credentials Manager API
 */
class GoogleSignInHelper(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    /**
     * Launch Google Sign-In flow and return ID token
     * @return Result with ID token on success, exception on failure
     */
    suspend fun signIn(): Result<String> {
        return try {
            Log.d(TAG, "Initiating Google Sign-In flow")

            val googleIdOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Defensive check: Verify credential type before casting
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d(TAG, "Successfully retrieved Google ID token")
                Result.success(googleIdTokenCredential.idToken)
            } else if (credential is GoogleIdTokenCredential) {
                // Direct type match (backward compatibility)
                Log.d(TAG, "Successfully retrieved Google ID token")
                Result.success(credential.idToken)
            } else {
                Log.e(TAG, "Unexpected credential type: ${credential.type}")
                Result.failure(Exception("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled sign-in")
            Result.failure(Exception("Sign-in was cancelled", e))
        } catch (e: NoCredentialException) {
            Log.e(TAG, "No Google accounts found on device", e)
            Result.failure(Exception("No Google account found. Please add a Google account to your device.", e))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(Exception("Sign-in failed: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            Result.failure(Exception("Sign-in error: ${e.message}", e))
        }
    }
}
