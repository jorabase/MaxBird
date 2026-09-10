package com.example.common.repository

import com.example.common.network.AcademicProgramFilterParams
import com.example.common.network.AcademicProgramsCatalog
import com.example.common.network.CourseContentUiState
import com.example.common.network.GqlChapterItem
import com.example.common.network.GqlLessonItem
import com.example.common.network.GqlResourceAttachment
import com.example.common.network.GraphQLCourseService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface CourseRepository {
    fun getChapters(
        programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
        phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID,
        subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID
    ): Flow<CourseContentUiState<List<GqlChapterItem>>>

    fun getChapterLessons(
        chapterId: String,
        programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
        phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID
    ): Flow<CourseContentUiState<List<GqlLessonItem>>>

    fun getChapterAttachments(
        chapterId: String,
        subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID,
        moduleId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
        phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID
    ): Flow<CourseContentUiState<List<GqlResourceAttachment>>>

    fun getAcademicPrograms(
        params: AcademicProgramFilterParams = AcademicProgramFilterParams()
    ): Flow<CourseContentUiState<AcademicProgramsCatalog>>
}

class CourseRepositoryImpl(
    private val service: GraphQLCourseService = GraphQLCourseService()
) : CourseRepository {

    override fun getChapters(
        programId: String,
        phaseId: String,
        subjectId: String
    ): Flow<CourseContentUiState<List<GqlChapterItem>>> = flow {
        emit(CourseContentUiState.Loading)
        val result = service.fetchPhaseWiseChapters(programId, phaseId, subjectId)
        result.onSuccess { list ->
            emit(CourseContentUiState.Success(list))
        }.onFailure { error ->
            emit(CourseContentUiState.Error(error.message ?: "অধ্যায় লোড করতে সমস্যা হয়েছে"))
        }
    }

    override fun getChapterLessons(
        chapterId: String,
        programId: String,
        phaseId: String
    ): Flow<CourseContentUiState<List<GqlLessonItem>>> = flow {
        emit(CourseContentUiState.Loading)
        val result = service.fetchUpcomingLessons(chapterId, programId, phaseId)
        result.onSuccess { list ->
            emit(CourseContentUiState.Success(list))
        }.onFailure { error ->
            emit(CourseContentUiState.Error(error.message ?: "ক্লাস ও পরীক্ষার তথ্য লোড করতে সমস্যা হয়েছে"))
        }
    }

    override fun getChapterAttachments(
        chapterId: String,
        subjectId: String,
        moduleId: String,
        phaseId: String
    ): Flow<CourseContentUiState<List<GqlResourceAttachment>>> = flow {
        emit(CourseContentUiState.Loading)
        val result = service.fetchResourceAttachments(chapterId, subjectId, moduleId, phaseId)
        result.onSuccess { list ->
            emit(CourseContentUiState.Success(list))
        }.onFailure { error ->
            emit(CourseContentUiState.Error(error.message ?: "রিসোর্স লোড করতে সমস্যা হয়েছে"))
        }
    }

    override fun getAcademicPrograms(
        params: AcademicProgramFilterParams
    ): Flow<CourseContentUiState<AcademicProgramsCatalog>> = flow {
        emit(CourseContentUiState.Loading)
        val result = service.fetchAcademicPrograms(params)
        result.onSuccess { catalog ->
            emit(CourseContentUiState.Success(catalog))
        }.onFailure { error ->
            emit(CourseContentUiState.Error(error.message ?: "কোর্স ক্যাটালগ লোড করতে সমস্যা হয়েছে"))
        }
    }

    companion object {
        val shared: CourseRepository by lazy { CourseRepositoryImpl() }
    }
}
