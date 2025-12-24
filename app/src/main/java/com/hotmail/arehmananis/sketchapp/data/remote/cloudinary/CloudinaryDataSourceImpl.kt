package com.hotmail.arehmananis.sketchapp.data.remote.cloudinary

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Implementation of CloudinaryDataSource using Cloudinary Android SDK
 * Android-specific - wraps Cloudinary SDK
 * For KMP migration: Create iOS implementation using Cloudinary iOS SDK
 */
class CloudinaryDataSourceImpl(
    private val context: Context,
    private val config: CloudinaryConfig
) : CloudinaryDataSource {

    companion object {
        private const val TAG = "CloudinaryDataSource"
        private const val FOLDER_PATH = "sketch_images"
    }

    /**
     * Initialize MediaManager on first use (lazy)
     * This ensures Cloudinary SDK is initialized only when needed
     */
    private val mediaManager: MediaManager by lazy {
        MediaManager.init(
            context,
            mapOf(
                "cloud_name" to config.cloudName,
                "api_key" to config.apiKey,
                "api_secret" to config.apiSecret,
                "secure" to config.secure
            )
        )
        MediaManager.get()
    }

    override suspend fun uploadImage(
        localFilePath: String,
        publicId: String
    ): Result<CloudinaryUploadResult> = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Starting upload: $publicId from $localFilePath")

            val requestId = mediaManager.upload(localFilePath)
                .option("public_id", publicId)
                .option("folder", FOLDER_PATH)
                .option("resource_type", "image")
                .option("overwrite", true) // Overwrite if exists
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d(TAG, "Upload progress: $progress% ($bytes/$totalBytes bytes)")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        Log.d(TAG, "Upload successful: $resultData")

                        try {
                            val uploadedPublicId = resultData["public_id"] as? String ?: publicId
                            val secureUrl = resultData["secure_url"] as? String
                                ?: throw CloudinaryUploadException("No secure_url in response")

                            val result = CloudinaryUploadResult(
                                publicId = uploadedPublicId,
                                originalUrl = secureUrl,
                                optimizedUrl = generateOptimizedUrl(uploadedPublicId),
                                thumbnailUrl = generateThumbnailUrl(uploadedPublicId)
                            )

                            Log.d(TAG, "Upload complete - Original: $secureUrl")
                            Log.d(TAG, "Optimized URL: ${result.optimizedUrl}")
                            Log.d(TAG, "Thumbnail URL: ${result.thumbnailUrl}")

                            continuation.resume(Result.success(result))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing upload result", e)
                            continuation.resume(
                                Result.failure(CloudinaryUploadException("Upload succeeded but failed to process result: ${e.message}"))
                            )
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Upload failed: ${error.description}")
                        val exception = when {
                            error.description.contains("network", ignoreCase = true) ->
                                CloudinaryNetworkException(error.description)
                            else ->
                                CloudinaryUploadException(error.description)
                        }
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Upload rescheduled: ${error.description}")
                        // Cloudinary will automatically retry
                    }
                })
                .dispatch()

            // Handle coroutine cancellation
            continuation.invokeOnCancellation {
                Log.d(TAG, "Upload cancelled, cancelling request: $requestId")
                mediaManager.cancelRequest(requestId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            continuation.resume(
                Result.failure(
                    CloudinaryUploadException("Failed to initiate upload: ${e.message}")
                )
            )
        }
    }

    override suspend fun deleteImage(publicId: String): Result<Unit> {
        return try {
            // Note: Cloudinary delete requires Admin API which should be done server-side
            // for security reasons (requires API secret).
            //
            // For now, we'll just log the deletion request.
            // Actual deletion should be implemented via:
            // 1. Backend API call that uses Cloudinary Admin API
            // 2. Cloudinary auto-deletion policies (delete after X days of no access)
            // 3. Manual cleanup via Cloudinary dashboard

            Log.w(TAG, "Delete requested for: $publicId")
            Log.w(TAG, "Client-side deletion not implemented for security.")
            Log.w(TAG, "Implement server-side deletion or use Cloudinary auto-deletion policies.")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete error", e)
            Result.failure(CloudinaryDeleteException(e.message ?: "Unknown error"))
        }
    }

    override fun generateThumbnailUrl(publicId: String, width: Int, height: Int): String {
        // Generate thumbnail URL with transformations manually
        // Format: https://res.cloudinary.com/{cloud}/image/upload/w_200,h_200,c_fill,g_auto,f_auto,q_auto/{publicId}
        val cloudName = config.cloudName
        val secure = if (config.secure) "https" else "http"

        return "$secure://res.cloudinary.com/$cloudName/image/upload/w_$width,h_$height,c_fill,g_auto,f_auto,q_auto/$publicId"
    }

    override fun generateOptimizedUrl(publicId: String): String {
        // Generate optimized URL with auto format and quality manually
        // Format: https://res.cloudinary.com/{cloud}/image/upload/f_auto,q_auto/{publicId}
        val cloudName = config.cloudName
        val secure = if (config.secure) "https" else "http"

        return "$secure://res.cloudinary.com/$cloudName/image/upload/f_auto,q_auto/$publicId"
    }
}
