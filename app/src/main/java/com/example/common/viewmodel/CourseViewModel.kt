package com.example.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.network.AcademicProgramFilterParams
import com.example.common.network.AcademicProgramItem
import com.example.common.network.AcademicProgramsCatalog
import com.example.common.network.AnalyticsTracker
import com.example.common.network.CourseContentUiState
import com.example.common.network.GqlChapterItem
import com.example.common.network.GqlLessonItem
import com.example.common.network.GqlResourceAttachment
import com.example.common.network.GraphQLCourseService
import com.example.common.network.UserSessionManager
import com.example.common.repository.CourseRepository
import com.example.common.repository.CourseRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseViewModel(
    private val repository: CourseRepository = CourseRepositoryImpl.shared
) : ViewModel() {

    private val _chaptersState = MutableStateFlow<CourseContentUiState<List<GqlChapterItem>>>(CourseContentUiState.Loading)
    val chaptersState: StateFlow<CourseContentUiState<List<GqlChapterItem>>> = _chaptersState.asStateFlow()

    private val _lessonsState = MutableStateFlow<CourseContentUiState<List<GqlLessonItem>>>(CourseContentUiState.Loading)
    val lessonsState: StateFlow<CourseContentUiState<List<GqlLessonItem>>> = _lessonsState.asStateFlow()

    private val _attachmentsState = MutableStateFlow<CourseContentUiState<List<GqlResourceAttachment>>>(CourseContentUiState.Loading)
    val attachmentsState: StateFlow<CourseContentUiState<List<GqlResourceAttachment>>> = _attachmentsState.asStateFlow()

    private val _academicProgramsState = MutableStateFlow<CourseContentUiState<AcademicProgramsCatalog>>(CourseContentUiState.Loading)
    val academicProgramsState: StateFlow<CourseContentUiState<AcademicProgramsCatalog>> = _academicProgramsState.asStateFlow()

    companion object {
        val shared: CourseViewModel by lazy { CourseViewModel() }

        fun currentFilterParamsFromProfile(): AcademicProgramFilterParams {
            val profile = UserSessionManager.currentUserProfile
            val classCode = when {
                profile.studentClass.contains("এইচএসসি") || profile.studentClass.contains("HSC") -> "C11"
                profile.studentClass.contains("১০") || profile.studentClass.contains("10") -> "C10"
                profile.studentClass.contains("৯") || profile.studentClass.contains("9") -> "C09"
                profile.studentClass.contains("৮") || profile.studentClass.contains("8") -> "C08"
                profile.studentClass.contains("৭") || profile.studentClass.contains("7") -> "C07"
                profile.studentClass.contains("৬") || profile.studentClass.contains("6") -> "C06"
                profile.studentClass.contains("৫") || profile.studentClass.contains("5") -> "C05"
                profile.studentClass.contains("এডমিশন") -> "CAD"
                else -> "C11"
            }
            val groupName = when {
                profile.group.contains("মানবিক") -> "Humanities"
                profile.group.contains("বিজ্ঞান") -> "Science"
                profile.group.contains("ব্যবসায়") -> "BusinessStudies"
                else -> "General"
            }
            val rawDigits = profile.examBatch.filter { it.isDigit() }
            val batchYear = if (rawDigits.isNotEmpty()) rawDigits else "2027"
            val batchId = if (classCode == "C11") "HSC $batchYear" else "${profile.studentClass} $batchYear"
            return AcademicProgramFilterParams(
                batchId = batchId,
                className = classCode,
                group = groupName,
                vendor = "BD"
            )
        }
    }

    init {
        loadChapters()
        loadAcademicPrograms(currentFilterParamsFromProfile())
    }

    /**
     * GetAcademicProgram কুয়েরি দিয়ে নির্ধারিত সিলেবাস ও কোর্স লোড
     */
    fun loadAcademicPrograms(
        params: AcademicProgramFilterParams = currentFilterParamsFromProfile()
    ) {
        viewModelScope.launch {
            repository.getAcademicPrograms(params).collect {
                _academicProgramsState.value = it
            }
        }
    }

    /**
     * সম্পূর্ণ ফ্রি'তে শুরু করো (The Next Champ - HSC '27)
     */
    fun enrollInFreeCourse(course: AcademicProgramItem) {
        AnalyticsTracker.trackFreeEnrollment(course.id, course.title)
        val currentState = _academicProgramsState.value
        if (currentState is CourseContentUiState.Success) {
            val catalog = currentState.data
            val updatedFreeList = catalog.freePrograms.filter { it.id != course.id }
            val newlyEnrolledCourse = course.copy(
                hasEnrolment = true,
                isActive = true,
                type = "Paid",
                badge = "ভর্তি সম্পন্ন"
            )
            val updatedEnrolledList = catalog.enrolledPrograms + newlyEnrolledCourse
            _academicProgramsState.value = CourseContentUiState.Success(
                catalog.copy(
                    enrolledPrograms = updatedEnrolledList,
                    freePrograms = updatedFreeList
                )
            )
        }
    }

    /**
     * ৩ দিন সবকিছু ফ্রি! ট্রায়াল অ্যাক্টিভেশন
     */
    fun activateTrialForCourse(
        course: AcademicProgramItem,
        onActivated: (com.example.common.network.TrialEnrollmentRecord) -> Unit
    ) {
        com.example.common.network.TrialManager.activateTrial(course) { record ->
            val currentState = _academicProgramsState.value
            if (currentState is CourseContentUiState.Success) {
                val catalog = currentState.data
                val updatedAllList = catalog.allCoursesPrograms.filter { it.id != course.id }
                val trialEnrolledCourse = course.copy(
                    type = "FullApTrial",
                    hasEnrolment = true,
                    isActive = true,
                    trialEndDate = record.trialEndDate,
                    badge = "৩ দিন ফ্রি ট্রায়াল"
                )
                val updatedEnrolledList = listOf(trialEnrolledCourse) + catalog.enrolledPrograms.filter { it.id != course.id }
                _academicProgramsState.value = CourseContentUiState.Success(
                    catalog.copy(
                        enrolledPrograms = updatedEnrolledList,
                        allCoursesPrograms = updatedAllList
                    )
                )
            }
            onActivated(record)
        }
    }

    /**
     * ট্রায়াল এক্সপায়ার সিমুলেশন (ReadOnly মোড ও পেওয়াল সক্রিয়করণ)
     */
    fun expireTrialForCourse(programId: String) {
        com.example.common.network.TrialManager.expireTrial(programId)
        val currentState = _academicProgramsState.value
        if (currentState is CourseContentUiState.Success) {
            val catalog = currentState.data
            val updatedEnrolled = catalog.enrolledPrograms.map { prog ->
                if (prog.id == programId) {
                    prog.copy(
                        type = "FullApTrial",
                        hasEnrolment = false,
                        isActive = false
                    )
                } else prog
            }
            _academicProgramsState.value = CourseContentUiState.Success(
                catalog.copy(enrolledPrograms = updatedEnrolled)
            )
        }
    }

    /**
     * পেওয়াল থেকে সম্পূর্ণ কোর্স কেনা (ভর্তি নিশ্চিতকরণ)
     */
    fun purchaseCourse(course: AcademicProgramItem) {
        val currentState = _academicProgramsState.value
        if (currentState is CourseContentUiState.Success) {
            val catalog = currentState.data
            val updatedEnrolled = catalog.enrolledPrograms.map { prog ->
                if (prog.id == course.id) {
                    prog.copy(
                        type = "Paid",
                        hasEnrolment = true,
                        isActive = true,
                        badge = "ভর্তি সম্পন্ন"
                    )
                } else prog
            }
            _academicProgramsState.value = CourseContentUiState.Success(
                catalog.copy(enrolledPrograms = updatedEnrolled)
            )
        }
    }

    fun loadChapters(
        programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
        phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID,
        subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID
    ) {
        viewModelScope.launch {
            repository.getChapters(programId, phaseId, subjectId).collect {
                _chaptersState.value = it
            }
        }
    }

    fun loadChapterDetails(
        chapterId: String,
        programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
        phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID,
        subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID
    ) {
        viewModelScope.launch {
            repository.getChapterLessons(chapterId, programId, phaseId).collect {
                _lessonsState.value = it
            }
        }
        viewModelScope.launch {
            repository.getChapterAttachments(chapterId, subjectId, programId, phaseId).collect {
                _attachmentsState.value = it
            }
        }
    }
}
