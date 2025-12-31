package com.hotmail.arehmananis.sketchapp.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.hotmail.arehmananis.sketchapp.data.local.db.dao.SketchDao
import com.hotmail.arehmananis.sketchapp.data.local.db.entity.SketchEntity
import com.hotmail.arehmananis.sketchapp.data.local.serializer.PathSerializer
import com.hotmail.arehmananis.sketchapp.data.remote.cloudinary.CloudinaryDataSource
import com.hotmail.arehmananis.sketchapp.data.remote.firebase.dto.SketchDto
import com.hotmail.arehmananis.sketchapp.domain.model.DrawingPath
import com.hotmail.arehmananis.sketchapp.domain.model.Sketch
import com.hotmail.arehmananis.sketchapp.domain.model.SyncStatus
import com.hotmail.arehmananis.sketchapp.domain.repository.SketchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * Implementation of SketchRepository with local-first architecture
 * - Room is the source of truth
 * - All reads come from Room
 * - Writes go to Room first, then sync to Cloudinary in background
 */
class SketchRepositoryImpl(
    private val sketchDao: SketchDao,
    private val firestore: FirebaseFirestore,
    private val cloudinary: CloudinaryDataSource
) : SketchRepository {

    companion object {
        private const val TAG = "SketchRepositoryImpl"
        private const val SKETCHES_COLLECTION = "sketches"
    }

    override fun getUserSketches(userId: String): Flow<List<Sketch>> {
        return sketchDao.getUserSketches(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSketchById(id: String): Sketch? {
        return sketchDao.getSketchById(id)?.toDomain()
    }

    override suspend fun createSketch(sketch: Sketch): Result<Sketch> {
        return try {
            // Save to Room with PENDING_UPLOAD status
            val sketchToSave = sketch.copy(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                updatedAt = System.currentTimeMillis()
            )
            sketchDao.insertSketch(SketchEntity.fromDomain(sketchToSave))

            Log.d(TAG, "Sketch created locally: ${sketch.id}")
            Result.success(sketchToSave)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create sketch", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSketch(sketch: Sketch): Result<Sketch> {
        return try {
            val updated = sketch.copy(
                syncStatus = SyncStatus.PENDING_UPLOAD,
                updatedAt = System.currentTimeMillis()
            )
            sketchDao.updateSketch(SketchEntity.fromDomain(updated))

            Log.d(TAG, "Sketch updated locally: ${sketch.id}")
            Result.success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update sketch", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSketch(id: String): Result<Unit> {
        return try {
            val sketch = sketchDao.getSketchById(id)

            if (sketch != null) {
                // Delete local image file
                sketch.localImagePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        file.delete()
                        Log.d(TAG, "Deleted local image: $path")
                    }
                }

                // Delete from Room
                sketchDao.deleteSketchById(id)
                Log.d(TAG, "Sketch deleted from Room: $id")

                // Delete from Cloud (if exists remotely)
                try {
                    // Delete Firestore document
                    firestore.collection(SKETCHES_COLLECTION)
                        .document(id)
                        .delete()
                        .await()
                    Log.d(TAG, "Sketch deleted from Firestore: $id")

                    // Delete from Cloudinary (image and paths JSON)
                    if (sketch.remoteImageUrl != null || sketch.remotePathsUrl != null) {
                        try {
                            val publicId = "${sketch.userId}/${sketch.id}"
                            cloudinary.deleteImage(publicId).getOrThrow()
                            Log.d(TAG, "Deleted from Cloudinary: $publicId")
                        } catch (e: Exception) {
                            // Log but don't fail - local deletion already succeeded
                            Log.w(TAG, "Failed to delete from Cloudinary: $id", e)
                        }
                    }
                } catch (e: Exception) {
                    // Cloud deletion failed, but local deletion succeeded
                    Log.w(TAG, "Failed to delete from cloud: $id", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete sketch", e)
            Result.failure(e)
        }
    }

    override suspend fun syncSketches(userId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Starting sync for user: $userId")

            // Step 1: Upload pending sketches to Firebase
            val pendingUpload = sketchDao.getSketchesByStatus(SyncStatus.PENDING_UPLOAD.name)
            Log.d(TAG, "Found ${pendingUpload.size} sketches pending upload")

            pendingUpload.forEach { entity ->
                uploadSketch(entity)
            }

            // Step 2: Download new remote sketches
            downloadRemoteSketches(userId)

            Log.d(TAG, "Sync completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.failure(e)
        }
    }

    /**
     * Upload drawing paths JSON to Cloudinary
     * Creates temp file, uploads as raw file, returns URL
     */
    private suspend fun uploadPathsJson(
        jsonContent: String,
        publicId: String
    ): Result<String> {
        var tempFile: File? = null
        return try {
            // Create temp JSON file
            tempFile = File.createTempFile("paths_${publicId.replace("/", "_")}", ".json")
            tempFile.writeText(jsonContent)

            Log.d(TAG, "Created temp paths file: ${tempFile.absolutePath}")

            // Upload to Cloudinary with resource_type="raw"
            val uploadResult = cloudinary.uploadRawFile(
                localFilePath = tempFile.absolutePath,
                publicId = publicId,
                folder = "sketch_paths"
            ).getOrThrow()

            Log.d(TAG, "Paths JSON uploaded: ${uploadResult.secureUrl}")
            Result.success(uploadResult.secureUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload paths JSON for $publicId", e)
            Result.failure(e)
        } finally {
            // Clean up temp file
            tempFile?.let {
                if (it.exists()) {
                    it.delete()
                    Log.d(TAG, "Deleted temp paths file: ${it.absolutePath}")
                }
            }
        }
    }

    /**
     * Download and cache drawing paths from Cloudinary
     * If paths already cached in Room, return cached version
     * If remotePathsUrl exists, download and cache in Room
     *
     * @param sketchId Sketch ID to download paths for
     * @return List of DrawingPath on success, empty list if no paths available
     */
    override suspend fun downloadAndCachePaths(sketchId: String): Result<List<DrawingPath>> {
        return try {
            val entity = sketchDao.getSketchById(sketchId)
                ?: return Result.failure(Exception("Sketch not found: $sketchId"))

            // Check if paths already cached in Room
            if (!entity.drawingPathsJson.isNullOrBlank()) {
                Log.d(TAG, "Paths already cached for sketch: $sketchId")
                val paths = PathSerializer.fromJson(entity.drawingPathsJson)
                    ?: return Result.failure(Exception("Failed to deserialize cached paths"))
                return Result.success(paths)
            }

            // Check if remotePathsUrl exists
            val remoteUrl = entity.remotePathsUrl
            if (remoteUrl.isNullOrBlank()) {
                Log.d(TAG, "No remote paths URL for sketch: $sketchId (legacy sketch)")
                return Result.success(emptyList())
            }

            // Download paths from Cloudinary
            Log.d(TAG, "Downloading paths from: $remoteUrl")
            val downloadResult = cloudinary.downloadRawFile(remoteUrl)

            if (downloadResult.isFailure) {
                Log.e(TAG, "Failed to download paths from Cloudinary", downloadResult.exceptionOrNull())
                return Result.failure(downloadResult.exceptionOrNull() ?: Exception("Download failed"))
            }

            val jsonContent = downloadResult.getOrNull()
                ?: return Result.failure(Exception("Downloaded content is null"))

            // Deserialize JSON to DrawingPath list
            val paths = PathSerializer.fromJson(jsonContent)
                ?: return Result.failure(Exception("Failed to deserialize downloaded paths"))

            Log.d(TAG, "Downloaded ${paths.size} paths for sketch: $sketchId")

            // Cache in Room for offline access
            val updatedEntity = entity.copy(drawingPathsJson = jsonContent)
            sketchDao.updateSketch(updatedEntity)

            Log.d(TAG, "Paths cached in Room for sketch: $sketchId")
            Result.success(paths)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading and caching paths for $sketchId", e)
            Result.failure(e)
        }
    }

    /**
     * Upload a single sketch to Cloudinary + Firestore
     */
    private suspend fun uploadSketch(entity: SketchEntity) {
        try {
            Log.d(TAG, "Uploading sketch: ${entity.id}")

            // Update status to SYNCING (use full update to trigger Flow emission)
            val syncingEntity = entity.copy(syncStatus = SyncStatus.SYNCING.name)
            sketchDao.updateSketch(syncingEntity)

            // Upload image to Cloudinary
            val localPath = entity.localImagePath
                ?: throw Exception("No local image path for sketch: ${entity.id}")

            val imageFile = File(localPath)
            if (!imageFile.exists()) {
                throw Exception("Local image file not found: $localPath")
            }

            // Public ID format: userId/sketchId
            val publicId = "${entity.userId}/${entity.id}"

            val uploadResult = cloudinary.uploadImage(localPath, publicId).getOrThrow()

            Log.d(TAG, "Image uploaded to Cloudinary: ${entity.id}")
            Log.d(TAG, "Original URL: ${uploadResult.originalUrl}")
            Log.d(TAG, "Optimized URL: ${uploadResult.optimizedUrl}")
            Log.d(TAG, "Thumbnail URL: ${uploadResult.thumbnailUrl}")

            // Upload drawing paths JSON to Cloudinary (if available)
            var remotePathsUrl: String? = null
            if (!entity.drawingPathsJson.isNullOrBlank()) {
                Log.d(TAG, "Uploading drawing paths JSON for: ${entity.id}")
                val pathsUploadResult = uploadPathsJson(entity.drawingPathsJson, publicId)

                if (pathsUploadResult.isSuccess) {
                    remotePathsUrl = pathsUploadResult.getOrNull()
                    Log.d(TAG, "Paths uploaded successfully: $remotePathsUrl")
                } else {
                    // Log warning but don't fail the entire upload
                    // Sketch can still work with image only
                    Log.w(TAG, "Failed to upload paths JSON, continuing with image only", pathsUploadResult.exceptionOrNull())
                }
            }

            // Save metadata to Firestore with Cloudinary URLs
            val sketchDto = SketchDto.fromDomain(
                entity.toDomain().copy(
                    remoteImageUrl = uploadResult.optimizedUrl,
                    thumbnailUrl = uploadResult.thumbnailUrl,
                    remotePathsUrl = remotePathsUrl
                )
            )

            firestore.collection(SKETCHES_COLLECTION)
                .document(entity.id)
                .set(sketchDto)
                .await()

            Log.d(TAG, "Metadata saved to Firestore: ${entity.id}")

            // Update Room with Cloudinary URLs and SYNCED status
            val updated = entity.copy(
                remoteImageUrl = uploadResult.optimizedUrl,
                thumbnailUrl = uploadResult.thumbnailUrl,
                remotePathsUrl = remotePathsUrl,
                syncStatus = SyncStatus.SYNCED.name
            )
            sketchDao.updateSketch(updated)

            Log.d(TAG, "Upload completed: ${entity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload sketch: ${entity.id}", e)
            // Revert to PENDING_UPLOAD on error (use full update to trigger Flow emission)
            val failedEntity = entity.copy(syncStatus = SyncStatus.PENDING_UPLOAD.name)
            sketchDao.updateSketch(failedEntity)
        }
    }

    /**
     * Download remote sketches that don't exist locally
     */
    private suspend fun downloadRemoteSketches(userId: String) {
        try {
            Log.d(TAG, "Downloading remote sketches for user: $userId")

            val remoteSketches = firestore.collection(SKETCHES_COLLECTION)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            Log.d(TAG, "Found ${remoteSketches.size()} remote sketches")

            remoteSketches.documents.forEach { doc ->
                val remoteSketch = doc.toObject(SketchDto::class.java)

                if (remoteSketch != null) {
                    val localSketch = sketchDao.getSketchById(remoteSketch.id)

                    when {
                        localSketch == null -> {
                            // New remote sketch - download metadata (not image file)
                            Log.d(TAG, "Downloading new sketch metadata: ${remoteSketch.id}")
                            val entity = SketchEntity.fromDomain(
                                Sketch(
                                    id = remoteSketch.id,
                                    title = remoteSketch.title,
                                    userId = remoteSketch.userId,
                                    createdAt = remoteSketch.createdAt,
                                    updatedAt = remoteSketch.updatedAt,
                                    localImagePath = null, // Will show from Cloudinary URL
                                    remoteImageUrl = remoteSketch.remoteImageUrl,
                                    thumbnailUrl = remoteSketch.thumbnailUrl,
                                    remotePathsUrl = remoteSketch.remotePathsUrl,
                                    syncStatus = SyncStatus.SYNCED,
                                    width = remoteSketch.width,
                                    height = remoteSketch.height
                                )
                            )
                            sketchDao.insertSketch(entity)
                        }

                        localSketch.syncStatus == SyncStatus.SYNCED.name -> {
                            // Check for conflicts (last-write-wins)
                            if (remoteSketch.updatedAt > localSketch.updatedAt) {
                                Log.d(TAG, "Remote sketch is newer, updating: ${remoteSketch.id}")
                                // Remote is newer, update local
                                val updated = localSketch.copy(
                                    title = remoteSketch.title,
                                    updatedAt = remoteSketch.updatedAt,
                                    remoteImageUrl = remoteSketch.remoteImageUrl,
                                    thumbnailUrl = remoteSketch.thumbnailUrl,
                                    remotePathsUrl = remoteSketch.remotePathsUrl,
                                    width = remoteSketch.width,
                                    height = remoteSketch.height
                                )
                                sketchDao.updateSketch(updated)
                            }
                        }
                        // If local is PENDING_UPLOAD, don't override with remote
                    }
                }
            }

            Log.d(TAG, "Download completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download remote sketches", e)
            throw e
        }
    }
}
