package com.example.common.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Main Room Database instance for local persistence.
 */
@Database(
    entities = [
        UserAcademicProfileEntity::class,
        CachedMyCoursesEntity::class,
        CachedAcademicConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userAcademicProfileDao(): UserAcademicProfileDao
    abstract fun cachedMyCoursesDao(): CachedMyCoursesDao
    abstract fun cachedAcademicConfigDao(): CachedAcademicConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_app_academic_v5.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

