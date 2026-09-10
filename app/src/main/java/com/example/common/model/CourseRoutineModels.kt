package com.example.common.model

data class EnrolledCourse(
    val id: String,
    val title: String,
    val badge: String = "",
    val instructor: String = "",
    val totalClasses: Int = 0,
    val completedClasses: Int = 0,
    val colorPrimaryHex: Long = 0xFF4338CA,
    val colorSecondaryHex: Long = 0xFF6366F1
)

enum class RoutineItemType {
    LIVE_CLASS,
    RECORDED,
    LAB_PRACTICE,
    EXAM_MCQ,
    EXAM_CQ
}

data class RoutineClassItem(
    val id: String,
    val time: String,
    val durationText: String = "",
    val subject: String,
    val chapterOrTopic: String,
    val instructor: String,
    val typeLabel: String = "লেকচার ক্লাস",
    val type: RoutineItemType = RoutineItemType.LIVE_CLASS,
    val subjectColorHex: Long = 0xFF0284C7,
    val isLiveNow: Boolean = false,
    val isCompleted: Boolean = false
)

data class RoutineExamItem(
    val id: String,
    val time: String,
    val title: String,
    val syllabus: String,
    val marks: Int,
    val durationText: String,
    val type: RoutineItemType = RoutineItemType.EXAM_MCQ
)

data class DaySchedule(
    val dayIndex: Int, // 0 = Sat, 1 = Sun, 2 = Mon, 3 = Tue, 4 = Wed, 5 = Thu, 6 = Fri
    val dayNameBn: String,
    val dayShortBn: String,
    val dateText: String,
    val fullDateBn: String = "",
    val classes: List<RoutineClassItem> = emptyList(),
    val exams: List<RoutineExamItem> = emptyList()
) {
    val totalCount: Int get() = classes.size + exams.size
}

data class MonthClassOverview(
    val dateString: String,
    val dayNameBn: String,
    val subject: String,
    val topic: String,
    val time: String,
    val typeName: String,
    val isExam: Boolean = false
)

data class SubjectItem(
    val id: String,
    val titleBn: String,
    val iconSymbol: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val progressPercent: Int = 0
)

data class ChapterItem(
    val id: String,
    val titleBn: String,
    val statusBadge: String = "", // e.g. "পড়ানো শেষ", "পড়ানো হচ্ছে"
    val isCompleted: Boolean = false,
    val classesCount: Int = 0,
    val examsCount: Int = 0
)

data class LectureItem(
    val id: String,
    val titleBn: String,
    val typeLabel: String = "লেকচার ক্লাস",
    val dateText: String = "",
    val statusBadge: String = "",
    val noticeText: String = "",
    val isExam: Boolean = false,
    val instructorName: String = "",
    val instructorBio: String = "",
    val instructorExp: String = "",
    val instructorStudents: String = "",
    val recordingUrl: String = "",
    val videoStreamUrl: String = "",
    val topics: List<String> = emptyList(),
    val pdfUrl: String = ""
)

data class QuarterProgressItem(
    val id: String,
    val nameBn: String,
    val durationBn: String,
    val progressPercent: Double = 0.0,
    val isCurrent: Boolean = false,
    val statusBadge: String = "আসন্ন"
)

data class UserProfile(
    val id: String = "",
    val name: String = "Student",
    val phone: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val avatarUrl: String = "",
    val studentClass: String = "এইচএসসি",
    val group: String = "",
    val examBatch: String = "",
    val classShift: String = "",
    val sscBoard: String = "",
    val sscRoll: String = "",
    val hscBoard: String = "",
    val hscRoll: String = "",
    val boardRegNumber: String = "",
    val institutionDivision: String = "",
    val institutionDistrict: String = "",
    val institutionName: String = "",
    val schoolId: String = "",
    val educationMedium: String = "বাংলা মাধ্যম",
    val guardianName: String = "",
    val guardianPhone: String = "",
    val otherTutoringSources: List<String> = emptyList(),
    val futurePlan: List<String> = emptyList(),
    val isLoggedIn: Boolean = false
)
