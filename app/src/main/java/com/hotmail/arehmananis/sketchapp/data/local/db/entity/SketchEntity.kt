package com.hotmail.arehmananis.sketchapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hotmail.arehmananis.sketchapp.data.local.serializer.PathSerializer
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
    val remotePathsUrl: String? = null, // Cloudinary URL to paths JSON file
    val syncStatus: String, // Stored as String, converted to/from enum
    val width: Int,
    val height: Int,
    val drawingPathsJson: String? = null // JSON serialized DrawingPath list
) {
    /**
     * Convert to domain model
     */
    fun toDomain(): Sketch {
        // Deserialize drawingPathsJson to List<DrawingPath>
        val paths = drawingPathsJson?.let { json ->
            PathSerializer.fromJson(json)
        }

        return Sketch(
            id = id,
            title = title,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            localImagePath = localImagePath,
            remoteImageUrl = remoteImageUrl,
            thumbnailUrl = thumbnailUrl,
            remotePathsUrl = remotePathsUrl,
            syncStatus = SyncStatus.valueOf(syncStatus),
            width = width,
            height = height,
            drawingPaths = paths
        )
    }

    companion object {
        /**
         * Convert from domain model
         */
        fun fromDomain(sketch: Sketch): SketchEntity {
            // Serialize drawingPaths to JSON
            val pathsJson = sketch.drawingPaths?.let { paths ->
                PathSerializer.toJson(paths)
            }

            return SketchEntity(
                id = sketch.id,
                title = sketch.title,
                userId = sketch.userId,
                createdAt = sketch.createdAt,
                updatedAt = sketch.updatedAt,
                localImagePath = sketch.localImagePath,
                remoteImageUrl = sketch.remoteImageUrl,
                thumbnailUrl = sketch.thumbnailUrl,
                remotePathsUrl = sketch.remotePathsUrl,
                syncStatus = sketch.syncStatus.name,
                width = sketch.width,
                height = sketch.height,
                drawingPathsJson = pathsJson
            )
        }
    }
}
