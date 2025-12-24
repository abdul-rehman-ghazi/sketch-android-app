package com.hotmail.arehmananis.sketchapp.data.remote.firebase.dto

import com.google.firebase.firestore.PropertyName

/**
 * Firebase Firestore DTO for sketch metadata
 * Image data is stored in Cloudinary (or Firebase Storage for legacy), this only contains metadata
 */
data class SketchDto(
    @PropertyName("id") val id: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("userId") val userId: String = "",
    @PropertyName("createdAt") val createdAt: Long = 0L,
    @PropertyName("updatedAt") val updatedAt: Long = 0L,
    @PropertyName("remoteImageUrl") val remoteImageUrl: String? = null,
    @PropertyName("thumbnailUrl") val thumbnailUrl: String? = null,
    @PropertyName("width") val width: Int = 0,
    @PropertyName("height") val height: Int = 0
) {
    companion object {
        /**
         * Convert from domain Sketch model
         * Note: Local-only fields (localImagePath, syncStatus) are not included
         */
        fun fromDomain(sketch: com.hotmail.arehmananis.sketchapp.domain.model.Sketch): SketchDto {
            return SketchDto(
                id = sketch.id,
                title = sketch.title,
                userId = sketch.userId,
                createdAt = sketch.createdAt,
                updatedAt = sketch.updatedAt,
                remoteImageUrl = sketch.remoteImageUrl,
                thumbnailUrl = sketch.thumbnailUrl,
                width = sketch.width,
                height = sketch.height
            )
        }
    }
}
