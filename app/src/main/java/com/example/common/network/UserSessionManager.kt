package com.example.common.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.common.model.EnrolledCourse
import com.example.common.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages persistent user session storage using Android SharedPreferences.
 * Keeps user authentication token and profile persistently stored across app closes and device reboots.
 * The user stays logged in until they explicitly click "Logout".
 */
object UserSessionManager {

    private const val TAG = "UserSessionManager"
    private const val PREFS_NAME = "shikho_user_session_prefs"

    private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
    private const val KEY_ACCESS_TOKEN = "key_access_token"
    private const val KEY_REFRESH_TOKEN = "key_refresh_token"
    private const val KEY_ID_TOKEN = "key_id_token"
    private const val KEY_USER_PROFILE_JSON = "key_user_profile_json"

    private var appContext: Context? = null
    private var cachedPrefs: SharedPreferences? = null

    /**
     * Active user profile state. Dynamically updated upon login, profile update, or session restore.
     */
    var currentUserProfile by mutableStateOf(
        UserProfile(
            id = "",
            name = "শিক্ষার্থী",
            phone = "",
            birthDate = "",
            gender = "",
            avatarUrl = "",
            studentClass = "এইচএসসি",
            group = "মানবিক বিভাগ",
            examBatch = "এইচএসসি ২০২৭",
            classShift = "",
            sscBoard = "",
            sscRoll = "",
            hscBoard = "",
            hscRoll = "",
            boardRegNumber = "",
            institutionDivision = "",
            institutionDistrict = "",
            institutionName = "",
            educationMedium = "বাংলা মাধ্যম",
            guardianName = "",
            guardianPhone = "",
            otherTutoringSources = emptyList(),
            isLoggedIn = false
        )
    )

    /**
     * Enrolled courses state for the active user. Empty by default until loaded or enrolled.
     */
    var enrolledCourses by mutableStateOf<List<EnrolledCourse>>(emptyList())

