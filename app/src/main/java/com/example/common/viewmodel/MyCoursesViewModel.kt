package com.example.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.data.local.UserAcademicProfileEntity
import com.example.common.model.CourseCardStateTransformer
import com.example.common.model.CourseCardUIState
import com.example.common.model.Resource
import com.example.common.network.UserSessionManager
import com.example.common.repository.CourseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State for MyCourses Screen
 */
sealed interface MyCoursesUiState {
    data object Loading : MyCoursesUiState
    data class Success(
        val classId: String,
        val classTitleBn: String,
        val batchYear: String?,
        val groupCode: String?,
        val groupTitleBn: String?,
        val courses: List<CourseCardUIState>
    ) : MyCoursesUiState
    data class Empty(
        val classTitleBn: String,
        val message: String = "আপনার নির্বাচিত সিলেবাসের কোনো কোর্স বর্তমানে পাওয়া যায়নি"
    ) : MyCoursesUiState
    data class Error(
        val message: String
    ) : MyCoursesUiState
}

/**
 * Reactive ViewModel for "আমার কোর্স" (My Courses) Module.
 * Strict Zero Mock / Demo Data Policy:
 * 1. Binds dynamically to the active syllabus profile.
 * 2. Starts strictly with Loading state.
 * 3. Fetches 100% real data from repository; renders Empty State if list is empty.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyCoursesViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    // Trigger for manual force refresh
    private val _refreshTrigger = MutableStateFlow(0L)

    private fun getFallbackProfile(): UserAcademicProfileEntity {
        val session = UserSessionManager.currentUserProfile
        return UserAcademicProfileEntity(
            userId = "usr_101",
            classId = "C11",
            classTitleBn = session.studentClass.ifBlank { "একাদশ-দ্বাদশ শ্রেণি" },
            batchYear = session.examBatch.ifBlank { "2027" },
            groupCode = if (session.group.contains("বিজ্ঞান")) "SCI" else if (session.group.contains("ব্যবসায়")) "BS" else "HUM",
            groupTitleBn = session.group.ifBlank { "মানবিক" },
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Primary reactive pipeline:
     * Listens to activeSyllabusFlow, triggers network calls via CourseRepository,
     * transforms real data to CourseCardUIState, or emits Empty state if no courses exist.
     */
    val uiState: StateFlow<MyCoursesUiState> = combine(
        repository.activeSyllabusFlow.map { profile -> profile ?: getFallbackProfile() },
        _refreshTrigger
    ) { profile, _ -> profile }
        .flatMapLatest { profile ->
            val classId = profile.classId
            val batchYear = profile.batchYear
            val groupCode = profile.groupCode

            repository.getMyCourses(
                classId = classId,
                batch = batchYear,
                group = groupCode
            ).map { resource ->
                when (resource) {
                    is Resource.Loading -> MyCoursesUiState.Loading

                    is Resource.Success -> {
                        val courses = resource.data
                        if (courses.isEmpty()) {
                            MyCoursesUiState.Empty(
                                classTitleBn = profile.classTitleBn,
                                message = "আপনার নির্বাচিত সিলেবাসের কোনো কোর্স বর্তমানে পাওয়া যায়নি"
                            )
                        } else {
                            val cardStates = courses.map { CourseCardStateTransformer.transform(it) }
                            MyCoursesUiState.Success(
                                classId = classId,
                                classTitleBn = profile.classTitleBn,
                                batchYear = batchYear,
                                groupCode = groupCode,
                                groupTitleBn = profile.groupTitleBn,
                                courses = cardStates
                            )
                        }
                    }

                    is Resource.Error -> {
                        MyCoursesUiState.Error(
                            message = resource.message
                        )
                    }
                }
            }
        }
        .catch { e ->
            emit(MyCoursesUiState.Error(e.localizedMessage ?: "অপ্রত্যাশিত ত্রুটি ঘটেছে।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MyCoursesUiState.Loading
        )

    fun refresh() {
        _refreshTrigger.value = System.currentTimeMillis()
    }
}
