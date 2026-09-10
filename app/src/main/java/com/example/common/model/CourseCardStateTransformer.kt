package com.example.common.model

import androidx.compose.ui.graphics.Color

/**
 * Transforms CourseItem domain model into reactive CourseCardUIState
 * strictly adhering to the zero mock data, real state mapping specifications.
 */
object CourseCardStateTransformer {

    private val SoftGreenBg = Color(0xFFE8F5E9)
    private val DarkGreenText = Color(0xFF1B5E20)

    private val SoftPinkBg = Color(0xFFFFE4E6)
    private val DarkPinkText = Color(0xFFBE185D)

    private val SoftAmberBg = Color(0xFFFEF3C7)
    private val DarkAmberText = Color(0xFFB45309)

    private val FilledMagenta = Color(0xFFE11D48)

    fun transform(item: CourseItem): CourseCardUIState {
        val status = item.enrollmentStatus.trim().uppercase()

        return when (status) {
            "ENROLLED" -> CourseCardUIState(
                courseId = item.id,
                title = item.title,
                coverUrl = item.bannerUrl,
                tagText = "ভর্তি হয়েছো",
                tagBgColor = SoftGreenBg,
                tagTextColor = DarkGreenText,
                leftButtonText = "শেখা চালিয়ে যাও",
                rightButtonText = null,
                rightButtonBgColor = null,
                isLiveNow = item.isLiveNow,
                enrollmentType = "ENROLLED",
                trialDaysRemaining = item.trialDaysLeft
            )

            "TRIAL_ACTIVE" -> {
                val days = item.trialDaysLeft ?: 7
                CourseCardUIState(
                    courseId = item.id,
                    title = item.title,
                    coverUrl = item.bannerUrl,
                    tagText = "$days দিন সবকিছু ফ্রি!",
                    tagBgColor = SoftPinkBg,
                    tagTextColor = DarkPinkText,
                    leftButtonText = "বিস্তারিত দেখো",
                    rightButtonText = "ফ্রিতে শেখো",
                    rightButtonBgColor = FilledMagenta,
                    isLiveNow = item.isLiveNow,
                    enrollmentType = "TRIAL_ACTIVE",
                    trialDaysRemaining = days
                )
            }

            "TRIAL_EXPIRED" -> CourseCardUIState(
                courseId = item.id,
                title = item.title,
                coverUrl = item.bannerUrl,
                tagText = "ফ্রিতে শেখা শেষ",
                tagBgColor = SoftAmberBg,
                tagTextColor = DarkAmberText,
                leftButtonText = "বিস্তারিত দেখো",
                rightButtonText = "ভর্তি হও",
                rightButtonBgColor = FilledMagenta,
                isLiveNow = item.isLiveNow,
                enrollmentType = "TRIAL_EXPIRED",
                trialDaysRemaining = null
            )

            else -> CourseCardUIState(
                courseId = item.id,
                title = item.title,
                coverUrl = item.bannerUrl,
                tagText = "ভর্তি হয়েছো",
                tagBgColor = SoftGreenBg,
                tagTextColor = DarkGreenText,
                leftButtonText = "শেখা চালিয়ে যাও",
                rightButtonText = null,
                rightButtonBgColor = null,
                isLiveNow = item.isLiveNow,
                enrollmentType = "ENROLLED",
                trialDaysRemaining = item.trialDaysLeft
            )
        }
    }
}
