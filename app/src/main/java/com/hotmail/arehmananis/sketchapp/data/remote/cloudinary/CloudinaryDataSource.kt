package com.hotmail.arehmananis.sketchapp.data.remote.cloudinary

/**
 * Interface for Cloudinary cloud storage operations
 * Pure Kotlin interface - KMP ready
 * Implementation is Android-specific (wraps Cloudinary Android SDK)
 */
interface CloudinaryDataSource {
    /**
     * Upload image file to Cloudinary
     *
     * @param localFilePath Absolute path to local file
     * @param publicId Cloudinary public ID (e.g., "userId/sketchId")
     * @return Upload result with URLs on success, error on failure
     */
    suspend fun uploadImage(
        localFilePath: String,
        publicId: String
    ): Result<CloudinaryUploadResult>

    /**
     * Delete image from Cloudinary
     * Note: For security, deletion should be done server-side.
     * This is a placeholder for client-side deletion.
     *
     * @param publicId Cloudinary public ID
     * @return Success or failure
     */
    suspend fun deleteImage(publicId: String): Result<Unit>

    /**
     * Generate transformation URL for thumbnail
     * No upload needed - Cloudinary generates thumbnails on-demand via URL
     *
     * @param publicId Cloudinary public ID
     * @param width Thumbnail width in pixels
     * @param height Thumbnail height in pixels
     * @return Thumbnail URL
     */
    fun generateThumbnailUrl(
        publicId: String,
        width: Int = 200,
        height: Int = 200
    ): String

    /**
     * Generate optimized image URL
     * Cloudinary auto-selects best format (WebP, AVIF) and quality
     *
     * @param publicId Cloudinary public ID
     * @return Optimized URL
     */
    fun generateOptimizedUrl(publicId: String): String
}

/**
 * Result of Cloudinary upload
 * Pure Kotlin data class - KMP ready
 *
 * @param publicId Cloudinary public ID that was uploaded
 * @param originalUrl URL to original uploaded image
 * @param optimizedUrl URL with automatic format/quality optimization
 * @param thumbnailUrl URL with thumbnail transformation (200x200)
 */
data class CloudinaryUploadResult(
    val publicId: String,
    val originalUrl: String,
    val optimizedUrl: String,
    val thumbnailUrl: String
)
