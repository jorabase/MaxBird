package com.example.common.network

import com.example.common.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import android.util.Base64
import android.util.Log
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

        fun extractUserIdFromTokensOrJwt(tokensObj: JSONObject?, accessToken: String): String {
            val fromTokens = tokensObj?.optString("user_id")?.takeIf { it.isNotBlank() }
            if (!fromTokens.isNullOrBlank()) return fromTokens

            try {
                val parts = accessToken.split(".")
                if (parts.size >= 2) {
                    val decoded = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
                    val json = JSONObject(String(decoded, Charsets.UTF_8))
                    val aud = json.optString("aud").takeIf { it.isNotBlank() }
                    if (aud != null) return aud
                    val id = json.optString("id").takeIf { it.isNotBlank() }
                    if (id != null) return id
                    val sub = json.optString("sub").takeIf { it.isNotBlank() }
                    if (sub != null) return sub
                }
            } catch (e: Exception) {
                Log.e("AuthService", "JWT decode error: ${e.message}")
            }
            return ""
        }

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
                val schoolObj = userObj.optJSONObject("school")
                    ?: profileSubObj?.optJSONObject("school")

                // 0. ID
                val parsedId = userObj.optString("id").takeIf { it.isNotBlank() }
                    ?: userObj.optString("user_id").takeIf { it.isNotBlank() }
                    ?: userObj.optString("student_id").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("id")?.takeIf { it.isNotBlank() }
                    ?: dataObj.optString("id").takeIf { it.isNotBlank() }

                // 1. Name
                val firstName = userObj.optString("first_name").ifBlank { profileSubObj?.optString("first_name").orEmpty() }
                val lastName = userObj.optString("last_name").ifBlank { profileSubObj?.optString("last_name").orEmpty() }
                val fullNameCombined = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").takeIf { it.isNotBlank() }

                val parsedName = fullNameCombined
                    ?: userObj.optString("name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("full_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("name_bn").takeIf { it.isNotBlank() }
                    ?: userObj.optString("name_en").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("name")?.takeIf { it.isNotBlank() }

                // 2. Phone
                val parsedPhone = userObj.optString("phone").takeIf { it.isNotBlank() }
                    ?: userObj.optString("mobile").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("phone")?.takeIf { it.isNotBlank() }
                    ?: fallbackPhone.takeIf { it.isNotBlank() }

                // 3. Avatar / Profile Photo (Cloudinary support with HTTPS)
                val rawAvatar = userObj.optString("avatar").takeIf { it.isNotBlank() }
                    ?: userObj.optString("avatar_url").takeIf { it.isNotBlank() }
                    ?: userObj.optString("photo").takeIf { it.isNotBlank() }
                    ?: userObj.optString("profile_pic").takeIf { it.isNotBlank() }
                    ?: userObj.optString("profile_photo").takeIf { it.isNotBlank() }
                    ?: userObj.optString("image").takeIf { it.isNotBlank() }
                    ?: userObj.optString("image_url").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("avatar")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("photo")?.takeIf { it.isNotBlank() }
                val parsedAvatar = rawAvatar?.replace("http://", "https://")

                // 4. College / Institution Name
                val parsedInstitution = schoolObj?.optString("name")?.takeIf { it.isNotBlank() }
                    ?: userObj.optString("institution_name").takeIf { it.isNotBlank() }
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
                val classSubObj = userObj.optJSONObject("class") ?: profileSubObj?.optJSONObject("class")
                val classCode = classSubObj?.optString("code") ?: userObj.optString("class")
                val classDisplay = classSubObj?.optString("display")
                val parsedClass = when {
                    classCode.equals("C11", ignoreCase = true) || classCode.equals("C12", ignoreCase = true) -> "এইচএসসি"
                    classCode.equals("C10", ignoreCase = true) -> "ক্লাস ১০"
                    classCode.equals("C9", ignoreCase = true) -> "ক্লাস ৯"
                    classCode.equals("C8", ignoreCase = true) -> "ক্লাস ৮"
                    classCode.equals("C7", ignoreCase = true) -> "ক্লাস ৭"
                    classCode.equals("C6", ignoreCase = true) -> "ক্লাস ৬"
                    !classDisplay.isNullOrBlank() -> classDisplay
                    else -> userObj.optString("class_name").takeIf { it.isNotBlank() }
                        ?: userObj.optString("student_class").takeIf { it.isNotBlank() }
                        ?: profileSubObj?.optString("class_name")?.takeIf { it.isNotBlank() }
                }

                // 6. Group / Discipline
                val rawGroup = userObj.optString("study_group").takeIf { it.isNotBlank() }
                    ?: userObj.optString("group").takeIf { it.isNotBlank() }
                    ?: userObj.optString("group_name").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("study_group")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("group")?.takeIf { it.isNotBlank() }
                val parsedGroup = when (rawGroup?.uppercase()) {
                    "HUM", "HUMANITIES" -> "মানবিক বিভাগ"
                    "SCI", "SCIENCE" -> "বিজ্ঞান বিভাগ"
                    "BS", "BUS", "BUSINESS", "COMMERCE" -> "ব্যবসায় শিক্ষা বিভাগ"
                    else -> rawGroup
                }

                // 7. Exam Batch
                val passingYear = userObj.optString("passing_year").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("passing_year")?.takeIf { it.isNotBlank() }
                val parsedBatch = if (!passingYear.isNullOrBlank()) {
                    if ((parsedClass ?: "").contains("এইচএসসি")) "এইচএসসি $passingYear"
                    else if ((parsedClass ?: "").contains("১০") || (parsedClass ?: "").contains("৯")) "এসএসসি $passingYear"
                    else "$parsedClass $passingYear"
                } else {
                    userObj.optString("batch").takeIf { it.isNotBlank() }
                        ?: userObj.optString("exam_batch").takeIf { it.isNotBlank() }
                        ?: profileSubObj?.optString("batch")?.takeIf { it.isNotBlank() }
                }

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

                val parsedGuardianPhone = userObj.optString("guardian_mobile").takeIf { it.isNotBlank() }
                    ?: userObj.optString("guardian_phone").takeIf { it.isNotBlank() }
                    ?: userObj.optString("parent_phone").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("guardian_mobile")?.takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("guardian_phone")?.takeIf { it.isNotBlank() }

                // 11. Board & Roll
                val parsedBoard = userObj.optString("ssc_board_name").takeIf { it.isNotBlank() }
                    ?: userObj.optString("board").takeIf { it.isNotBlank() }
                    ?: userObj.optString("ssc_board").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("ssc_board_name")?.takeIf { it.isNotBlank() }

                val parsedRoll = userObj.optString("board_roll_number").takeIf { it.isNotBlank() }
                    ?: userObj.optString("roll").takeIf { it.isNotBlank() }
                    ?: userObj.optString("ssc_roll").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("board_roll_number")?.takeIf { it.isNotBlank() }

                val parsedReg = userObj.optString("board_reg_number").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("board_reg_number")?.takeIf { it.isNotBlank() }

                val parsedHscBoard = userObj.optString("hsc_board_name").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("hsc_board_name")?.takeIf { it.isNotBlank() }

                val parsedHscRoll = userObj.optString("hsc_board_roll_number").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("hsc_board_roll_number")?.takeIf { it.isNotBlank() }

                // 12. Division & District
                val addressObj = schoolObj?.optJSONObject("address") ?: userObj.optJSONObject("address")
                val parsedDivision = addressObj?.optJSONObject("division")?.optString("display")?.takeIf { it.isNotBlank() }
                    ?: userObj.optString("division").takeIf { it.isNotBlank() }
                    ?: profileSubObj?.optString("division")?.takeIf { it.isNotBlank() }

                val parsedDistrict = addressObj?.optJSONObject("district")?.optString("display")?.takeIf { it.isNotBlank() }
                    ?: userObj.optString("district").takeIf { it.isNotBlank() }
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
                    boardRegNumber = parsedReg ?: baseProfile.boardRegNumber,
                    hscBoard = parsedHscBoard ?: baseProfile.hscBoard,
                    hscRoll = parsedHscRoll ?: baseProfile.hscRoll,
                    institutionDivision = parsedDivision ?: baseProfile.institutionDivision,
                    institutionDistrict = parsedDistrict ?: baseProfile.institutionDistrict,
                    id = parsedId ?: baseProfile.id,
                    isLoggedIn = true
                )
            } catch (e: Exception) {
                Log.e("AuthService", "parseUserProfileFromJson error: ${e.message}")
                baseProfile.copy(isLoggedIn = true)
            }
        }
    }

    /**
     * Executes a single GraphQL query and safely parses response data or returns explicit error description.
     */
    private fun executeSingleGraphQLQuery(
        url: String,
        operationName: String,
        query: String,
        variables: JSONObject,
        accessToken: String,
        userId: String
    ): Pair<JSONObject?, String?> {
        val payload = JSONObject().apply {
            put("operationName", operationName)
            put("query", query)
            put("variables", variables)
            put("extensions", JSONObject().apply {
                put("clientLibrary", JSONObject().apply {
                    put("name", "apollo-kotlin")
                    put("version", "5.0.1")
                })
            })
        }

        val userAgent = if (userId.isNotBlank()) {
            "Shikho/(605) 6.0.5 (Android 12; V2029; vivo 2027; en; WIFI; $userId)"
        } else {
            HEADER_USER_AGENT
        }

        return try {
            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(MEDIA_TYPE_JSON.toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Accept", HEADER_ACCEPT)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-User-Timezone", HEADER_TIMEZONE)
                .addHeader("Build-Version", HEADER_BUILD_VERSION)
                .addHeader("User-Agent", userAgent)
                .build()

            client.newCall(request).execute().use { response ->
                val code = response.code
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful && body.isNotBlank()) {
                    val rootJson = JSONObject(body)
                    val dataObj = rootJson.optJSONObject("data")
                    val profileObj = dataObj?.optJSONObject("profile")
                    if (profileObj != null) {
                        Pair(profileObj, null)
                    } else {
                        val errors = rootJson.optJSONArray("errors")
                        val errMsg = if (errors != null && errors.length() > 0) {
                            errors.getJSONObject(0).optString("message", "সার্ভার এরর")
                        } else {
                            "GraphQL data.profile অনুপস্থিত"
                        }
                        Pair(null, errMsg)
                    }
                } else {
                    val errDesc = "HTTP $code: ${response.message.ifBlank { body.take(120) }}"
                    Pair(null, errDesc)
                }
            }
        } catch (e: Exception) {
            Pair(null, e.localizedMessage ?: "নেটওয়ার্ক সংযোগ ত্রুটি")
        }
    }

    private fun parseGraphQLProfileObj(
        profileObj: JSONObject,
        resolvedUserId: String,
        currentProfile: UserProfile
    ): UserProfile {
        val firstName = profileObj.optString("first_name").trim()
        val lastName = profileObj.optString("last_name").trim()
        val fullName = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
            ?: profileObj.optString("name").takeIf { it.isNotBlank() }

        val rawAvatar = profileObj.optString("avatar").ifBlank { profileObj.optString("photo") }
        val cleanAvatar = if (rawAvatar.isNotBlank()) {
            rawAvatar.replace("http://", "https://")
        } else {
            currentProfile.avatarUrl
        }

        val userSubObj = profileObj.optJSONObject("user")
        val phone = userSubObj?.optString("phone")?.takeIf { it.isNotBlank() }
            ?: profileObj.optString("phone").takeIf { it.isNotBlank() }
            ?: currentProfile.phone

        val classObj = profileObj.optJSONObject("class")
        val classCode = classObj?.optString("code") ?: profileObj.optString("class")
        val classDisplay = classObj?.optString("display")
        val studentClass = when {
            classCode.equals("C11", ignoreCase = true) || classCode.equals("C12", ignoreCase = true) -> "এইচএসসি"
            classCode.equals("C10", ignoreCase = true) -> "ক্লাস ১০"
            classCode.equals("C9", ignoreCase = true) -> "ক্লাস ৯"
            classCode.equals("C8", ignoreCase = true) -> "ক্লাস ৮"
            classCode.equals("C7", ignoreCase = true) -> "ক্লাস ৭"
            classCode.equals("C6", ignoreCase = true) -> "ক্লাস ৬"
            !classDisplay.isNullOrBlank() -> classDisplay
            else -> currentProfile.studentClass
        }

        val rawGroup = profileObj.optString("study_group").ifBlank { profileObj.optString("group") }
        val group = when (rawGroup.uppercase()) {
            "HUM", "HUMANITIES" -> "মানবিক বিভাগ"
            "SCI", "SCIENCE" -> "বিজ্ঞান বিভাগ"
            "BS", "BUS", "BUSINESS", "COMMERCE" -> "ব্যবসায় শিক্ষা বিভাগ"
            else -> if (rawGroup.isNotBlank()) rawGroup else currentProfile.group
        }

        val passingYear = profileObj.optString("passing_year")
        val examBatch = if (passingYear.isNotBlank()) {
            if (studentClass.contains("এইচএসসি")) "এইচএসসি $passingYear"
            else if (studentClass.contains("১০") || studentClass.contains("৯")) "এসএসসি $passingYear"
            else "$studentClass $passingYear"
        } else {
            currentProfile.examBatch
        }

        val schoolObj = profileObj.optJSONObject("school")
        val schoolName = schoolObj?.optString("name")?.takeIf { it.isNotBlank() } ?: currentProfile.institutionName
        val schoolId = schoolObj?.optString("id")?.takeIf { it.isNotBlank() } ?: currentProfile.schoolId
        val addressObj = schoolObj?.optJSONObject("address")
        val division = addressObj?.optJSONObject("division")?.optString("display")?.takeIf { it.isNotBlank() } ?: currentProfile.institutionDivision
        val district = addressObj?.optJSONObject("district")?.optString("display")?.takeIf { it.isNotBlank() } ?: currentProfile.institutionDistrict

        val rawGender = profileObj.optString("gender")
        val gender = when (rawGender.uppercase()) {
            "M", "MALE", "BOY" -> "ছাত্র"
            "F", "FEMALE", "GIRL" -> "ছাত্রী"
            else -> if (rawGender.isNotBlank()) rawGender else currentProfile.gender
        }

        val rawShift = profileObj.optString("shift")
        val classShift = when (rawShift.uppercase()) {
            "MORNING" -> "প্রভাতী (সকাল)"
            "DAY" -> "দিবা (দুপুর)"
            "EVENING" -> "সান্ধ্য"
            else -> if (rawShift.isNotBlank()) rawShift else currentProfile.classShift
        }

        val tutoringArray = profileObj.optJSONArray("other_tutoring_source")
        val tutoringSources = if (tutoringArray != null && tutoringArray.length() > 0) {
            (0 until tutoringArray.length()).map { tutoringArray.getString(it) }
        } else currentProfile.otherTutoringSources

        val futurePlanArray = profileObj.optJSONArray("future_plan")
        val futurePlan = if (futurePlanArray != null && futurePlanArray.length() > 0) {
            (0 until futurePlanArray.length()).mapNotNull { futurePlanArray.optString(it).takeIf { s -> s.isNotBlank() } }
        } else {
            val singlePlan = profileObj.optString("future_plan").trim()
            if (singlePlan.isNotBlank()) listOf(singlePlan) else currentProfile.futurePlan
        }
        val dob = profileObj.optString("dob").takeIf { it.isNotBlank() } ?: currentProfile.birthDate
        val guardianName = profileObj.optString("guardian_name").takeIf { it.isNotBlank() } ?: currentProfile.guardianName
        val guardianPhone = profileObj.optString("guardian_mobile").takeIf { it.isNotBlank() } ?: currentProfile.guardianPhone

        val sscBoard = profileObj.optString("ssc_board_name").takeIf { it.isNotBlank() } ?: currentProfile.sscBoard
        val sscRoll = profileObj.optString("board_roll_number").takeIf { it.isNotBlank() } ?: currentProfile.sscRoll
        val boardReg = profileObj.optString("board_reg_number").takeIf { it.isNotBlank() } ?: currentProfile.boardRegNumber
        val hscBoard = profileObj.optString("hsc_board_name").takeIf { it.isNotBlank() } ?: currentProfile.hscBoard
        val hscRoll = profileObj.optString("hsc_board_roll_number").takeIf { it.isNotBlank() } ?: currentProfile.hscRoll
        val id = profileObj.optString("id").takeIf { it.isNotBlank() } ?: resolvedUserId.ifBlank { currentProfile.id }

        return currentProfile.copy(
            id = id,
            name = fullName ?: currentProfile.name,
            phone = phone,
            avatarUrl = cleanAvatar,
            studentClass = studentClass,
            group = group,
            examBatch = examBatch,
            institutionName = schoolName,
            institutionDivision = division,
            institutionDistrict = district,
            schoolId = schoolId,
            classShift = classShift,
            otherTutoringSources = tutoringSources,
            futurePlan = futurePlan,
            gender = gender,
            birthDate = dob,
            guardianName = guardianName,
            guardianPhone = guardianPhone,
            sscBoard = sscBoard,
            sscRoll = sscRoll,
            boardRegNumber = boardReg,
            hscBoard = hscBoard,
            hscRoll = hscRoll,
            isLoggedIn = true
        )
    }

    /**
     * Step 4: Real Shikho GraphQL API sync (GetProfile) for student details, avatar, board info, and institution
     * POST https://api.shikho.com/graphql
     */
    suspend fun syncStudentProfileFromGraphQL(
        accessToken: String,
        userId: String,
        currentProfile: UserProfile
    ): UserProfile = withContext(Dispatchers.IO) {
        if (accessToken.isBlank()) {
            UserSessionManager.profileSyncState = ProfileSyncState.Error("অ্যাক্সেস টোকেন পাওয়া যায়নি")
            return@withContext currentProfile
        }

        val resolvedUserId = if (userId.isNotBlank()) userId else extractUserIdFromTokensOrJwt(null, accessToken)
        val graphqlUrl = "https://api.shikho.com/graphql"
        var lastErrorMsg: String? = null

        // 1. Primary GraphQL Query: GetProfile with user_id
        if (resolvedUserId.isNotBlank()) {
            val queryWithId = """
                query GetProfile(${'$'}user_id: String, ${'$'}type: String!) {
                  profile(user_id: ${'$'}user_id, type: ${'$'}type) {
                    id
                    first_name
                    last_name
                    avatar
                    gender
                    dob
                    guardian_mobile
                    guardian_name
                    ssc_board_name
                    hsc_board_name
                    school_roll
                    board_reg_number
                    board_roll_number
                    hsc_board_roll_number
                    passing_year
                    study_group
                    class {
                      code
                      display
                    }
                    school {
                      name
                      id
                      address {
                        district {
                          code
                          display
                        }
                        division {
                          code
                          display
                        }
                      }
                    }
                    user {
                      email
                      phone
                    }
                  }
                }
            """.trimIndent()

            val varsWithId = JSONObject().apply {
                put("user_id", resolvedUserId)
                put("type", "student")
            }

            val res = executeSingleGraphQLQuery(graphqlUrl, "GetProfile", queryWithId, varsWithId, accessToken, resolvedUserId)
            if (res.first != null) {
                val parsed = parseGraphQLProfileObj(res.first!!, resolvedUserId, currentProfile)
                if (parsed.name.isNotBlank() || parsed.avatarUrl.isNotBlank() || parsed.studentClass.isNotBlank()) {
                    Log.d("AuthService", "GraphQL GetProfile succeeded! Loaded student: ${parsed.name}, avatar=${parsed.avatarUrl}")
                    UserSessionManager.profileSyncState = ProfileSyncState.Success("GraphQL থেকে রিয়েল প্রোফাইল লোড সম্পন্ন")
                    return@withContext parsed
                }
            } else if (res.second != null) {
                lastErrorMsg = res.second
            }
        }

        // 2. Secondary GraphQL Query: Self profile without user_id (Apollo token context)
        val querySelf = """
            query GetMyProfile(${'$'}type: String!) {
              profile(type: ${'$'}type) {
                id
                first_name
                last_name
                avatar
                gender
                dob
                guardian_mobile
                guardian_name
                ssc_board_name
                hsc_board_name
                school_roll
                board_reg_number
                board_roll_number
                hsc_board_roll_number
                passing_year
                study_group
                class {
                  code
                  display
                }
                school {
                  name
                  id
                  address {
                    district {
                      code
                      display
                    }
                    division {
                      code
                      display
                    }
                  }
                }
                user {
                  email
                  phone
                }
              }
            }
        """.trimIndent()

        val varsSelf = JSONObject().apply {
            put("type", "student")
        }

        val resSelf = executeSingleGraphQLQuery(graphqlUrl, "GetMyProfile", querySelf, varsSelf, accessToken, resolvedUserId)
        if (resSelf.first != null) {
            val parsed = parseGraphQLProfileObj(resSelf.first!!, resolvedUserId, currentProfile)
            if (parsed.name.isNotBlank() || parsed.avatarUrl.isNotBlank() || parsed.studentClass.isNotBlank()) {
                Log.d("AuthService", "GraphQL GetMyProfile succeeded! Loaded student: ${parsed.name}")
                UserSessionManager.profileSyncState = ProfileSyncState.Success("GraphQL থেকে রিয়েল প্রোফাইল লোড সম্পন্ন")
                return@withContext parsed
            }
        } else if (resSelf.second != null) {
            lastErrorMsg = resSelf.second
        }

        // 3. Fallback GraphQL Query: Minimal core fields
        val queryMinimal = """
            query GetProfileMinimal(${'$'}type: String!) {
              profile(type: ${'$'}type) {
                id
                first_name
                last_name
                avatar
                gender
                dob
                study_group
                passing_year
                class {
                  code
                  display
                }
                school {
                  name
                }
                user {
                  phone
                  email
                }
              }
            }
        """.trimIndent()

        val resMinimal = executeSingleGraphQLQuery(graphqlUrl, "GetProfileMinimal", queryMinimal, varsSelf, accessToken, resolvedUserId)
        if (resMinimal.first != null) {
            val parsed = parseGraphQLProfileObj(resMinimal.first!!, resolvedUserId, currentProfile)
            if (parsed.name.isNotBlank() || parsed.avatarUrl.isNotBlank()) {
                Log.d("AuthService", "GraphQL GetProfileMinimal succeeded! Loaded student: ${parsed.name}")
                UserSessionManager.profileSyncState = ProfileSyncState.Success("GraphQL থেকে রিয়েল প্রোফাইল লোড সম্পন্ন")
                return@withContext parsed
            }
        } else if (resMinimal.second != null) {
            lastErrorMsg = resMinimal.second
        }

        if (lastErrorMsg != null) {
            Log.e("AuthService", "GraphQL GetProfile returned error: $lastErrorMsg")
            UserSessionManager.profileSyncState = ProfileSyncState.Error(
                message = "GraphQL সার্ভার থেকে প্রোফাইল আসেনি",
                details = lastErrorMsg
            )
        }

        currentProfile
    }

    /**
     * Attempts to fetch full student profile from Shikho GraphQL and REST APIs using the Bearer token.
     */
    suspend fun fetchUserProfile(
        accessToken: String,
        currentProfile: UserProfile,
        userId: String = ""
    ): UserProfile = withContext(Dispatchers.IO) {
        var updatedProfile = currentProfile
        val resolvedUserId = if (userId.isNotBlank()) userId else extractUserIdFromTokensOrJwt(null, accessToken)

        // 1. Primary: Real Shikho GraphQL GetProfile API
        if (resolvedUserId.isNotBlank() || accessToken.isNotBlank()) {
            updatedProfile = syncStudentProfileFromGraphQL(accessToken, resolvedUserId, updatedProfile)
        }

        // 2. Secondary fallback: REST endpoints
        if (updatedProfile.name.isBlank()) {
            val urlsToTry = listOf(
                "https://api.shikho.com/auth/v2/user",
                "https://api.shikho.com/auth/v2/profile",
                "https://api.shikho.com/students/v1/profile",
                "https://api.shikho.com/academic/v1/student/profile"
            )
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
                                val parsed = parseUserProfileFromJson(
                                    jsonString = body,
                                    fallbackPhone = currentProfile.phone,
                                    baseProfile = updatedProfile
                                )
                                if (parsed.name.isNotBlank() || parsed.avatarUrl.isNotBlank()) {
                                    updatedProfile = parsed
                                    UserSessionManager.profileSyncState = ProfileSyncState.Success("REST সার্ভার থেকে রিয়েল প্রোফাইল লোড সম্পন্ন")
                                    return@withContext updatedProfile
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AuthService", "REST fallback error for $url: ${e.message}")
                }
            }
        }

        if (updatedProfile.name.isNotBlank() || updatedProfile.avatarUrl.isNotBlank()) {
            UserSessionManager.profileSyncState = ProfileSyncState.Success("রিয়েল প্রোফাইল লোড সফল")
        } else if (UserSessionManager.profileSyncState !is ProfileSyncState.Error) {
            UserSessionManager.profileSyncState = ProfileSyncState.Error(
                message = "প্রোফাইল লোড সম্পন্ন হয়নি",
                details = "সার্ভার থেকে শিক্ষার্থীর নাম বা ডেটা রিটার্ন হয়নি।"
            )
        }

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
                    val resolvedUserId = extractUserIdFromTokensOrJwt(tokensObj, validToken)
                    var studentProfile = parseUserProfileFromJson(
                        jsonString = responseBodyString,
                        fallbackPhone = cleanPhone
                    ).copy(id = resolvedUserId.ifBlank { "student_$cleanPhone" })

                    if (validToken.isNotEmpty()) {
                        studentProfile = fetchUserProfile(validToken, studentProfile, resolvedUserId)
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

                        val validTokenFinal = if (!accessToken.isNullOrEmpty()) {
                            accessToken
                        } else {
                            "session_token_${System.currentTimeMillis()}"
                        }

                        // 1. Initial parse of user profile from the login response body
                        val resolvedUserId = extractUserIdFromTokensOrJwt(tokensObj, validTokenFinal)
                        var studentProfile = parseUserProfileFromJson(
                            jsonString = responseBodyString,
                            fallbackPhone = cleanPhone
                        ).copy(id = resolvedUserId.ifBlank { "student_$cleanPhone" })

                        // Save in auth repository
                        authRepository.saveTokens(validTokenFinal, refreshToken, idToken)

                        // 2. Fetch full profile using GraphQL GetProfile query
                        if (validTokenFinal.isNotEmpty()) {
                            studentProfile = fetchUserProfile(validTokenFinal, studentProfile, resolvedUserId)
                        }

                        // 3. Immediately sync to persistent storage
                        studentProfile = studentProfile.copy(isLoggedIn = true)
                        UserSessionManager.saveSession(
                            accessToken = validTokenFinal,
                            refreshToken = refreshToken,
                            idToken = idToken,
                            profile = studentProfile
                        )

                        LoginResult.Success(
                            accessToken = validTokenFinal,
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
