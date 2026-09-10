package com.example.common.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: T? = null,
    @Json(name = "message") val message: String? = null
)

/**
 * Academic Group Data Contract (Science, Humanities, Business Studies, Engineering, etc.)
 */
@JsonClass(generateAdapter = true)
data class AcademicGroup(
    @Json(name = "code") val code: String,       // "SCI", "HUM", "BS"
    @Json(name = "title_bn") val titleBn: String,    // "বিজ্ঞান", "মানবিক", "ব্যবসায় শিক্ষা"
    @Json(name = "badge") val badge: String       // "S", "H", "B"
)

typealias AcademicGroupConfig = AcademicGroup

/**
 * Relational Academic Class Data Contract
 * Each class directly carries its own available batches and groups.
 */
@JsonClass(generateAdapter = true)
data class ClassItem(
    @Json(name = "id") val id: String,
    @Json(name = "class_code") val classCode: String,
    @Json(name = "title_bn") val titleBn: String,
    @Json(name = "badge") val badge: String,
    @Json(name = "badge_type") val badgeType: String = "TEXT",
    @Json(name = "batches") val batches: List<String> = emptyList(),
    @Json(name = "groups") val groups: List<AcademicGroup> = emptyList()
)

typealias AcademicClassConfig = ClassItem
typealias AcademicClass = ClassItem

/**
 * Request and Response payloads for PATCH /api/v2/user/academic-profile
 */
@JsonClass(generateAdapter = true)
data class UpdateSyllabusRequest(
    @Json(name = "class_id") val classId: String,
    @Json(name = "batch") val batch: String? = null,
    @Json(name = "group_code") val groupCode: String? = null
)

@JsonClass(generateAdapter = true)
data class ActiveSyllabusDto(
    @Json(name = "class_id") val classId: String,
    @Json(name = "class_title") val classTitle: String,
    @Json(name = "batch_year") val batchYear: String? = null,
    @Json(name = "group_code") val groupCode: String? = null,
    @Json(name = "group_title") val groupTitle: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateSyllabusData(
    @Json(name = "user_id") val userId: String,
    @Json(name = "active_syllabus") val activeSyllabus: ActiveSyllabusDto
)

@JsonClass(generateAdapter = true)
data class UpdateSyllabusResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: UpdateSyllabusData? = null
)

/**
 * Global App Event Bus for cache eviction and module sync
 */
sealed interface AppGlobalEvent {
    data class SyllabusChanged(
        val userId: String,
        val classId: String,
        val classTitleBn: String,
        val batchYear: String?,
        val groupCode: String?,
        val groupTitleBn: String?
    ) : AppGlobalEvent
}

