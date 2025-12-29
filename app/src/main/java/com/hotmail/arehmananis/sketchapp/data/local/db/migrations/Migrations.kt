package com.hotmail.arehmananis.sketchapp.data.local.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migration from version 1 to 2
 * Adds drawingPathsJson column for storing vector path data
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column (nullable to support existing sketches without path data)
        database.execSQL(
            "ALTER TABLE sketches ADD COLUMN drawingPathsJson TEXT DEFAULT NULL"
        )
    }
}

/**
 * Database migration from version 2 to 3
 * Adds remotePathsUrl column for storing Cloudinary URL to paths JSON file
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new column (nullable for backward compatibility)
        database.execSQL(
            "ALTER TABLE sketches ADD COLUMN remotePathsUrl TEXT DEFAULT NULL"
        )
    }
}
