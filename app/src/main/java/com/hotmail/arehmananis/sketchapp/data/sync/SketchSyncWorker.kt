package com.hotmail.arehmananis.sketchapp.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.hotmail.arehmananis.sketchapp.domain.repository.AuthRepository
import com.hotmail.arehmananis.sketchapp.domain.usecase.sketch.SyncSketchesUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for periodic sketch synchronization
 */
class SketchSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val syncSketchesUseCase: SyncSketchesUseCase by inject()
    private val authRepository: AuthRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting background sketch sync")

            // Get current user
            val user = authRepository.getCurrentUser().first()

            if (user != null) {
                // Sync sketches
                syncSketchesUseCase(user.uid).fold(
                    onSuccess = {
                        Log.d(TAG, "Background sync completed successfully")
                        Result.success()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Background sync failed: ${error.message}", error)
                        // Retry on failure
                        if (runAttemptCount < 3) {
                            Result.retry()
                        } else {
                            Result.failure()
                        }
                    }
                )
            } else {
                Log.w(TAG, "No authenticated user, skipping sync")
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Background sync error", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SketchSyncWorker"
        private const val WORK_NAME = "sketch_sync"

        /**
         * Schedule periodic sync work
         * Runs every 15 minutes when connected to network
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SketchSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

            Log.d(TAG, "Scheduled periodic sketch sync")
        }

        /**
         * Cancel scheduled sync work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled periodic sketch sync")
        }
    }
}
