package com.hotmail.arehmananis.sketchapp.domain.usecase

import com.hotmail.arehmananis.sketchapp.domain.model.User
import com.hotmail.arehmananis.sketchapp.domain.repository.UserRepository

class UpdateUserUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> {
        return userRepository.updateUser(user)
    }
}
