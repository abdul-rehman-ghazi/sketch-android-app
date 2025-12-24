package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing all user sketches
 */
class GetUserSketchesUseCase(
    private val sketchRepository: SketchRepository
) {
    operator fun invoke(userId: String): Flow<List<Sketch>> {
        return sketchRepository.getUserSketches(userId)
    }
}
