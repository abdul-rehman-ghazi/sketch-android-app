package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository

/**
 * Use case for creating a new sketch
 */
class CreateSketchUseCase(
    private val sketchRepository: SketchRepository
) {
    suspend operator fun invoke(sketch: Sketch): Result<Sketch> {
        return sketchRepository.createSketch(sketch)
    }
}
