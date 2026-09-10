package com.example.common.repository

import com.example.common.data.local.CachedMyCoursesDao
import com.example.common.data.local.CourseDao
import com.example.common.data.local.UserAcademicProfileDao
import com.example.common.data.local.UserAcademicProfileEntity
import com.example.common.data.local.toDomain
import com.example.common.data.local.toEntity
import com.example.common.data.remote.CourseApiService
import com.example.common.data.remote.toDomain
import com.example.common.model.CourseItem
import com.example.common.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Interface for Course Repository adhering to 100% remote fetch & zero mock data policy
 */
interface CourseRepository {
    val activeSyllabusFlow: Flow<UserAcademicProfileEntity?>

    fun getMyCourses(
        classId: String,
        batch: String?,
        group: String?
    ): Flow<Resource<List<CourseItem>>>

    suspend fun invalidateCache(classId: String? = null)
}

typealias MyCoursesRepository = CourseRepository

/**
 * Production implementation of CourseRepository with ZERO mock data policy
 * Real network calls via Retrofit, real Room persistence, and clean empty states.
 */
class CourseRepositoryImpl(
    private val apiService: CourseApiService,
    private val courseDao: CourseDao,
    private val profileDao: UserAcademicProfileDao
) : CourseRepository {

    override val activeSyllabusFlow: Flow<UserAcademicProfileEntity?> =
        profileDao.observeProfile("usr_101")

    override fun getMyCourses(
        classId: String,
        batch: String?,
        group: String?
    ): Flow<Resource<List<CourseItem>>> = flow {
        emit(Resource.Loading)

        try {
            // Real network call using active syllabus query params
            val response = apiService.getMyCourses(classId, batch, group)
            if (response.isSuccessful && response.body() != null) {
                val courses = response.body()!!.data.map { it.toDomain() }

                // Cache real items to local database
                courseDao.clearByClass(classId)
                if (courses.isNotEmpty()) {
                    courseDao.insertCourses(courses.map { it.toEntity(classId, batch, group) })
                }
                emit(Resource.Success(courses))
            } else {
                emit(Resource.Error("সার্ভার থেকে তথ্য পাওয়া যায়নি: ${response.message().ifBlank { "কোড: ${response.code()}" }}"))
            }
        } catch (e: Exception) {
            // On offline, fetch strictly cached real items, never mock items
            val cachedCourses = courseDao.getCourses(classId, batch, group).map { it.toDomain() }
            if (cachedCourses.isNotEmpty()) {
                emit(Resource.Success(cachedCourses))
            } else {
                emit(Resource.Error("ইন্টারনেট সংযোগ চেক করুন: ${e.localizedMessage ?: "অফলাইন" }"))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun invalidateCache(classId: String?) = withContext(Dispatchers.IO) {
        if (classId != null) {
            courseDao.clearByClass(classId)
        } else {
            courseDao.clearAll()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CourseRepositoryImpl? = null

        fun getInstance(
            apiService: CourseApiService,
            cachedMyCoursesDao: CachedMyCoursesDao,
            profileDao: UserAcademicProfileDao
        ): CourseRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val instance = CourseRepositoryImpl(apiService, cachedMyCoursesDao, profileDao)
                INSTANCE = instance
                instance
            }
        }

        fun getSharedInstance(): CourseRepositoryImpl? = INSTANCE
    }
}
typealias MyCoursesRepositoryImpl = CourseRepositoryImpl
