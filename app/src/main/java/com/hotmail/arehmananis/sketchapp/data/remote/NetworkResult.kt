package com.hotmail.arehmananis.sketchapp.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.ResponseException
import java.io.IOException

/**
 * Sealed interface representing network operation results
 * Provides type-safe error handling
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val exception: Exception, val message: String? = null) : NetworkResult<Nothing>
    object Loading : NetworkResult<Nothing>
}

/**
 * Network exception types for better error handling
 */
sealed class NetworkException(message: String) : Exception(message) {
    class NoInternetException : NetworkException("No internet connection")
    class TimeoutException : NetworkException("Request timeout")
    class ServerException(val code: Int, message: String) : NetworkException("Server error: $code - $message")
    class UnknownException(message: String) : NetworkException(message)
}

/**
 * Extension function to safely execute network calls
 * Converts exceptions to NetworkResult
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): NetworkResult<T> {
    return try {
        NetworkResult.Success(apiCall())
    } catch (e: IOException) {
        NetworkResult.Error(
            NetworkException.NoInternetException(),
            "Check your internet connection"
        )
    } catch (e: ClientRequestException) {
        // 4xx errors
        NetworkResult.Error(
            NetworkException.ServerException(e.response.status.value, e.message),
            "Client error occurred"
        )
    } catch (e: ServerResponseException) {
        // 5xx errors
        NetworkResult.Error(
            NetworkException.ServerException(e.response.status.value, e.message),
            "Server error occurred"
        )
    } catch (e: ResponseException) {
        // Other HTTP errors
        NetworkResult.Error(
            NetworkException.ServerException(e.response.status.value, e.message ?: "Unknown error"),
            "HTTP error occurred"
        )
    } catch (e: Exception) {
        NetworkResult.Error(
            NetworkException.UnknownException(e.message ?: "Unknown error"),
            "An unexpected error occurred"
        )
    }
}

/**
 * Convert NetworkResult to domain Result type
 */
fun <T> NetworkResult<T>.toResult(): Result<T> {
    return when (this) {
        is NetworkResult.Success -> Result.success(data)
        is NetworkResult.Error -> Result.failure(exception)
        is NetworkResult.Loading -> Result.failure(IllegalStateException("Still loading"))
    }
}