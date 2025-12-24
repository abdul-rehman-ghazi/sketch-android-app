package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository

/**
 * Use case for syncing sketches with Firebase
 */
class SyncSketchesUseCase(
    private val sketchRepository: SketchRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return sketchRepository.syncSketches(userId)
    }
}
