package com.example.common.data.remote

import com.example.common.model.CourseItem
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Real Retrofit interface for fetching My Courses dynamically based on active syllabus query params.
 * ZERO mock data policy — communicates directly with the remote backend.
 */
interface CourseApiService {

    @GET("api/v1/courses/my-courses")
    suspend fun getMyCourses(
        @Query("class_id") classId: String,
        @Query("batch_year") batchYear: String?,
        @Query("group_code") groupCode: String?
    ): Response<CourseListResponse>
}

@JsonClass(generateAdapter = true)
data class CourseListResponse(
    @Json(name = "status") val status: String = "success",
    @Json(name = "data") val data: List<RemoteCourseItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RemoteCourseItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "banner_url") val bannerUrl: String = "",
    @Json(name = "enrollment_status") val enrollmentStatus: String, // "ENROLLED", "TRIAL_ACTIVE", "TRIAL_EXPIRED"
    @Json(name = "is_live_now") val isLiveNow: Boolean = false,
    @Json(name = "trial_days_left") val trialDaysLeft: Int? = null
)

fun RemoteCourseItem.toDomain(): CourseItem = CourseItem(
    id = id,
    title = title,
    bannerUrl = bannerUrl,
    enrollmentStatus = enrollmentStatus,
    isLiveNow = isLiveNow,
    trialDaysLeft = trialDaysLeft
)

object CourseApiClientFactory {
    fun createService(baseUrl: String = "https://api.shikho.com/"): CourseApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(CourseApiService::class.java)
    }
}
