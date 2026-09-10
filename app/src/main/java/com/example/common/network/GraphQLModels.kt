package com.example.common.network

/**
 * Common GraphQL Request wrapper for api.shikho.com/graphql
 */
data class GraphQLRequest(
    val operationName: String,
    val query: String,
    val variables: Map<String, Any?>
)

/**
 * Domain Models for GraphQL Course Hierarchy
 */
data class GqlChapterItem(
    val chapterId: String,
    val chapterName: String,
    val chapterNo: String = "",
    val status: String = "Finished", // e.g. "Running", "Finished", "Upcoming"
    val classCounter: Int = 0,
    val examCounter: Int = 0,
    val progressPercentage: Double = 0.0
) {
    val isRunning: Boolean get() = status.equals("Running", ignoreCase = true) || status == "চলমান"
    val statusBadgeBn: String
        get() = when {
            isRunning -> "চলমান"
            status.equals("Finished", ignoreCase = true) -> "পড়ানো শেষ"
            status.equals("Upcoming", ignoreCase = true) -> "আসন্ন"
            else -> status
        }
}

data class GqlTopic(
    val id: String,
    val name: String
)

data class GqlLiveClass(
    val id: String,
    val chapterName: String? = null,
    val recordingUrl: String? = null,
    val isOngoing: Boolean = false,
    val type: String? = null,
    val topics: List<GqlTopic> = emptyList()
)

data class GqlQuiz(
    val id: String,
    val title: String
)

data class GqlAnimatedVideo(
    val id: String,
    val topicName: String
)

enum class LessonContentType {
    LIVE_CLASS,
    RECORDED_CLASS,
    EXAM_LIVE,
    EXAM_QUIZ,
    ANIMATED_VIDEO,
    HOMEWORK,
    UNKNOWN
}

data class GqlLessonItem(
    val id: String,
    val title: String,
    val contentType: String, // e.g. "LiveClass", "LiveExam", "HomeWork"
    val userActivityState: String = "Upcoming", // "Missed", "Completed", "Upcoming"
    val startTime: String? = null,
    val endTime: String? = null,
    val hwType: String? = null,
    val liveClass: GqlLiveClass? = null,
    val hwQuiz: GqlQuiz? = null,
    val hwAnimatedVideo: GqlAnimatedVideo? = null
) {
    val parsedContentType: LessonContentType
        get() = when {
            contentType.equals("LiveClass", ignoreCase = true) -> {
                if (liveClass?.isOngoing == true) LessonContentType.LIVE_CLASS
                else LessonContentType.RECORDED_CLASS
            }
            contentType.equals("LiveExam", ignoreCase = true) -> LessonContentType.EXAM_LIVE
            hwQuiz != null || contentType.contains("Quiz", ignoreCase = true) -> LessonContentType.EXAM_QUIZ
            hwAnimatedVideo != null || contentType.contains("Animated", ignoreCase = true) -> LessonContentType.ANIMATED_VIDEO
            else -> LessonContentType.LIVE_CLASS
        }

    val isExam: Boolean
        get() = parsedContentType == LessonContentType.EXAM_LIVE || parsedContentType == LessonContentType.EXAM_QUIZ

    val activityStateBadgeBn: String
        get() = when (userActivityState.lowercase()) {
            "completed" -> "সম্পন্ন"
            "missed" -> "মিসড"
            "upcoming" -> "আসন্ন"
            "ongoing" -> "চলমান"
            else -> userActivityState
        }
}

data class GqlResourceAttachment(
    val id: String,
    val title: String,
    val description: String? = null,
    val url: String // AWS S3 pre-signed direct URL
) {
    val isPdf: Boolean
        get() = url.contains(".pdf", ignoreCase = true) || title.contains("PDF", ignoreCase = true) || title.contains("বই", ignoreCase = true) || title.contains("শিট", ignoreCase = true)
}

/**
 * Domain Models for Syllabus Switch Flow & Program Phases
 */
