package com.hotmail.arehmananis.sketchapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hotmail.arehmananis.sketchapp.data.local.serializer.EmojiSerializer
import com.hotmail.arehmananis.sketchapp.data.local.serializer.ImageSerializer
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
    val remoteEmojisUrl: String? = null, // Cloudinary URL to emojis JSON file
    val syncStatus: String, // Stored as String, converted to/from enum
    val width: Int,
    val height: Int,
    val drawingPathsJson: String? = null,
    val emojiElementsJson: String? = null,
    val imageElementsJson: String? = null
) {
    fun toDomain(): Sketch {
        val paths = drawingPathsJson?.let { PathSerializer.fromJson(it) }
        val emojis = emojiElementsJson?.let { EmojiSerializer.fromJson(it) }
        val images = imageElementsJson?.let { ImageSerializer.fromJson(it) }

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
            remoteEmojisUrl = remoteEmojisUrl,
            syncStatus = SyncStatus.valueOf(syncStatus),
            width = width,
            height = height,
            drawingPaths = paths,
            emojiElements = emojis,
            imageElements = images
        )
    }

    companion object {
        fun fromDomain(sketch: Sketch): SketchEntity {
            val pathsJson = sketch.drawingPaths?.let { PathSerializer.toJson(it) }
            val emojisJson = sketch.emojiElements?.let { EmojiSerializer.toJson(it) }
            val imagesJson = sketch.imageElements?.let { ImageSerializer.toJson(it) }

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
                remoteEmojisUrl = sketch.remoteEmojisUrl,
                syncStatus = sketch.syncStatus.name,
                width = sketch.width,
                height = sketch.height,
                drawingPathsJson = pathsJson,
                emojiElementsJson = emojisJson,
                imageElementsJson = imagesJson
            )
        }
    }
}
