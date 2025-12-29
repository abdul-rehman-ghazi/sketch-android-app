package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository

/**
 * Use case for updating an existing sketch
 * Handles rename and other metadata updates
 */
class UpdateSketchUseCase(
    private val sketchRepository: SketchRepository
) {
    suspend operator fun invoke(sketch: Sketch): Result<Sketch> {
        return sketchRepository.updateSketch(sketch)
    }
}
