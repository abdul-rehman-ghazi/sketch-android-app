package com.hotmail.arehmananis.sketchapp.domain.model

/**
 * Domain model representing a sketch
 * Pure Kotlin - KMP Ready (no Android dependencies)
 */
data class Sketch(
    val id: String,
    val title: String,
    val userId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val localImagePath: String? = null,
    val remoteImageUrl: String? = null,
    val thumbnailUrl: String? = null,
    val remotePathsUrl: String? = null, // Cloudinary URL to paths JSON file
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val width: Int,
    val height: Int,
    val drawingPaths: List<DrawingPath>? = null, // Vector path data for editing
    val emojiElements: List<EmojiElement>? = null, // Emoji elements on canvas
    val imageElements: List<ImageElement>? = null // Imported image elements on canvas
)

/**
 * Sync status for local-first architecture
 */
enum class SyncStatus {
    SYNCED,           // In sync with cloud
    PENDING_UPLOAD,   // Created locally, needs upload
    PENDING_DOWNLOAD, // Exists in cloud, needs download
    SYNCING,          // Currently syncing
    CONFLICT          // Conflict between local and remote
}
