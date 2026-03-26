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
        val sketch = sketchRepository.getSketchById(id)
            ?: return Result.failure(Exception("Sketch not found: $id"))

        var result = sketch

        // Download paths if not cached locally
        if (downloadPaths && result.drawingPaths.isNullOrEmpty() && !result.remotePathsUrl.isNullOrBlank()) {
            val pathsResult = sketchRepository.downloadAndCachePaths(id)
            if (pathsResult.isSuccess) {
                result = result.copy(drawingPaths = pathsResult.getOrNull())
            }
        }

        // Download emojis if not cached locally
        if (downloadPaths && result.emojiElements.isNullOrEmpty() && !result.remoteEmojisUrl.isNullOrBlank()) {
            val emojisResult = sketchRepository.downloadAndCacheEmojis(id)
            if (emojisResult.isSuccess) {
                result = result.copy(emojiElements = emojisResult.getOrNull())
            }
        }

        return Result.success(result)
    }
}
