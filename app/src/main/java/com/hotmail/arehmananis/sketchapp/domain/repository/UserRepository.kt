package com.hotmail.arehmananis.sketchapp.domain.repository

import com.hotmail.arehmananis.sketchapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun updateUser(user: User): Result<Unit>
}
