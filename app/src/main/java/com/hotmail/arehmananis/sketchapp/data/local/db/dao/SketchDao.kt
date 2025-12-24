package com.hotmail.arehmananis.sketchapp.data.local.db.dao

import androidx.room.*
import com.hotmail.arehmananis.sketchapp.data.local.db.entity.SketchEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for sketch database operations
 */
@Dao
interface SketchDao {
    /**
     * Observe all sketches for a user, sorted by most recent first
     */
    @Query("SELECT * FROM sketches WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getUserSketches(userId: String): Flow<List<SketchEntity>>

    /**
     * Get a specific sketch by ID
     */
    @Query("SELECT * FROM sketches WHERE id = :id")
    suspend fun getSketchById(id: String): SketchEntity?

    /**
     * Insert or replace a sketch
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSketch(sketch: SketchEntity)

    /**
     * Update an existing sketch
     */
    @Update
    suspend fun updateSketch(sketch: SketchEntity)

    /**
     * Delete a sketch by ID
     */
    @Query("DELETE FROM sketches WHERE id = :id")
    suspend fun deleteSketchById(id: String)

    /**
     * Get sketches by sync status (for background sync)
     */
    @Query("SELECT * FROM sketches WHERE syncStatus = :status")
    suspend fun getSketchesByStatus(status: String): List<SketchEntity>

    /**
     * Update sync status for a sketch
     */
    @Query("UPDATE sketches SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}
