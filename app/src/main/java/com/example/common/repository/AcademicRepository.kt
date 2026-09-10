package com.example.common.repository

import com.example.common.data.local.CachedAcademicConfigDao
import com.example.common.data.local.CachedAcademicConfigEntity
import com.example.common.data.local.UserAcademicProfileDao
import com.example.common.data.local.UserAcademicProfileEntity
import com.example.common.data.remote.AcademicNetworkDataSource
import com.example.common.model.ApiResponse
import com.example.common.model.ClassItem
import com.example.common.model.AppGlobalEvent
import com.example.common.model.UpdateSyllabusRequest
import com.example.common.network.UserSessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository orchestrating Dynamic Remote API calls (GET /api/v2/academic/classes-config),
 * Room Database Local Persistence (Offline-First), and the Cache Invalidation Pipeline
 * with AppGlobalEvent broadcasting.
 */
class AcademicRepository(
    private val networkDataSource: AcademicNetworkDataSource,
    private val profileDao: UserAcademicProfileDao,
    private val configDao: CachedAcademicConfigDao,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    // Shared Event Bus across the entire application
    private val _appEvents = MutableSharedFlow<AppGlobalEvent>(replay = 0, extraBufferCapacity = 64)
    val appEvents: SharedFlow<AppGlobalEvent> = _appEvents.asSharedFlow()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    
    private val configListType = Types.newParameterizedType(List::class.java, ClassItem::class.java)
    private val configAdapter = moshi.adapter<List<ClassItem>>(configListType)

    /**
     * Fetches academic config dynamically from GET /api/v2/academic/classes-config,
     * caches it in Room for offline access, and serves cached config if offline.
     */
    suspend fun getAcademicConfig(): Result<List<ClassItem>> = withContext(Dispatchers.IO) {
        try {
            // Check local Room cache first
            val cachedEntity = configDao.getConfig()
            var cachedConfig: List<ClassItem>? = null
            if (cachedEntity != null && cachedEntity.configJson.isNotBlank()) {
                try {
                    cachedConfig = configAdapter.fromJson(cachedEntity.configJson)
                } catch (_: Exception) { }
            }

            // Attempt dynamic remote network fetch
            try {
                val remoteResponse = networkDataSource.fetchAcademicConfig()
                if (remoteResponse.isSuccessful) {
                    val apiData = remoteResponse.body()?.data ?: emptyList()
                    // Update Room Cache for offline persistence
                    val json = configAdapter.toJson(apiData)
                    configDao.insertOrUpdateConfig(
                        CachedAcademicConfigEntity(
                            id = "global_academic_config",
                            configJson = json,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                    Result.success(apiData)
                } else {
                    if (cachedConfig != null) {
                        Result.success(cachedConfig)
                    } else {
                        Result.failure(Exception("Network error: ${remoteResponse.code()}"))
                    }
                }
            } catch (networkError: Exception) {
                if (cachedConfig != null) {
                    Result.success(cachedConfig)
                } else {
                    Result.failure(networkError)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observes cached academic configuration from Room DB as a reactive Flow
     */
    fun observeAcademicConfig(): Flow<List<ClassItem>> {
        return configDao.observeConfig().map { entity ->
            entity?.configJson?.let { json ->
                try {
                    configAdapter.fromJson(json) ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }
    }

    /**
     * Updates user's syllabus choice via PATCH /api/v2/user/academic-profile,
     * writes to Room DB, updates global session, and broadcasts AppGlobalEvent.SyllabusChanged.
     */
    suspend fun updateSyllabus(
        userId: String = "usr_101",
        classId: String,
        classTitle: String,
        batchYear: String?,
        groupCode: String?,
        groupTitle: String?
    ): Result<UserAcademicProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateSyllabusRequest(
                classId = classId,
                batch = batchYear,
                groupCode = groupCode
            )
            val response = networkDataSource.updateAcademicProfile(request)
            
            if (response.isSuccessful) {
                val entity = UserAcademicProfileEntity(
                    userId = userId,
                    classId = classId,
                    classTitleBn = classTitle,
                    batchYear = batchYear,
                    groupCode = groupCode,
                    groupTitleBn = groupTitle,
                    lastUpdated = System.currentTimeMillis()
                )

                // 1. Write to Room Database (Local Persistence)
                profileDao.insertOrUpdateProfile(entity)

                // 2. Synchronize Session Manager for seamless UI consistency
                UserSessionManager.updateClassAndBatch(
                    newClass = classTitle,
                    newGroup = groupTitle ?: "",
                    newBatch = batchYear ?: ""
                )

                // 3. Cache Invalidation Pipeline: Broadcast global event to trigger cache clearance in ViewModels
                externalScope.launch {
                    _appEvents.emit(
                        AppGlobalEvent.SyllabusChanged(
                            userId = entity.userId,
                            classId = entity.classId,
                            classTitleBn = entity.classTitleBn,
                            batchYear = entity.batchYear,
                            groupCode = entity.groupCode,
                            groupTitleBn = entity.groupTitleBn
                        )
                    )
                    com.example.common.network.AppGlobalEventBus.emit(
                        com.example.common.model.AppEvent.SyllabusUpdated(
                            classId = entity.classId,
                            batch = entity.batchYear ?: "",
                            group = entity.groupCode
                        )
                    )
                }

                Result.success(entity)
            } else {
                Result.failure(Exception("Failed to update syllabus"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves currently active syllabus profile from Room DB
     */
    suspend fun getActiveSyllabus(userId: String = "usr_101"): UserAcademicProfileEntity? = withContext(Dispatchers.IO) {
        profileDao.getProfile(userId)
    }

    /**
     * Observes active syllabus profile as a reactive Flow from Room DB
     */
    fun observeActiveSyllabus(userId: String = "usr_101"): Flow<UserAcademicProfileEntity?> {
        return profileDao.observeProfile(userId)
    }

    companion object {
        @Volatile
        private var INSTANCE: AcademicRepository? = null

        fun getInstance(
            networkDataSource: AcademicNetworkDataSource,
            profileDao: UserAcademicProfileDao,
            configDao: CachedAcademicConfigDao
        ): AcademicRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = AcademicRepository(networkDataSource, profileDao, configDao)
                INSTANCE = instance
                instance
            }
        }

        fun getSharedInstance(): AcademicRepository? = INSTANCE
    }
}

