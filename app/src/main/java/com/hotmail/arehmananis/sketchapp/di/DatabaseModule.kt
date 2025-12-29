package com.hotmail.arehmananis.sketchapp.di

import androidx.room.Room
import com.hotmail.arehmananis.sketchapp.data.local.db.SketchDatabase
import com.hotmail.arehmananis.sketchapp.data.local.db.migrations.MIGRATION_1_2
import com.hotmail.arehmananis.sketchapp.data.local.db.migrations.MIGRATION_2_3
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for Room database dependencies
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SketchDatabase::class.java,
            "sketch_database"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    single { get<SketchDatabase>().sketchDao() }
}
