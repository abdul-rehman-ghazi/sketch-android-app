package com.hotmail.arehmananis.sketchapp.di

import androidx.room.Room
import com.hotmail.arehmananis.sketchapp.data.local.db.SketchDatabase
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
        ).build()
    }

    single { get<SketchDatabase>().sketchDao() }
}
