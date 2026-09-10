package com.example.common.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonClass

/**
 * Generic Resource Result wrapper for Clean Architecture Flows
 */
sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val message: String) : Resource<Nothing>
}

/**
 * Domain representation of a Course Item
 */
data class CourseItem(
    val id: String,
    val title: String,
    val bannerUrl: String,
    val enrollmentStatus: String, // "ENROLLED", "TRIAL_ACTIVE", "TRIAL_EXPIRED"
    val isLiveNow: Boolean = false,
    val trialDaysLeft: Int? = null
)

/**
 * UI State representation for dynamic Course Cards
 */
data class CourseCardUIState(
    val courseId: String,
    val title: String,
    val coverUrl: String,
    val tagText: String,
    val tagBgColor: Color,
    val tagTextColor: Color,
    val leftButtonText: String,
    val rightButtonText: String? = null,
    val rightButtonBgColor: Color? = null,
    val isLiveNow: Boolean = false,
    val enrollmentType: String = "ENROLLED",
    val trialDaysRemaining: Int? = null
)

/**
 * Global App Event Bus for reactive module communication
 */
sealed interface AppEvent {
    data class SyllabusUpdated(
        val classId: String,
        val batch: String,
        val group: String?
    ) : AppEvent
}
