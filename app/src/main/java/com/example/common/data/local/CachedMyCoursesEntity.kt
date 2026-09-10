package com.example.common.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.example.common.model.CourseItem
import kotlinx.coroutines.flow.Flow

/**
 * Room Entity for offline-first caching of My Courses per syllabus
 */
@Entity(tableName = "cached_my_courses")
data class CachedMyCoursesEntity(
    @PrimaryKey
    val id: String,
    val classId: String,
    val batchYear: String?,
    val groupCode: String?,
    val title: String,
    val coverUrl: String,
    val enrollmentType: String,
    val trialDaysRemaining: Int?,
    val expiryDate: String?,
    val isLiveNow: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

fun CachedMyCoursesEntity.toDomain(): CourseItem = CourseItem(
    id = id,
    title = title,
    bannerUrl = coverUrl,
    enrollmentStatus = enrollmentType,
    isLiveNow = isLiveNow,
    trialDaysLeft = trialDaysRemaining
)

fun CourseItem.toEntity(classId: String, batchYear: String?, groupCode: String?): CachedMyCoursesEntity =
    CachedMyCoursesEntity(
        id = id,
        classId = classId,
        batchYear = batchYear,
        groupCode = groupCode,
        title = title,
        coverUrl = bannerUrl,
        enrollmentType = enrollmentStatus,
        trialDaysRemaining = trialDaysLeft,
        expiryDate = null,
        isLiveNow = isLiveNow
    )

/**
 * DAO for cached My Courses
 */
@Dao
interface CachedMyCoursesDao {

    @Query("""
        SELECT * FROM cached_my_courses 
        WHERE classId = :classId 
          AND (:batchYear IS NULL OR batchYear = :batchYear) 
          AND (:groupCode IS NULL OR groupCode = :groupCode)
        ORDER BY isLiveNow DESC, cachedAt ASC
    """)
    fun observeCourses(
        classId: String,
        batchYear: String?,
        groupCode: String?
    ): Flow<List<CachedMyCoursesEntity>>

    @Query("""
        SELECT * FROM cached_my_courses 
        WHERE classId = :classId 
          AND (:batchYear IS NULL OR batchYear = :batchYear) 
          AND (:groupCode IS NULL OR groupCode = :groupCode)
    """)
    suspend fun getCourses(
        classId: String,
        batchYear: String?,
        groupCode: String?
    ): List<CachedMyCoursesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CachedMyCoursesEntity>)

    @Query("DELETE FROM cached_my_courses")
    suspend fun clearAll()

    @Query("DELETE FROM cached_my_courses WHERE classId = :classId")
    suspend fun clearByClass(classId: String)

    @Transaction
    suspend fun clearAndInsertForClass(classId: String, courses: List<CachedMyCoursesEntity>) {
        clearByClass(classId)
        insertCourses(courses)
    }

    @Transaction
    suspend fun clearAndInsert(courses: List<CachedMyCoursesEntity>) {
        clearAll()
        insertCourses(courses)
    }
}

typealias CourseDao = CachedMyCoursesDao
