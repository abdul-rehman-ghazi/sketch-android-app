package com.hotmail.arehmananis.sketchapp.domain.repository

/**
 * Repository for scheduling background sync operations
 * Platform-agnostic interface - implementation will use WorkManager on Android
 */
interface SyncSchedulerRepository {
    /**
     * Trigger an immediate one-time sync of sketches
     */
    fun triggerImmediateSync()
}