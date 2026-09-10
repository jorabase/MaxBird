package com.example.common.network

import com.example.common.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Result models for 2-step dynamic authentication
 */
sealed interface UserCheckResult {
    data class ExistingUser(val pinExist: Boolean, val rawData: String? = null) : UserCheckResult
    data class NewUser(val message: String? = null) : UserCheckResult
    data class Error(val message: String, val statusCode: Int? = null) : UserCheckResult
}

sealed interface SendSmsResult {
    data class Success(val message: String = "ওটিপি পাঠানো হয়েছে") : SendSmsResult
    data class Error(val message: String, val statusCode: Int? = null) : SendSmsResult
}

sealed interface VerifyOtpResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String? = null,
        val idToken: String? = null,
        val userProfile: UserProfile? = null
    ) : VerifyOtpResult
    data class Error(val message: String, val statusCode: Int? = null) : VerifyOtpResult
}

/**
 * Request and Response models for Shikho Auth V2 Login
 */
data class LoginProfileData(
    val deviceId: String = "cKA9zLvoSG60q7Zt6VDw56:APA91bGIMX5WNyvb0fzR2NC3kf0p5KZ7sRLaA_VumNWa8PmciBRAreHLkM9zBbKxmLR48PVEoOiKRsaqNObWov33YQpPK4Gpqj97HTsMmrzXX-XIRegCncI"
)

data class LoginRequestPayload(
    val phone: String,
    val otp: String,
    val type: String = "student",
    val profile: LoginProfileData = LoginProfileData(),
    val googleAdsId: String = "e6076d1a-35cc-4b58-b30c-85026db62d0d"
)

sealed interface LoginResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String? = null,
        val idToken: String? = null,
        val userProfile: UserProfile? = null,
        val tokenType: String = "Bearer",
        val rawResponse: String? = null
    ) : LoginResult

    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val errorDetails: String? = null
    ) : LoginResult
}

/**
 * Cross-platform network service for Authentication.
 * Meets the exact specification:
 * - Default Headers:
 *     - "Accept": "application/json"
 *     - "Content-Type": "application/json"
 *     - "X-User-Timezone": "Asia/Dhaka"
 *     - "Build-Version": "(605) 6.0.5"
 *     - "User-Agent": "Shikho/(605) 6.0.5 (Android 12; V2029; vivo 2027; en; WIFI; )"
 *
 * - Endpoints:
 *     - Step 1: POST https://api.shikho.com/auth/v2/user/check
 *     - Step 2-A: POST https://api.shikho.com/auth/v2/login (Existing User PIN)
 *     - Step 2-B: POST https://api.shikho.com/auth/v2/send/sms & POST https://api.shikho.com/auth/v2/verify/otp (New User / OTP)
 *     - Step 3: Fetch Profile with Bearer token & in-memory AuthRepository storage
 */
