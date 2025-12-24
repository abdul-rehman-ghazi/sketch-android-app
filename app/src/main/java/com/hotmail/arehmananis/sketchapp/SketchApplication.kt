package com.hotmail.arehmananis.sketchapp

import android.app.Application
import com.hotmail.arehmananis.sketchapp.data.sync.SketchSyncWorker
import com.hotmail.arehmananis.sketchapp.di.appModule
import com.hotmail.arehmananis.sketchapp.di.cloudinaryModule
import com.hotmail.arehmananis.sketchapp.di.databaseModule
import com.hotmail.arehmananis.sketchapp.di.firebaseModule
import com.hotmail.arehmananis.sketchapp.di.networkModule
import com.hotmail.arehmananis.sketchapp.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class for SketchApp
 * Initializes Koin dependency injection
 */
class SketchApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin
        startKoin {
            // Enable logging in debug builds
            if (BuildConfig.DEBUG) {
                androidLogger(Level.ERROR)
            }

            // Provide Android context
            androidContext(this@SketchApplication)

            // Load modules
            modules(
                appModule,
                networkModule,
                firebaseModule,
                cloudinaryModule,
                databaseModule,
                repositoryModule
            )
        }

        // Schedule periodic sketch sync
        SketchSyncWorker.schedule(this)
    }
}
