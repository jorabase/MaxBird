package com.example.common.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing the locally cached active academic syllabus profile.
 */
@Entity(tableName = "user_academic_profile")
data class UserAcademicProfileEntity(
    @PrimaryKey
    val userId: String,
    val classId: String,
    val classTitleBn: String,
    val batchYear: String?,
    val groupCode: String?,
    val groupTitleBn: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
