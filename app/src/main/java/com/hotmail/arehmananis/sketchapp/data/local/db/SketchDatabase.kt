package com.hotmail.arehmananis.sketchapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hotmail.arehmananis.sketchapp.data.local.db.dao.SketchDao
import com.hotmail.arehmananis.sketchapp.data.local.db.entity.SketchEntity

/**
 * Room database for sketch offline storage
 */
@Database(
    entities = [SketchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SketchDatabase : RoomDatabase() {
    abstract fun sketchDao(): SketchDao
}