data class GqlAcademicProgram(
    val id: String,
    val title: String,
    val className: String,
    val group: String,
    val examBatch: String,
    val isActive: Boolean = true,
    val expiryDate: String = "০১ সেপ্টেম্বর, ২০২৬",
    val isOnInstallment: Boolean = true
)

data class GqlProgramPhase(
    val id: String,
    val academicProgramId: String,
    val title: String,
    val status: String,
    val isCurrent: Boolean = false,
    val courseProgressPercentage: Double = 0.0,
    val syllabusAttachmentUrl: String? = null
)

/**
 * Filter variables for GetAcademicProgram GraphQL query
 */
data class AcademicProgramFilterParams(
    val batchId: String = "HSC 2027",
    val className: String = "C11",
    val group: String = "Humanities",
    val vendor: String = "BD"
)

/**
 * Domain model representing a course/program item from GetAcademicProgram response
 */
data class AcademicProgramItem(
    val id: String,
    val title: String,
    val type: String = "Paid", // "FullApTrial", "Paid", "Free"
    val trialEndDate: String? = null, // e.g. "2025-12-11" or "2026-12-31"
    val hasEnrolment: Boolean = false,
    val isFree: Boolean = false,
    val isActive: Boolean = true,
    val expiryDate: String? = null, // e.g. "2029-04-30"
    val phasePricing: Int = 0,
    val trialEnabled: Boolean = false,
    val trialDuration: Int = 3,
    val badge: String = "",
    val programTag: String = "ACADEMIC PROGRAM",
    val classTag: String = "HSC 27 • Humanities",
    val bannerUrl: String? = null,
    val colorPrimaryHex: Long = 0xFF0B1440,
    val colorSecondaryHex: Long = 0xFF1E3A8A,
    val features: List<String> = emptyList()
) {
    /**
     * UI Status Badge text based on 6-step Trial State Machine:
     * 1. Active Trial: "৩ দিন ফ্রি ট্রায়াল সক্রিয়"
     * 2. Expired Trial: "ফ্রিতে শেখা শেষ"
     * 3. Trial Eligible: "৩ দিন সবকিছু ফ্রি!"
     * 4. Enrolled Paid: "ভর্তি হয়েছো"
     * 5. 100% Free: "সম্পূর্ণ ফ্রি!"
     * 6. Regular Paid: "৳X"
     */
    val tagText: String
        get() = when {
            // সক্রিয় ট্রায়াল
            type.equals("FullApTrial", ignoreCase = true) && hasEnrolment && isActive -> "৩ দিন ফ্রি ট্রায়াল সক্রিয়"
            // মেয়াদোত্তীর্ণ ট্রায়াল
            type.equals("FullApTrial", ignoreCase = true) && !hasEnrolment -> "ফ্রিতে শেখা শেষ"
            // পেইড এনরোলমেন্ট
            (type.equals("Paid", ignoreCase = true) || hasEnrolment) && isActive && !isFree -> "ভর্তি হয়েছো"
            // সম্পূর্ণ ফ্রি
            isFree -> "সম্পূর্ণ ফ্রি!"
            // ট্রায়াল এলিজিবল কিন্তু এখনো নেওয়া হয়নি
            trialEnabled -> "৩ দিন সবকিছু ফ্রি!"
            phasePricing > 0 -> "৳%,d".format(phasePricing)
            else -> badge.ifEmpty { "প্রোগ্রাম" }
        }

    val buttonText: String
        get() = when {
            // সক্রিয় ট্রায়াল
            type.equals("FullApTrial", ignoreCase = true) && hasEnrolment && isActive -> "শেখা চালিয়ে যাও"
            // মেয়াদোত্তীর্ণ ট্রায়াল
            type.equals("FullApTrial", ignoreCase = true) && !hasEnrolment -> "বিস্তারিত দেখো"
            // পেইড এনরোলমেন্ট
            (type.equals("Paid", ignoreCase = true) || hasEnrolment) && isActive -> "শেখা চালিয়ে যাও"
            // সম্পূর্ণ ফ্রি
            isFree -> "সম্পূর্ণ ফ্রি'তে শুরু করো"
            // ট্রায়াল এলিজিবল
            trialEnabled -> "৩ দিন ফ্রিতে শেখো"
            else -> "বিস্তারিত দেখো"
        }

    val isTrialExpired: Boolean
        get() = type.equals("FullApTrial", ignoreCase = true) && !hasEnrolment

    val isTrialActive: Boolean
        get() = type.equals("FullApTrial", ignoreCase = true) && hasEnrolment && isActive

    val isEnrolled: Boolean
        get() = (type.equals("Paid", ignoreCase = true) || hasEnrolment) && isActive
}

