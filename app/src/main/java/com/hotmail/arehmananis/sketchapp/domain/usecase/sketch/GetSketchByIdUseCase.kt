package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository

/**
 * Use case for retrieving a sketch by ID
 * Pure Kotlin - KMP Ready
 */
class GetSketchByIdUseCase(
    private val sketchRepository: SketchRepository
) {
    /**
     * Get sketch by ID, optionally downloading paths from cloud
     *
     * @param id Sketch ID
     * @param downloadPaths If true, download and cache paths from Cloudinary (default: true)
     * @return Sketch with paths loaded, or null if not found
     */
    suspend operator fun invoke(id: String, downloadPaths: Boolean = true): Result<Sketch> {
        // Get sketch from repository
        val sketch = sketchRepository.getSketchById(id)
            ?: return Result.failure(Exception("Sketch not found: $id"))

        // If paths already loaded locally, return immediately
        if (!sketch.drawingPaths.isNullOrEmpty()) {
            return Result.success(sketch)
        }

        // If downloadPaths is false or no remotePathsUrl, return sketch as-is
        if (!downloadPaths || sketch.remotePathsUrl.isNullOrBlank()) {
            return Result.success(sketch)
        }

        // Download and cache paths from Cloudinary
        val pathsResult = sketchRepository.downloadAndCachePaths(id)

        return if (pathsResult.isSuccess) {
            // Paths downloaded successfully, return updated sketch
            val paths = pathsResult.getOrNull()
            Result.success(sketch.copy(drawingPaths = paths))
        } else {
            // Path download failed, but return sketch anyway (graceful degradation)
            // User can still view the image, just can't edit
            Result.success(sketch)
        }
    }
}
