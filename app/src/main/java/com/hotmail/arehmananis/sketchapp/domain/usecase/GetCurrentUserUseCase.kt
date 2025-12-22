package com.hotmail.arehmananis.sketchapp.domain.usecase

import com.hotmail.arehmananis.sketchapp.domain.model.User
import com.hotmail.arehmananis.sketchapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<User?> {
        return userRepository.getCurrentUser()
    }
}
