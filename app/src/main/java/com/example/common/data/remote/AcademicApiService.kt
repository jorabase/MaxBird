package com.example.common.data.remote

import com.example.common.model.ApiResponse
import com.example.common.model.ClassItem
import com.example.common.model.UpdateSyllabusRequest
import com.example.common.model.UpdateSyllabusResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import java.util.concurrent.TimeUnit

/**
 * Retrofit interface for Academic Syllabus Endpoints
 */
interface AcademicApiService {

    @GET("api/v2/academic/classes-config")
    suspend fun getAcademicConfig(): Response<ApiResponse<List<ClassItem>>>

    @PATCH("api/v2/user/academic-profile")
    suspend fun updateAcademicProfile(
        @Body request: UpdateSyllabusRequest
    ): Response<Unit>
}

/**
 * Service implementation providing network requests without mock data fallback
 */
class AcademicNetworkDataSource(
    private val apiService: AcademicApiService? = null
) {
    suspend fun fetchAcademicConfig(): Response<ApiResponse<List<ClassItem>>> = withContext(Dispatchers.IO) {
        if (apiService != null) {
            return@withContext apiService.getAcademicConfig()
        }
        // In case apiService is not injected properly, throw exception to prevent silent failures
        throw IllegalStateException("AcademicApiService is not initialized")
    }

    suspend fun updateAcademicProfile(
        request: UpdateSyllabusRequest
    ): Response<Unit> = withContext(Dispatchers.IO) {
        if (apiService != null) {
            return@withContext apiService.updateAcademicProfile(request)
        }
        throw IllegalStateException("AcademicApiService is not initialized")
    }

    companion object {
        fun createRetrofitService(baseUrl: String = "https://api.shikho.com/"): AcademicApiService {
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

            return retrofit.create(AcademicApiService::class.java)
        }
    }
}


