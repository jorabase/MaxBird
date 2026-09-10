package com.example.common.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity for offline-first caching of the full Server-Driven Academic Config
 * (Classes, Batches, Groups) retrieved from GET /api/v1/academic/config.
 */
@Entity(tableName = "cached_academic_config")
data class CachedAcademicConfigEntity(
    @PrimaryKey
    val id: String = "global_academic_config",
    val configJson: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Data Access Object for cached academic config
 */
@Dao
interface CachedAcademicConfigDao {

    @Query("SELECT * FROM cached_academic_config WHERE id = :id LIMIT 1")
    fun observeConfig(id: String = "global_academic_config"): Flow<CachedAcademicConfigEntity?>

    @Query("SELECT * FROM cached_academic_config WHERE id = :id LIMIT 1")
    suspend fun getConfig(id: String = "global_academic_config"): CachedAcademicConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(entity: CachedAcademicConfigEntity)

    @Query("DELETE FROM cached_academic_config")
    suspend fun clearConfig()
}
