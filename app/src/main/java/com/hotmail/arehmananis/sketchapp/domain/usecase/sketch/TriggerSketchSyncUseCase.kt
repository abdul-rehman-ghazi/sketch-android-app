package com.hotmail.arehmananis.sketchapp.domain.usecase.sketch

import com.hotmail.arehmananis.sketchapp.domain.repository.SyncSchedulerRepository

/**
 * Use case for triggering immediate sketch synchronization
 * Triggers a one-time background sync to upload pending sketches
 */
class TriggerSketchSyncUseCase(
    private val syncSchedulerRepository: SyncSchedulerRepository
) {
    operator fun invoke() {
        syncSchedulerRepository.triggerImmediateSync()
    }
}