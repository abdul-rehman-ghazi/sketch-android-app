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

    /**
     * Upload raw file (JSON, text, etc.) to Cloudinary
     *
     * @param localFilePath Absolute path to local file
     * @param publicId Cloudinary public ID (e.g., "userId/sketchId")
     * @param folder Folder path in Cloudinary (e.g., "sketch_paths")
     * @return Upload result with secure URL on success, error on failure
     */
    suspend fun uploadRawFile(
        localFilePath: String,
        publicId: String,
        folder: String = "sketch_paths"
    ): Result<CloudinaryRawUploadResult>

    /**
     * Download raw file content from Cloudinary
     *
     * @param url Full URL to the raw file
     * @return File content as String on success, error on failure
     */
    suspend fun downloadRawFile(url: String): Result<String>
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

/**
 * Result of raw file upload to Cloudinary
 * Pure Kotlin data class - KMP ready
 *
 * @param publicId Cloudinary public ID that was uploaded
 * @param secureUrl HTTPS URL to the uploaded file
 */
data class CloudinaryRawUploadResult(
    val publicId: String,
    val secureUrl: String
)
