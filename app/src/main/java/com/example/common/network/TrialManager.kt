package com.example.common.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.common.model.EnrolledCourse
import com.example.common.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * গ্লোবাল ট্রায়াল পলিসি মডেল (/version API)
 * https://analytics.shikho.com/version
 */
data class GlobalTrialPolicy(
    val trialDurationDays: Int = 3,
    val trialDurationHours: Int = 72,
    val appBannerThumbnailUrl: String = "https://images.shikho.com/banner/free_trial_3days.png",
    val trialEligibilityClasses: List<String> = listOf("C11", "C12", "C10", "C9", "C8", "C7", "C6", "C5")
)

/**
 * ট্রায়াল স্ট্যাটাস স্টেট মেশিন
 */
enum class CourseTrialStatus {
    ELIGIBLE,       // trial_enabled == true, class eligible, not yet activated -> গোলাপী ব্যাজ "৩ দিন সবকিছু ফ্রি!", বাটন "৩ দিন ফ্রিতে শেখো"
    ACTIVE,         // FullApTrial active, time remaining -> ব্যাজ "৩ দিন ফ্রি ট্রায়াল সক্রিয়", বাটন "শেখা চালিয়ে যাও"
    EXPIRED,        // FullApTrial expired (now > trial_end_date) -> ব্যাজ "ফ্রিতে শেখা শেষ", বাটন "বিস্তারিত দেখো" (Paywall active)
    ENROLLED_PAID,  // Paid student enrollment -> ব্যাজ "ভর্তি হয়েছো", বাটন "শেখা চালিয়ে যাও"
    FREE,           // 100% Free course -> ব্যাজ "সম্পূর্ণ ফ্রি!", বাটন "সম্পূর্ণ ফ্রি'তে শুরু করো"
    NOT_ELIGIBLE    // trial_enabled == false or class ineligible -> রেগুলার প্রাইস, বাটন "বিস্তারিত দেখো"
}

/**
 * ট্রায়াল এনরোলমেন্ট রেকর্ড মডেল
 */
data class TrialEnrollmentRecord(
    val programId: String,
    val programTitle: String,
    val studentType: String = "FullApTrial",
    val createdAt: String,
    val trialEndDate: String,
    val trialDurationHours: Int = 72,
    val isExpiredManual: Boolean = false,
    val isPurchased: Boolean = false
) {
    fun isExpired(): Boolean {
        if (isExpiredManual) return true
        if (isPurchased) return false
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val endDate = isoFormat.parse(trialEndDate) ?: return false
            Date().after(endDate)
        } catch (e: Exception) {
            false
        }
    }

    fun getRemainingDaysText(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val endDate = isoFormat.parse(trialEndDate) ?: return "৩ দিন"
            val diffMs = endDate.time - Date().time
            if (diffMs <= 0) return "০ ঘণ্টা"
            val hours = diffMs / (1000 * 60 * 60)
            val days = (hours / 24).toInt()
            if (days > 0) "আর $days দিন বাকি" else "আর $hours ঘণ্টা বাকি"
        } catch (e: Exception) {
            "আর ২ দিন বাকি"
        }
    }
}

/**
 * TrialManager:
 * সার্ভার কনফিগারেশন, ক্লাস এলিজিবিলিটি এবং ইউজার এনরোলমেন্ট স্টেট মেশিনের সমন্বয়কারী।
 */
object TrialManager {

    private const val TAG = "TrialManager"
    private const val PREFS_NAME = "shikho_trial_prefs"
    private const val KEY_TRIALS_JSON = "key_trials_json"

    private var appContext: Context? = null
    private var prefs: SharedPreferences? = null

    // ১. গ্লোবাল ট্রায়াল পলিসি (/version API)
    val globalPolicy = GlobalTrialPolicy()

