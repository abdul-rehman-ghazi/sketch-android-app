package com.hotmail.arehmananis.sketchapp.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.hotmail.arehmananis.sketchapp.data.local.db.dao.SketchDao
import com.hotmail.arehmananis.sketchapp.data.local.db.entity.SketchEntity
import com.hotmail.arehmananis.sketchapp.data.remote.cloudinary.CloudinaryDataSource
import com.hotmail.arehmananis.sketchapp.data.remote.firebase.dto.SketchDto
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

                    // Note: Cloudinary deletion should be done server-side for security
                    // For now, just delete the Firestore metadata
                    // The image will remain in Cloudinary until manually cleaned up
                    // or through Cloudinary auto-deletion policies

                    Log.d(TAG, "Sketch deleted from Firestore: $id")
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
     * Upload a single sketch to Cloudinary + Firestore
     */
    private suspend fun uploadSketch(entity: SketchEntity) {
        try {
            Log.d(TAG, "Uploading sketch: ${entity.id}")

            // Update status to SYNCING
            sketchDao.updateSyncStatus(entity.id, SyncStatus.SYNCING.name)

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

            // Save metadata to Firestore with Cloudinary URLs
            val sketchDto = SketchDto.fromDomain(
                entity.toDomain().copy(
                    remoteImageUrl = uploadResult.optimizedUrl,
                    thumbnailUrl = uploadResult.thumbnailUrl
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
                syncStatus = SyncStatus.SYNCED.name
            )
            sketchDao.updateSketch(updated)

            Log.d(TAG, "Upload completed: ${entity.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload sketch: ${entity.id}", e)
            // Revert to PENDING_UPLOAD on error
            sketchDao.updateSyncStatus(entity.id, SyncStatus.PENDING_UPLOAD.name)
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
