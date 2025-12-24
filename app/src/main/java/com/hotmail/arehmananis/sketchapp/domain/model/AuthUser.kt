package com.hotmail.arehmananis.sketchapp.domain.model

/**
 * Domain model representing an authenticated user
 * Pure Kotlin - KMP Ready (no Android dependencies)
 */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean = false
)
