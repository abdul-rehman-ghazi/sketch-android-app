package com.hotmail.arehmananis.sketchapp.data.repository

import com.hotmail.arehmananis.sketchapp.domain.model.User
import com.hotmail.arehmananis.sketchapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepositoryImpl : UserRepository {

    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = "1",
            name = "John Doe",
            email = "john.doe@example.com",
            bio = "Android Developer | Kotlin Enthusiast | Coffee Lover",
            avatarUrl = null
        )
    )

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    override suspend fun updateUser(user: User): Result<Unit> {
        return try {
            _currentUser.value = user
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