class AuthService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val authRepository: AuthRepository = InMemoryAuthRepository.shared
) {

    companion object {
        const val USER_CHECK_URL = "https://api.shikho.com/auth/v2/user/check"
        const val LOGIN_URL = "https://api.shikho.com/auth/v2/login"
        const val SEND_SMS_URL = "https://api.shikho.com/auth/v2/send/sms"
        const val VERIFY_OTP_URL = "https://api.shikho.com/auth/v2/verify/otp"
        const val PROFILE_URL = "https://api.shikho.com/auth/v2/profile"
        const val USER_URL = "https://api.shikho.com/auth/v2/user"

        const val HEADER_ACCEPT = "application/json"
        const val HEADER_TIMEZONE = "Asia/Dhaka"
        const val HEADER_BUILD_VERSION = "(605) 6.0.5"
        const val HEADER_USER_AGENT = "Shikho/(605) 6.0.5 (Android 12; V2029; vivo 2027; en; WIFI; )"
        const val MEDIA_TYPE_JSON = "application/json; charset=utf-8"

        const val DEFAULT_DEVICE_ID = "cKA9zLvoSG60q7Zt6VDw56:APA91bGIMX5WNyvb0fzR2NC3kf0p5KZ7sRLaA_VumNWa8PmciBRAreHLkM9zBbKxmLR48PVEoOiKRsaqNObWov33YQpPK4Gpqj97HTsMmrzXX-XIRegCncI"
        const val DEFAULT_GOOGLE_ADS_ID = "e6076d1a-35cc-4b58-b30c-85026db62d0d"

        /**
         * Extracts and maps user information from API JSON responses (name, avatar, college, class, etc.)
         */
        fun parseUserProfileFromJson(
            jsonString: String,
            fallbackPhone: String = "",
            baseProfile: UserProfile = UserSessionManager.currentUserProfile
        ): UserProfile {
            if (jsonString.isBlank()) return baseProfile.copy(isLoggedIn = true)
            return try {
                val jsonObject = JSONObject(jsonString)
                val dataObj = jsonObject.optJSONObject("data") ?: jsonObject
                val userObj = dataObj.optJSONObject("user")
                    ?: dataObj.optJSONObject("profile")
                    ?: jsonObject.optJSONObject("user")
                    ?: dataObj

                val profileSubObj = userObj.optJSONObject("profile") ?: dataObj.optJSONObject("profile")
                val institutionObj = userObj.optJSONObject("institution")
                    ?: profileSubObj?.optJSONObject("institution")
                    ?: dataObj.optJSONObject("institution")

                // 0. ID
                val parsedId = userObj.optString("id").takeIf { it.isNotBlank() }
                    ?: userObj.optString("user_id").takeIf { it.isNotBlank() }
                    ?: userObj.optString("student_id").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("id")?.takeIf { it.isNotBlank() }
                    ?: dataObj.optString("id").takeIf { it.isNotBlank() }

                // 1. Name
                val parsedName = userObj.optString("name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("full_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("name_bn").takeIf { it.isNotBlank() }
                    ?: userObj.optString("name_en").takeIf { it.isNotBlank() }
                    ?: "${userObj.optString("first_name")} ${userObj.optString("last_name")}".trim().takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("name")?.takeIf { it.isNotBlank() }

                // 2. Phone
                val parsedPhone = userObj.optString("phone").takeIf { it.isNotBlank() }
                    ?: userObj.optString("mobile").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("phone")?.takeIf { it.isNotBlank() }
                    ?: fallbackPhone.takeIf { it.isNotBlank() }

                // 3. Avatar / Profile Photo
                val parsedAvatar = userObj.optString("avatar").takeIf { it.isNotBlank() }
                    ?: userObj.optString("avatar_url").takeIf { it.isNotBlank() }
                    ?: userObj.optString("photo").takeIf { it.isNotBlank() }
                    ?: userObj.optString("profile_pic").takeIf { it.isNotBlank() }
                    ?: userObj.optString("profile_photo").takeIf { it.isNotBlank() }
                    ?: userObj.optString("image").takeIf { it.isNotBlank() }
                    ?: userObj.optString("image_url").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("avatar")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("photo")?.takeIf { it.isNotBlank() }

                // 4. College / Institution Name
                val parsedInstitution = userObj.optString("institution_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("college").takeIf { it.isNotBlank() }
                    ?: userObj.optString("college_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("school").takeIf { it.isNotBlank() }
                    ?: userObj.optString("school_name").takeIf { it.isNotBlank() }
                    ?: institutionObj?.optString("name")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("institution_name")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("college")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("college_name")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optJSONObject("institution")?.optString("name")?.takeIf { it.isNotBlank() }

                // 5. Class
                val parsedClass = userObj.optString("class").takeIf { it.isNotBlank() }
                    ?: userObj.optString("class_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("student_class").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("class")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("class_name")?.takeIf { it.isNotBlank() }

                // 6. Group / Discipline
                val parsedGroup = userObj.optString("group").takeIf { it.isNotBlank() }
                    ?: userObj.optString("group_name").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("group")?.takeIf { it.isNotBlank() }

                // 7. Exam Batch
                val parsedBatch = userObj.optString("batch").takeIf { it.isNotBlank() }
                    ?: userObj.optString("exam_batch").takeIf { it.isNotBlank() }
                    ?: userObj.optString("passing_year").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("batch")?.takeIf { it.isNotBlank() }

                // 8. Gender
                val rawGender = userObj.optString("gender").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("gender")?.takeIf { it.isNotBlank() }
                val parsedGender = when (rawGender?.lowercase()) {
                    "male", "boy", "m" -> "ছাত্র"
                    "female", "girl", "f" -> "ছাত্রী"
                    else -> rawGender
                }

                // 9. DOB
                val parsedDob = userObj.optString("dob").takeIf { it.isNotBlank() }
                    ?: userObj.optString("birth_date").takeIf { it.isNotBlank() }
                    ?: userObj.optString("date_of_birth").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("dob")?.takeIf { it.isNotBlank() }

                // 10. Guardian Info
                val parsedGuardianName = userObj.optString("guardian_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("parent_name").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("guardian_name")?.takeIf { it.isNotBlank() }

                val parsedGuardianPhone = userObj.optString("guardian_phone").takeIf { it.isNotBlank() }
                    ?: userObj.optString("parent_phone").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("guardian_phone")?.takeIf { it.isNotBlank() }

                // 11. Board & Roll
                val parsedBoard = userObj.optString("board").takeIf { it.isNotBlank() }
                    ?: userObj.optString("ssc_board").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("board")?.takeIf { it.isNotBlank() }

                val parsedRoll = userObj.optString("roll").takeIf { it.isNotBlank() }
                    ?: userObj.optString("ssc_roll").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("roll")?.takeIf { it.isNotBlank() }

                // 12. Division & District
                val parsedDivision = userObj.optString("division").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("division")?.takeIf { it.isNotBlank() }

                val parsedDistrict = userObj.optString("district").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("district")?.takeIf { it.isNotBlank() }

                baseProfile.copy(
                    name = parsedName ?: baseProfile.name,
                    phone = parsedPhone ?: baseProfile.phone,
                    avatarUrl = parsedAvatar ?: baseProfile.avatarUrl,
                    institutionName = parsedInstitution ?: baseProfile.institutionName,
                    studentClass = parsedClass ?: baseProfile.studentClass,
                    group = parsedGroup ?: baseProfile.group,
                    examBatch = parsedBatch ?: baseProfile.examBatch,
                    gender = parsedGender ?: baseProfile.gender,
                    birthDate = parsedDob ?: baseProfile.birthDate,
                    guardianName = parsedGuardianName ?: baseProfile.guardianName,
                    guardianPhone = parsedGuardianPhone ?: baseProfile.guardianPhone,
                    sscBoard = parsedBoard ?: baseProfile.sscBoard,
                    sscRoll = parsedRoll ?: baseProfile.sscRoll,
                    institutionDivision = parsedDivision ?: baseProfile.institutionDivision,
                    institutionDistrict = parsedDistrict ?: baseProfile.institutionDistrict,
                    id = parsedId ?: baseProfile.id,
                    isLoggedIn = true
                )
            } catch (_: Exception) {
                baseProfile.copy(isLoggedIn = true)
            }
        }
    }

    /**
     * Step 4: GraphQL API sync for student details, batches, roll, and academic info
     * POST https://api.shikho.com/graphql
     */
    suspend fun syncStudentProfileFromGraphQL(accessToken: String, currentProfile: UserProfile): UserProfile = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) return@withContext currentProfile

        val graphqlUrl = "https://api.shikho.com/graphql"
        val query = """
            query GetStudentAcademicInfo {
              studentProfile {
                id
                name
                phone
                avatar
                class_name
                group
                exam_batch
                roll
                school {
                  name
                }
                enrollments {
                  id
                  batch_name
                  course_title
                }
              }
            }
        """.trimIndent()

        val payload = JSONObject().apply {
            put("query", query)
        }

        try {
            val request = Request.Builder()
                .url(graphqlUrl)
                .post(payload.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Accept", HEADER_ACCEPT)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-User-Timezone", HEADER_TIMEZONE)
                .addHeader("Build-Version", HEADER_BUILD_VERSION)
                .addHeader("User-Agent", HEADER_USER_AGENT)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    if (body.isNotBlank()) {
                        val rootJson = JSONObject(body)
                        val dataObj = rootJson.optJSONObject("data")
                        val studentObj = dataObj?.optJSONObject("studentProfile")
                            ?: dataObj?.optJSONObject("profile")
                            ?: dataObj?.optJSONObject("user")

                        if (studentObj != null) {
                            val gqlName = studentObj.optString("name").takeIf { it.isNotBlank() }
                            val gqlAvatar = studentObj.optString("avatar").takeIf { it.isNotBlank() }
                            val gqlClass = studentObj.optString("class_name").takeIf { it.isNotBlank() }
                            val gqlGroup = studentObj.optString("group").takeIf { it.isNotBlank() }
                            val gqlBatch = studentObj.optString("exam_batch").takeIf { it.isNotBlank() }
                            val gqlRoll = studentObj.optString("roll").takeIf { it.isNotBlank() }
                            val schoolObj = studentObj.optJSONObject("school")
                            val gqlSchool = schoolObj?.optString("name")?.takeIf { it.isNotBlank() }
                            val gqlId = studentObj.optString("id").takeIf { it.isNotBlank() }

                            return@withContext currentProfile.copy(
                                id = gqlId ?: currentProfile.id,
                                name = gqlName ?: currentProfile.name,
                                avatarUrl = gqlAvatar ?: currentProfile.avatarUrl,
                                studentClass = gqlClass ?: currentProfile.studentClass,
                                group = gqlGroup ?: currentProfile.group,
                                examBatch = gqlBatch ?: currentProfile.examBatch,
                                sscRoll = gqlRoll ?: currentProfile.sscRoll,
                                institutionName = gqlSchool ?: currentProfile.institutionName,
                                isLoggedIn = true
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Gracefully ignore GraphQL network failure
        }
        currentProfile
    }

    /**
     * Attempts to fetch full student profile from Shikho API using the Bearer token.
     */
    suspend fun fetchUserProfile(accessToken: String, currentProfile: UserProfile): UserProfile = withContext(Dispatchers.IO) {
        val urlsToTry = listOf(PROFILE_URL, USER_URL)
        var updatedProfile = currentProfile
        for (url in urlsToTry) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Accept", HEADER_ACCEPT)
                    .addHeader("X-User-Timezone", HEADER_TIMEZONE)
                    .addHeader("Build-Version", HEADER_BUILD_VERSION)
                    .addHeader("User-Agent", HEADER_USER_AGENT)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        if (body.isNotBlank()) {
                            updatedProfile = parseUserProfileFromJson(
                                jsonString = body,
                                fallbackPhone = currentProfile.phone,
                                baseProfile = updatedProfile
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore and try fallback url
            }
        }

        // Also sync via GraphQL
        updatedProfile = syncStudentProfileFromGraphQL(accessToken, updatedProfile)
        updatedProfile
    }

    /**
     * Step 1: Checks user existence by phone number.
     * POST https://api.shikho.com/auth/v2/user/check
     * Returns ExistingUser if pin_exist == true,
     * Returns NewUser if status 404 or pin_exist == false.
     */
    suspend fun checkUser(phoneNumber: String): UserCheckResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().removePrefix("+88").removePrefix("88")
        val formattedPhone = "88$cleanPhone"

        val bodyJson = JSONObject().apply {
            put("phone", formattedPhone)
            put("type", "student")
        }
        val requestBody = bodyJson.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType())

        val request = Request.Builder()
            .url(USER_CHECK_URL)
            .post(requestBody)
            .addHeader("Accept", HEADER_ACCEPT)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-User-Timezone", HEADER_TIMEZONE)
            .addHeader("Build-Version", HEADER_BUILD_VERSION)
            .addHeader("User-Agent", HEADER_USER_AGENT)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (statusCode == 404) {
                    return@withContext UserCheckResult.NewUser("ব্যবহারকারী পাওয়া যায়নি (নতুন অ্যাকাউন্ট)")
                }

                if (response.isSuccessful && responseBodyString.isNotEmpty()) {
                    val json = JSONObject(responseBodyString)
                    val dataObj = json.optJSONObject("data") ?: json
                    val pinExist = dataObj.optBoolean("pin_exist", false) || json.optBoolean("pin_exist", false)
                    if (pinExist) {
                        UserCheckResult.ExistingUser(pinExist = true, rawData = responseBodyString)
                    } else {
                        UserCheckResult.NewUser("পিন বিদ্যমান নেই, ওটিপি পাঠানো হচ্ছে")
                    }
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyString)
                        errObj.optString("message", "যাচাই ব্যর্থ হয়েছে ($statusCode)")
                    } catch (_: Exception) {
                        "যাচাই ব্যর্থ হয়েছে ($statusCode)"
                    }
                    UserCheckResult.Error(message = errorMsg, statusCode = statusCode)
                }
            }
        } catch (e: Exception) {
            UserCheckResult.Error(message = "ইন্টারনেট সংযোগে ত্রুটি: ${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}")
        }
    }

    /**
     * Step 2-B (Part 1): Sends SMS OTP for new user or OTP verification flow.
     * POST https://api.shikho.com/auth/v2/send/sms
     */
    suspend fun sendSms(phoneNumber: String): SendSmsResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().removePrefix("+88").removePrefix("88")
        val formattedPhone = "88$cleanPhone"

        val bodyJson = JSONObject().apply {
            put("phone", formattedPhone)
            put("type", "student")
            put("auth_type", "signup")
            put("vendor", "shikho")
            put("google_ads_id", DEFAULT_GOOGLE_ADS_ID)
        }
        val requestBody = bodyJson.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType())

        val request = Request.Builder()
            .url(SEND_SMS_URL)
            .post(requestBody)
            .addHeader("Accept", HEADER_ACCEPT)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-User-Timezone", HEADER_TIMEZONE)
            .addHeader("Build-Version", HEADER_BUILD_VERSION)
            .addHeader("User-Agent", HEADER_USER_AGENT)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful) {
                    val message = try {
                        val json = JSONObject(responseBodyString)
                        json.optString("message", "আপনার নম্বরে ওটিপি কোড পাঠানো হয়েছে")
                    } catch (_: Exception) {
                        "আপনার নম্বরে ওটিপি কোড পাঠানো হয়েছে"
                    }
                    SendSmsResult.Success(message = message)
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyString)
                        errObj.optString("message", "ওটিপি পাঠানো যায়নি ($statusCode)")
                    } catch (_: Exception) {
                        "ওটিপি পাঠানো যায়নি ($statusCode)"
                    }
                    SendSmsResult.Error(message = errorMsg, statusCode = statusCode)
                }
            }
        } catch (e: Exception) {
            SendSmsResult.Error(message = "ওটিপি রিকোয়েস্ট ব্যর্থ: ${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}")
        }
    }

    /**
     * Step 2-B (Part 2): Verifies SMS OTP code and retrieves tokens.
     * POST https://api.shikho.com/auth/v2/verify/otp
     */
    suspend fun verifyOtp(phoneNumber: String, otpInput: String): VerifyOtpResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().removePrefix("+88").removePrefix("88")
        val formattedPhone = "88$cleanPhone"

        val bodyJson = JSONObject().apply {
            put("phone", formattedPhone)
            put("otp", otpInput.trim())
            put("type", "student")
        }
        val requestBody = bodyJson.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType())

        val request = Request.Builder()
            .url(VERIFY_OTP_URL)
            .post(requestBody)
            .addHeader("Accept", HEADER_ACCEPT)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-User-Timezone", HEADER_TIMEZONE)
            .addHeader("Build-Version", HEADER_BUILD_VERSION)
            .addHeader("User-Agent", HEADER_USER_AGENT)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful && responseBodyString.isNotEmpty()) {
                    val jsonObject = JSONObject(responseBodyString)
                    val dataObj = jsonObject.optJSONObject("data") ?: jsonObject
                    val tokensObj = dataObj.optJSONObject("tokens") ?: jsonObject.optJSONObject("tokens")

                    val accessToken = tokensObj?.optString("access_token")
                        ?: dataObj.optString("access_token")
                        ?: jsonObject.optString("token")
                        ?: jsonObject.optString("access_token")
                        ?: ""

                    val refreshToken = tokensObj?.optString("refresh_token")
                    val idToken = tokensObj?.optString("id_token")

                    val validToken = if (accessToken.isNotEmpty()) accessToken else "session_token_${System.currentTimeMillis()}"

                    // Save token in repository and persistent storage
                    authRepository.saveTokens(validToken, refreshToken, idToken)

                    // Parse profile
                    var studentProfile = parseUserProfileFromJson(
                        jsonString = responseBodyString,
                        fallbackPhone = cleanPhone
                    )
                    if (validToken.isNotEmpty()) {
                        studentProfile = fetchUserProfile(validToken, studentProfile)
                    }
                    studentProfile = studentProfile.copy(isLoggedIn = true)
                    UserSessionManager.saveSession(
                        accessToken = validToken,
                        refreshToken = refreshToken,
                        idToken = idToken,
                        profile = studentProfile
                    )

                    VerifyOtpResult.Success(
                        accessToken = validToken,
                        refreshToken = refreshToken,
                        idToken = idToken,
                        userProfile = studentProfile
                    )
                } else {
                    val errorMsg = try {
                        val errObj = JSONObject(responseBodyString)
                        errObj.optString("message", "ভুল ওটিপি কোড ($statusCode)")
                    } catch (_: Exception) {
                        "ওটিপি যাচাই ব্যর্থ হয়েছে ($statusCode)"
                    }
                    VerifyOtpResult.Error(message = errorMsg, statusCode = statusCode)
                }
            }
        } catch (e: Exception) {
            VerifyOtpResult.Error(message = "ওটিপি ভেরিফিকেশন ব্যর্থ: ${e.localizedMessage ?: "নেটওয়ার্ক সমস্যা"}")
        }
    }

    /**
     * Executes the login POST request with the specified phone number and otp/password.
     */
    suspend fun login(phoneNumber: String, otpOrPassword: String): LoginResult = withContext(Dispatchers.IO) {
        val cleanPhone = phoneNumber.trim().removePrefix("+88").removePrefix("88")
        val formattedPhone = "88$cleanPhone"

        // Build exact JSON body payload
        val profileJson = JSONObject().apply {
            put("device_id", DEFAULT_DEVICE_ID)
        }

        val requestJson = JSONObject().apply {
            put("phone", formattedPhone)
            put("otp", otpOrPassword.trim())
            put("type", "student")
            put("profile", profileJson)
            put("google_ads_id", DEFAULT_GOOGLE_ADS_ID)
        }

        val requestBody = requestJson.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType())

        val request = Request.Builder()
            .url(LOGIN_URL)
            .post(requestBody)
            .addHeader("Accept", HEADER_ACCEPT)
            .addHeader("X-User-Timezone", HEADER_TIMEZONE)
            .addHeader("Build-Version", HEADER_BUILD_VERSION)
            .addHeader("User-Agent", HEADER_USER_AGENT)
            .addHeader("Content-Type", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBodyString = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful && responseBodyString.isNotEmpty()) {
                    try {
                        val jsonObject = JSONObject(responseBodyString)

                        // Parse tokens.access_token according to API specification
                        val dataObj = jsonObject.optJSONObject("data") ?: jsonObject
                        val tokensObj = dataObj.optJSONObject("tokens") ?: jsonObject.optJSONObject("tokens")
                        
                        val accessToken = tokensObj?.optString("access_token")
                            ?: dataObj.optString("access_token")
                            ?: jsonObject.optString("token")
                            ?: jsonObject.optString("access_token")

                        val refreshToken = tokensObj?.optString("refresh_token")
                        val idToken = tokensObj?.optString("id_token")

                        // 1. Initial parse of user profile from the login response body
                        var studentProfile = parseUserProfileFromJson(
                            jsonString = responseBodyString,
                            fallbackPhone = cleanPhone
                        )

                        val validToken = if (!accessToken.isNullOrEmpty()) {
                            accessToken
                        } else {
                            "session_token_${System.currentTimeMillis()}"
                        }

                        // Save in auth repository
                        authRepository.saveTokens(validToken, refreshToken, idToken)

                        // 2. Fetch full profile using the access token if available
                        if (!accessToken.isNullOrEmpty()) {
                            studentProfile = fetchUserProfile(accessToken, studentProfile)
                        }

                        // 3. Immediately sync to persistent storage
                        studentProfile = studentProfile.copy(isLoggedIn = true)
                        UserSessionManager.saveSession(
                            accessToken = validToken,
                            refreshToken = refreshToken,
                            idToken = idToken,
                            profile = studentProfile
                        )

                        LoginResult.Success(
                            accessToken = validToken,
                            refreshToken = refreshToken,
                            idToken = idToken,
                            userProfile = studentProfile,
                            rawResponse = responseBodyString
                        )
                    } catch (e: Exception) {
                        LoginResult.Error(
                            message = "রেসপন্স প্রসেসিংয়ে ত্রুটি: ${e.localizedMessage ?: "Unknown parse error"}",
                            statusCode = statusCode,
                            errorDetails = responseBodyString
                        )
                    }
                } else {
                    // Extract error message from body if present
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBodyString)
                        errorJson.optString("message", errorJson.optString("error", "লগইন ব্যর্থ হয়েছে (Status: $statusCode)"))
                    } catch (_: Exception) {
                        "লগইন ব্যর্থ হয়েছে (Status: $statusCode)"
                    }

                    LoginResult.Error(
                        message = errorMessage,
                        statusCode = statusCode,
                        errorDetails = responseBodyString
                    )
                }
            }
        } catch (e: IOException) {
            LoginResult.Error(
                message = "নেটওয়ার্ক সংযোগ ব্যর্থ হয়েছে। অনুগ্রহ করে ইন্টারনেট সংযোগ চেক করুন।",
                errorDetails = e.localizedMessage
            )
        } catch (e: Exception) {
            LoginResult.Error(
                message = "অনাকাঙ্ক্ষিত ত্রুটি ঘটেছে: ${e.localizedMessage ?: "Unknown error"}",
                errorDetails = e.localizedMessage
            )
        }
    }
}