/**
 * Data container representing the 3 UI sections derived from GetAcademicProgram response:
 * 1. আমার কোর্স (My Courses) -> enrolled_programs
 * 2. ফ্রি কোর্স (Free Courses) -> other_programs where is_free == true
 * 3. সকল কোর্স (All Courses) -> other_programs where !is_free and id not in blacklisted_programs
 */
data class AcademicProgramsCatalog(
    val enrolledPrograms: List<AcademicProgramItem> = emptyList(),
    val freePrograms: List<AcademicProgramItem> = emptyList(),
    val allCoursesPrograms: List<AcademicProgramItem> = emptyList(),
    val blacklistedPrograms: List<String> = emptyList()
)

/**
 * Domain Models for REST Address API & Profile Updation
 */
data class AddressDivision(
    val id: String,
    val name: String,
    val nameBn: String = name
)

data class AddressDistrict(
    val id: String,
    val divisionId: String,
    val name: String,
    val nameBn: String = name
)

data class SchoolItem(
    val id: String,
    val name: String,
    val districtId: String? = null
)

data class ProfileUpdatePayload(
    val dob: String? = null,
    val gender: String? = null,
    val shift: String? = null,
    val sscBoardName: String? = null,
    val hscBoardName: String? = null,
    val boardRollNumber: String? = null,
    val hscBoardRollNumber: String? = null,
    val boardRegNumber: String? = null,
    val otherTutoringSource: List<String> = emptyList(),
    val guardianName: String? = null,
    val guardianMobile: String? = null,
    val schoolId: String? = null
)

/**
 * Domain Models for Enrollment & Payment Plans
 */
data class PricePlan(
    val title: String,
    val amount: Double,
    val serial: Int = 1
)

data class EnrollPaymentPlanItem(
    val id: String,
    val isActive: Boolean = true,
    val serial: Int = 1,
    val paymentStatus: String = "Paid", // "Paid", "Pending", "Due"
    val paymentDate: String? = null,
    val dueDate: String? = null,
    val pricePlan: PricePlan? = null
) {
    val isPaid: Boolean
        get() = paymentStatus.equals("Paid", ignoreCase = true) || paymentStatus == "পরিশোধিত"

    val titleBn: String
        get() = pricePlan?.title ?: when (serial) {
            1 -> "১ম কিস্তি"
            2 -> "২য় কিস্তি"
            3 -> "৩য় কিস্তি"
            4 -> "৪র্থ কিস্তি"
            else -> "$serial-তম কিস্তি"
        }

    val amountFormattedBn: String
        get() {
            val amt = pricePlan?.amount?.toInt() ?: 0
            return "৳%,d".format(amt)
        }
}

/**
 * Sealed UI State wrappers for clean StateFlow presentation
 */
sealed interface CourseContentUiState<out T> {
    data object Loading : CourseContentUiState<Nothing>
    data class Success<T>(val data: T) : CourseContentUiState<T>
    data class Error(val message: String, val cachedData: Any? = null) : CourseContentUiState<Nothing>
}

data class BatchOption(
    val year: Int,
    val label: String
)

data class ShikhoClassItem(
    val serial: Int,
    val code: String,
    val nameEn: String,
    val nameBn: String,
    val isGroupRequired: Boolean = false,
    val hasBatchSelection: Boolean = true,
    val parentName: String? = null,
    val parentNameBn: String? = null,
    val isActive: Boolean = true
)

