package com.hotmail.arehmananis.sketchapp.data.remote.cloudinary

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
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

    override suspend fun deleteImage(publicId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Deleting resources for publicId: $publicId")

            // Delete image resource (resource_type=image)
            val imagePublicId = "$FOLDER_PATH/$publicId"
            deleteResource(imagePublicId, "image").getOrThrow()
            Log.d(TAG, "Deleted image: $imagePublicId")

            // Delete paths JSON (resource_type=raw)
            val pathsPublicId = "sketch_paths/$publicId"
            try {
                deleteResource(pathsPublicId, "raw").getOrThrow()
                Log.d(TAG, "Deleted paths JSON: $pathsPublicId")
            } catch (e: Exception) {
                // Paths might not exist for older sketches, just log
                Log.w(TAG, "Failed to delete paths (may not exist): $pathsPublicId", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Delete error for: $publicId", e)
            Result.failure(CloudinaryDeleteException(e.message ?: "Unknown error"))
        }
    }

    /**
     * Delete a resource from Cloudinary using Admin API
     * Note: This exposes API secret client-side. For production, use backend proxy.
     */
    private fun deleteResource(publicId: String, resourceType: String): Result<Unit> {
        return try {
            val timestamp = (System.currentTimeMillis() / 1000).toString()

            // Generate signature for authentication
            // signature = SHA1(public_id={publicId}&timestamp={timestamp}{api_secret})
            val stringToSign = "public_id=$publicId&timestamp=$timestamp${config.apiSecret}"
            val signature = generateSHA1(stringToSign)

            // Build request URL
            val url = "https://api.cloudinary.com/v1_1/${config.cloudName}/$resourceType/destroy"

            // Make HTTP POST request
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Build POST body
            val postData = buildString {
                append("public_id=").append(URLEncoder.encode(publicId, "UTF-8"))
                append("&timestamp=").append(timestamp)
                append("&api_key=").append(config.apiKey)
                append("&signature=").append(signature)
            }

            // Send request
            connection.outputStream.use { it.write(postData.toByteArray()) }

            // Check response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "Successfully deleted $resourceType: $publicId")
                Result.success(Unit)
            } else {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e(TAG, "Delete failed with code $responseCode: $errorBody")
                Result.failure(CloudinaryDeleteException("Delete failed: HTTP $responseCode - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting resource: $publicId", e)
            Result.failure(e)
        }
    }

    /**
     * Generate SHA-1 hash for Cloudinary signature
     */
    private fun generateSHA1(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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

    override suspend fun uploadRawFile(
        localFilePath: String,
        publicId: String,
        folder: String
    ): Result<CloudinaryRawUploadResult> = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Starting raw file upload: $publicId from $localFilePath")

            val requestId = mediaManager.upload(localFilePath)
                .option("public_id", publicId)
                .option("folder", folder)
                .option("resource_type", "raw") // Key: upload as raw file (not image)
                .option("overwrite", true)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Raw file upload started: $requestId")
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = (bytes * 100 / totalBytes).toInt()
                        Log.d(TAG, "Raw upload progress: $progress% ($bytes/$totalBytes bytes)")
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        Log.d(TAG, "Raw file upload successful: $resultData")

                        try {
                            val uploadedPublicId = resultData["public_id"] as? String ?: publicId
                            val secureUrl = resultData["secure_url"] as? String
                                ?: throw CloudinaryUploadException("No secure_url in response")

                            val result = CloudinaryRawUploadResult(
                                publicId = uploadedPublicId,
                                secureUrl = secureUrl
                            )

                            Log.d(TAG, "Raw upload complete - URL: $secureUrl")
                            continuation.resume(Result.success(result))
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing upload result", e)
                            continuation.resume(
                                Result.failure(CloudinaryUploadException("Upload succeeded but failed to process result: ${e.message}"))
                            )
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        Log.e(TAG, "Raw file upload failed: ${error.description}")
                        val exception = when {
                            error.description.contains("network", ignoreCase = true) ->
                                CloudinaryNetworkException(error.description)

                            else ->
                                CloudinaryUploadException(error.description)
                        }
                        continuation.resume(Result.failure(exception))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Raw file upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()

            continuation.invokeOnCancellation {
                Log.d(TAG, "Raw file upload cancelled, cancelling request: $requestId")
                mediaManager.cancelRequest(requestId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Raw file upload error", e)
            continuation.resume(
                Result.failure(CloudinaryUploadException("Failed to initiate upload: ${e.message}"))
            )
        }
    }

    override suspend fun downloadRawFile(url: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Downloading raw file: $url")

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val content = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d(TAG, "Raw file downloaded successfully, size: ${content.length} chars")
                    Result.success(content)
                } else {
                    Log.e(TAG, "Download failed with code: $responseCode")
                    Result.failure(CloudinaryUploadException("Download failed: HTTP $responseCode"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Download error", e)
                Result.failure(CloudinaryNetworkException(e.message ?: "Download failed"))
            }
        }
}
