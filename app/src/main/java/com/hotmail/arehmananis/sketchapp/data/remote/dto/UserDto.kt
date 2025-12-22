package com.hotmail.arehmananis.sketchapp.data.remote.dto

import com.hotmail.arehmananis.sketchapp.domain.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Data Transfer Object
 * Represents user data from API
 *
 * Keep this separate from domain model to:
 * - Isolate API changes from business logic
 * - Allow different naming conventions (snake_case vs camelCase)
 * - Enable proper data mapping
 */
@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("email")
    val email: String,

    @SerialName("bio")
    val bio: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Extension function to convert DTO to Domain model
 */
fun UserDto.toDomain(): User {
    return User(
        id = id,
        name = name,
        email = email,
        bio = bio ?: "",
        avatarUrl = avatarUrl
    )
}

/**
 * Extension function to convert Domain model to DTO
 */
fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        bio = bio,
        avatarUrl = avatarUrl
    )
}