    // অ্যাক্টিভ ও সংরক্ষিত ট্রায়াল স্টেট
    private val _trialEnrollments = MutableStateFlow<Map<String, TrialEnrollmentRecord>>(emptyMap())
    val trialEnrollments: StateFlow<Map<String, TrialEnrollmentRecord>> = _trialEnrollments.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadSavedTrials()
    }

    private fun loadSavedTrials() {
        try {
            val jsonString = prefs?.getString(KEY_TRIALS_JSON, null)
            if (!jsonString.isNullOrBlank()) {
                val jsonArray = JSONArray(jsonString)
                val map = mutableMapOf<String, TrialEnrollmentRecord>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val pId = obj.getString("program_id")
                    map[pId] = TrialEnrollmentRecord(
                        programId = pId,
                        programTitle = obj.optString("program_title", "কোর্স"),
                        studentType = obj.optString("student_type", "FullApTrial"),
                        createdAt = obj.optString("created_at", ""),
                        trialEndDate = obj.optString("trial_end_date", ""),
                        trialDurationHours = obj.optInt("trial_duration_hours", 72),
                        isExpiredManual = obj.optBoolean("is_expired_manual", false),
                        isPurchased = obj.optBoolean("is_purchased", false)
                    )
                }
                _trialEnrollments.value = map
                Log.d(TAG, "Loaded ${map.size} trial records from persistent storage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading saved trials: ${e.localizedMessage}")
        }
    }

    private fun persistTrials() {
        try {
            val jsonArray = JSONArray()
            _trialEnrollments.value.values.forEach { record ->
                val obj = JSONObject().apply {
                    put("program_id", record.programId)
                    put("program_title", record.programTitle)
                    put("student_type", record.studentType)
                    put("created_at", record.createdAt)
                    put("trial_end_date", record.trialEndDate)
                    put("trial_duration_hours", record.trialDurationHours)
                    put("is_expired_manual", record.isExpiredManual)
                    put("is_purchased", record.isPurchased)
                }
                jsonArray.put(obj)
            }
            prefs?.edit()?.putString(KEY_TRIALS_JSON, jsonArray.toString())?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving trials: ${e.localizedMessage}")
        }
    }

    /**
     * ২. ক্লাস ও ইউজারের যোগ্যতা যাচাই (trial_eligibility)
     * যাচাই করে ব্যবহারকারীর নির্বাচিত ক্লাস (যেমন C11, C10, C5) অনুমোদিত তালিকায় আছে কি না।
     */
    fun isClassEligible(studentClass: String): Boolean {
        val classCode = mapClassNameToCode(studentClass)
        return globalPolicy.trialEligibilityClasses.contains(classCode)
    }

    fun mapClassNameToCode(studentClass: String): String {
        return when {
            studentClass.contains("১১") || studentClass.contains("এইচএসসি") || studentClass.contains("11") -> "C11"
            studentClass.contains("১২") || studentClass.contains("12") -> "C12"
            studentClass.contains("১০") || studentClass.contains("10") -> "C10"
            studentClass.contains("৯") || studentClass.contains("9") -> "C9"
            studentClass.contains("৮") || studentClass.contains("8") -> "C8"
            studentClass.contains("৭") || studentClass.contains("7") -> "C7"
            studentClass.contains("৬") || studentClass.contains("6") -> "C6"
            studentClass.contains("৫") || studentClass.contains("5") -> "C5"
            studentClass.contains("এডমিশন") -> "ADMISSION"
            else -> "C11"
        }
    }

    /**
     * ৩. কোর্স-লেভেল ট্রায়াল স্ট্যাটাস রেজুলেশন
     */
    fun resolveCourseTrialStatus(
        program: AcademicProgramItem,
        userProfile: UserProfile = UserSessionManager.currentUserProfile
    ): CourseTrialStatus {
        // ১. ইতিমধ্যে পেইড পারচেজ থাকলে
        val trialRecord = _trialEnrollments.value[program.id]
        if (trialRecord?.isPurchased == true || (program.isEnrolled && program.type.equals("Paid", ignoreCase = true))) {
            return CourseTrialStatus.ENROLLED_PAID
        }

        // ২. ট্রায়াল রেকর্ড থাকলে সেটার মেয়াদ পরীক্ষা
        if (trialRecord != null) {
            return if (trialRecord.isExpired()) {
                CourseTrialStatus.EXPIRED
            } else {
                CourseTrialStatus.ACTIVE
            }
        }

        // ৩. হার্ডকোডেড অথবা সার্ভার ডেটায় FullApTrial এবং trial_end_date থাকলে (যেমন HSC '27 ২য় বর্ষ)
        if (program.type.equals("FullApTrial", ignoreCase = true)) {
            return if (program.hasEnrolment && program.isActive) {
                CourseTrialStatus.ACTIVE
            } else {
                CourseTrialStatus.EXPIRED
            }
        }

        // ৪. ১০০% ফ্রি কোর্স (যেমন The Next Champ)
        if (program.isFree) {
            return CourseTrialStatus.FREE
        }

        // ৫. ক্লাস যোগ্যতা এবং কোর্স লেভেলে trial_enabled চেক
        if (program.trialEnabled && isClassEligible(userProfile.studentClass)) {
            return CourseTrialStatus.ELIGIBLE
        }

        return CourseTrialStatus.NOT_ELIGIBLE
    }

    /**
     * ৪. ট্রায়াল অ্যাক্টিভেশন ও লাইফসাইকেল (enrollment_details)
     * ইউজার যখন "৩ দিন ফ্রিতে শেখো" বাটনে ক্লিক করেন:
     * - type: "FullApTrial"
     * - created_at: বর্তমান টাইমস্ট্যাম্প
     * - trial_end_date: বর্তমান + ৩ দিন (৭২ ঘণ্টা)
     * - CleverTap & Meta ইভেন্ট "TrialCourse_HomePage" ফায়ার
     * - পুশ নোটিফিকেশন সিডিউল ট্রিগার
     */
    fun activateTrial(
        program: AcademicProgramItem,
        onSuccess: (TrialEnrollmentRecord) -> Unit
    ) {
        val now = Date()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val createdAtStr = isoFormat.format(now)

        // ৭২ ঘণ্টা (৩ দিন) যোগ
        val expiryMs = now.time + (3L * 24 * 60 * 60 * 1000)
        val trialEndDateStr = isoFormat.format(Date(expiryMs))

        val record = TrialEnrollmentRecord(
            programId = program.id,
            programTitle = program.title,
            studentType = "FullApTrial",
            createdAt = createdAtStr,
            trialEndDate = trialEndDateStr,
            trialDurationHours = 72,
            isExpiredManual = false,
            isPurchased = false
        )

        // মেমোরি এবং পারসিসটেন্ট স্টোরেজ আপডেট
        val updatedMap = _trialEnrollments.value.toMutableMap()
        updatedMap[program.id] = record
        _trialEnrollments.value = updatedMap
        persistTrials()

        // ৬. অ্যানালিটিক্স ডিসপ্যাচ
        AnalyticsTracker.trackTrialCourseHomePage(
            programId = program.id,
            accessLevel = "Full",
            hasEnrolment = true,
            isPurchasable = true
        )
        AnalyticsTracker.trackTrialActivated(
            programId = program.id,
            studentType = "FullApTrial",
            trialEndDate = trialEndDateStr
        )

        // হোম স্ক্রিনের "আমার কোর্স"-এ যোগ
        val enrolledItem = EnrolledCourse(
            id = program.id,
            title = program.title,
            badge = "৩ দিন ফ্রি ট্রায়াল",
            instructor = "শিখো একাডেমিক মেন্টরস",
            totalClasses = 60,
            completedClasses = 0,
            colorPrimaryHex = program.colorPrimaryHex,
            colorSecondaryHex = program.colorSecondaryHex
        )
        val currentList = UserSessionManager.enrolledCourses.toMutableList()
        if (currentList.none { it.id == program.id }) {
            currentList.add(0, enrolledItem)
            UserSessionManager.enrolledCourses = currentList
        }

        Log.i(TAG, "Trial activated for ${program.title}. Expiry: $trialEndDateStr")
        onSuccess(record)
    }

    /**
     * ম্যানুয়ালি ট্রায়াল এক্সপায়ার করার মেকানিজম (টেস্টিং এবং ৩ দিন পরবর্তী স্টেট দেখানোর জন্য)
     */
    fun expireTrial(programId: String) {
        val current = _trialEnrollments.value[programId]
        if (current != null) {
            val updated = current.copy(isExpiredManual = true)
            val map = _trialEnrollments.value.toMutableMap()
            map[programId] = updated
            _trialEnrollments.value = map
            persistTrials()
            Log.i(TAG, "Trial manually expired for $programId")
        }
    }

    /**
     * ট্রায়াল রিসেট (টেস্টিং সুবিধা)
     */
    fun resetTrial(programId: String) {
        val map = _trialEnrollments.value.toMutableMap()
        map.remove(programId)
        _trialEnrollments.value = map
        persistTrials()
        Log.i(TAG, "Trial reset for $programId")
    }

    /**
     * ৫. কন্টেন্ট অ্যাক্সেস কন্ট্রোল (access_level: "Full" vs "ReadOnly")
     */
    fun getAccessLevel(programId: String): String {
        val record = _trialEnrollments.value[programId]
        return if (record != null && !record.isExpired()) {
            "Full"
        } else if (record != null && record.isExpired()) {
            "ReadOnly"
        } else {
            // যদি হার্ডকোডেড ২য় বর্ষ কোর্সের মতো ডিফল্ট এক্সপায়ার্ড থাকে
            if (programId == "6864d3a806800acba2e27099") "ReadOnly" else "Full"
        }
    }
}
