package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository

/**
 * Use case for deleting a sketch
 */
class DeleteSketchUseCase(
    private val sketchRepository: SketchRepository
) {
    suspend operator fun invoke(sketchId: String): Result<Unit> {
        return sketchRepository.deleteSketch(sketchId)
    }
}
