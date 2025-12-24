package com.hotmail.arehmananis.sketchapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.model.SyncStatus

/**
 * Room entity for sketch storage
 * Stores SyncStatus as String for Room compatibility
 */
@Entity(tableName = "sketches")
data class SketchEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val localImagePath: String?,
    val remoteImageUrl: String?,
    val thumbnailUrl: String?,
    val syncStatus: String, // Stored as String, converted to/from enum
    val width: Int,
    val height: Int
) {
    /**
     * Convert to domain model
     */
    fun toDomain(): Sketch = Sketch(
        id = id,
        title = title,
        userId = userId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        localImagePath = localImagePath,
        remoteImageUrl = remoteImageUrl,
        thumbnailUrl = thumbnailUrl,
        syncStatus = SyncStatus.valueOf(syncStatus),
        width = width,
        height = height
    )

    companion object {
        /**
         * Convert from domain model
         */
        fun fromDomain(sketch: Sketch): SketchEntity = SketchEntity(
            id = sketch.id,
            title = sketch.title,
            userId = sketch.userId,
            createdAt = sketch.createdAt,
            updatedAt = sketch.updatedAt,
            localImagePath = sketch.localImagePath,
            remoteImageUrl = sketch.remoteImageUrl,
            thumbnailUrl = sketch.thumbnailUrl,
            syncStatus = sketch.syncStatus.name,
            width = sketch.width,
            height = sketch.height
        )
    }
}
