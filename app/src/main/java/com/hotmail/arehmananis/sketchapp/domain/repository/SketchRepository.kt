package com.hotmail.arehmananis.sketchapp.domain.repository

import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for sketch operations
 * Pure Kotlin - KMP Ready
 */
interface SketchRepository {
    /**
     * Observe all sketches for a specific user
     * @param userId The user ID to get sketches for
     * @return Flow of sketch list (automatically updates from Room)
     */
    fun getUserSketches(userId: String): Flow<List<Sketch>>

    /**
     * Get a specific sketch by ID
     * @param id The sketch ID
     * @return Sketch if found, null otherwise
     */
    suspend fun getSketchById(id: String): Sketch?

    /**
     * Create a new sketch
     * @param sketch The sketch to create
     * @return Result with created Sketch on success
     */
    suspend fun createSketch(sketch: Sketch): Result<Sketch>

    /**
     * Update an existing sketch
     * @param sketch The sketch to update
     * @return Result with updated Sketch on success
     */
    suspend fun updateSketch(sketch: Sketch): Result<Sketch>

    /**
     * Delete a sketch
     * @param id The sketch ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteSketch(id: String): Result<Unit>

    /**
     * Sync local sketches with Firebase
     * - Uploads pending sketches
     * - Downloads new remote sketches
     * - Resolves conflicts
     * @param userId The user ID to sync for
     * @return Result indicating sync success or failure
     */
    suspend fun syncSketches(userId: String): Result<Unit>
}
