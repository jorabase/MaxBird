package com.example.common.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserAcademicProfileEntity
 */
@Dao
interface UserAcademicProfileDao {

    @Query("SELECT * FROM user_academic_profile WHERE userId = :userId LIMIT 1")
    fun observeProfile(userId: String): Flow<UserAcademicProfileEntity?>

    @Query("SELECT * FROM user_academic_profile WHERE userId = :userId LIMIT 1")
    suspend fun getProfile(userId: String): UserAcademicProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserAcademicProfileEntity)

    @Query("DELETE FROM user_academic_profile WHERE userId = :userId")
    suspend fun deleteProfile(userId: String)
}