    /**
     * Initializes the UserSessionManager with the Application Context.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        cachedPrefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Automatically restore session upon initialization
        restoreSessionIfPresent()
    }

    private fun getPrefs(): SharedPreferences? {
        if (cachedPrefs == null && appContext != null) {
            cachedPrefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        return cachedPrefs
    }

    /**
     * Saves complete authentication session (tokens + profile) to persistent storage.
     */
    fun saveSession(
        accessToken: String,
        refreshToken: String? = null,
        idToken: String? = null,
        profile: UserProfile
    ) {
        try {
            val prefs = getPrefs() ?: return
            val profileWithLogin = profile.copy(isLoggedIn = true)
            val profileJson = serializeProfileToJson(profileWithLogin)

            prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken ?: "")
                .putString(KEY_ID_TOKEN, idToken ?: "")
                .putString(KEY_USER_PROFILE_JSON, profileJson)
                .apply()

            // Update in-memory state
            InMemoryAuthRepository.shared.saveTokens(accessToken, refreshToken, idToken)
            currentUserProfile = profileWithLogin

            Log.d(TAG, "User session successfully saved persistently for: ${profile.phone}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user session: ${e.localizedMessage}", e)
        }
    }

    /**
     * Updates and persists changes to the user profile without altering tokens.
     */
    fun saveProfile(profile: UserProfile) {
        try {
            val prefs = getPrefs() ?: return
            val profileJson = serializeProfileToJson(profile)

            prefs.edit()
                .putString(KEY_USER_PROFILE_JSON, profileJson)
                .putBoolean(KEY_IS_LOGGED_IN, profile.isLoggedIn)
                .apply()

            currentUserProfile = profile
            Log.d(TAG, "User profile updated and saved persistently.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile: ${e.localizedMessage}", e)
        }
    }

    /**
     * Restores saved session from persistent storage if the user was previously logged in.
     */
    fun restoreSessionIfPresent(): Boolean {
        try {
            val prefs = getPrefs() ?: return false
            val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)

            if (isLoggedIn && !accessToken.isNullOrBlank()) {
                val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
                val idToken = prefs.getString(KEY_ID_TOKEN, null)
                val profileJson = prefs.getString(KEY_USER_PROFILE_JSON, null)

                // Restore tokens in repository
                InMemoryAuthRepository.shared.saveTokens(accessToken, refreshToken, idToken)

                // Restore profile
                if (!profileJson.isNullOrBlank()) {
                    val profile = deserializeProfileFromJson(profileJson)
                    currentUserProfile = profile.copy(isLoggedIn = true)
                } else {
                    currentUserProfile = currentUserProfile.copy(isLoggedIn = true)
                }
                enrolledCourses = SyllabusDatabaseManager.getEnrolledCoursesForProfile(currentUserProfile)

                Log.d(TAG, "Saved session restored successfully. User is logged in.")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring session: ${e.localizedMessage}", e)
        }
        return false
    }

    /**
     * Clears all session credentials and profile login status from persistent storage.
     * Called when the user explicitly clicks "Log Out".
     */
    fun logout() {
        try {
            val prefs = getPrefs()
            prefs?.edit()
                ?.remove(KEY_IS_LOGGED_IN)
                ?.remove(KEY_ACCESS_TOKEN)
                ?.remove(KEY_REFRESH_TOKEN)
                ?.remove(KEY_ID_TOKEN)
                ?.remove(KEY_USER_PROFILE_JSON)
                ?.apply()

            // Reset In-Memory session
            InMemoryAuthRepository.shared.clearSession()

            // Reset profile login state
            currentUserProfile = currentUserProfile.copy(
                isLoggedIn = false
            )

            Log.d(TAG, "User session cleared. Logged out successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.localizedMessage}", e)
        }
    }

    /**
     * Checks if a valid persistent session exists.
     */
    fun isLoggedIn(): Boolean {
        val prefs = getPrefs() ?: return InMemoryAuthRepository.shared.isLoggedIn
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && !prefs.getString(KEY_ACCESS_TOKEN, "").isNullOrBlank()
    }

    fun getSavedAccessToken(): String? {
        val prefs = getPrefs() ?: return InMemoryAuthRepository.shared.accessToken
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    private fun serializeProfileToJson(profile: UserProfile): String {
        val json = JSONObject()
        json.put("id", profile.id)
        json.put("name", profile.name)
        json.put("phone", profile.phone)
        json.put("birthDate", profile.birthDate)
        json.put("gender", profile.gender)
        json.put("avatarUrl", profile.avatarUrl)
        json.put("studentClass", profile.studentClass)
        json.put("group", profile.group)
        json.put("examBatch", profile.examBatch)
        json.put("classShift", profile.classShift)
        json.put("sscBoard", profile.sscBoard)
        json.put("sscRoll", profile.sscRoll)
        json.put("hscBoard", profile.hscBoard)
        json.put("hscRoll", profile.hscRoll)
        json.put("boardRegNumber", profile.boardRegNumber)
        json.put("institutionDivision", profile.institutionDivision)
        json.put("institutionDistrict", profile.institutionDistrict)
        json.put("institutionName", profile.institutionName)
        json.put("educationMedium", profile.educationMedium)
        json.put("guardianName", profile.guardianName)
        json.put("guardianPhone", profile.guardianPhone)
        json.put("isLoggedIn", profile.isLoggedIn)

        val tutoringArray = JSONArray()
        profile.otherTutoringSources.forEach { tutoringArray.put(it) }
        json.put("otherTutoringSources", tutoringArray)

        return json.toString()
    }

    private fun deserializeProfileFromJson(jsonStr: String): UserProfile {
        return try {
            val json = JSONObject(jsonStr)
            val tutoringList = mutableListOf<String>()
            val tutoringArray = json.optJSONArray("otherTutoringSources")
            if (tutoringArray != null) {
                for (i in 0 until tutoringArray.length()) {
                    tutoringList.add(tutoringArray.optString(i))
                }
            }

            UserProfile(
                id = json.optString("id", ""),
                name = json.optString("name", "Student"),
                phone = json.optString("phone", ""),
                birthDate = json.optString("birthDate", ""),
                gender = json.optString("gender", ""),
                avatarUrl = json.optString("avatarUrl", ""),
                studentClass = json.optString("studentClass", "এইচএসসি"),
                group = json.optString("group", ""),
                examBatch = json.optString("examBatch", ""),
                classShift = json.optString("classShift", ""),
                sscBoard = json.optString("sscBoard", ""),
                sscRoll = json.optString("sscRoll", ""),
                hscBoard = json.optString("hscBoard", ""),
                hscRoll = json.optString("hscRoll", ""),
                boardRegNumber = json.optString("boardRegNumber", ""),
                institutionDivision = json.optString("institutionDivision", ""),
                institutionDistrict = json.optString("institutionDistrict", ""),
                institutionName = json.optString("institutionName", ""),
                educationMedium = json.optString("educationMedium", ""),
                guardianName = json.optString("guardianName", ""),
                guardianPhone = json.optString("guardianPhone", ""),
                otherTutoringSources = tutoringList,
                isLoggedIn = json.optBoolean("isLoggedIn", true)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error deserializing profile: ${e.localizedMessage}")
            currentUserProfile.copy(isLoggedIn = true)
        }
    }
}
