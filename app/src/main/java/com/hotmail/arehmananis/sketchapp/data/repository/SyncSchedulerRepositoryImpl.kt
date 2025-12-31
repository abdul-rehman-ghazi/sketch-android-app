package com.hotmail.arehmananis.sketchapp.data.repository

import android.content.Context
import com.hotmail.arehmananis.sketchapp.data.sync.SketchSyncWorker
import com.hotmail.arehmananis.sketchapp.domain.repository.SyncSchedulerRepository

/**
 * Android implementation of SyncSchedulerRepository using WorkManager
 */
class SyncSchedulerRepositoryImpl(
    private val context: Context
) : SyncSchedulerRepository {

    override fun triggerImmediateSync() {
        SketchSyncWorker.syncNow(context)
    }
}