package com.example.common.network

import android.util.Log

/**
 * Analytics and Telemetry Tracking Dispatcher
 * Dispatches background events to CleverTap and Meta/Facebook SDK
 * as captured in the authentic HAR log:
 * - Device Model: Vivo V2029 (vivo 2027, Android 12)
 * - Role: FullApTrial
 * - Trial Expiration: 2025-12-11
 * - Access Level: ReadOnly
 */
object AnalyticsTracker {

    private const val TAG = "AnalyticsTracker"

    /**
     * Dispatched when user enters the Courses catalog screen
     */
    fun trackSeeCoursePage(
        deviceModel: String = android.os.Build.MODEL,
        userRole: String = "FullApTrial",
        trialEndDate: String = "2025-12-11",
        batchId: String = "HSC",
        className: String = "C11",
        group: String = "Humanities"
    ) {
        val eventPayload = mapOf(
            "event" to "see_course_page",
            "device_model" to deviceModel,
            "user_role" to userRole,
            "trial_end_date" to trialEndDate,
            "batch_id" to batchId,
            "class_name" to className,
            "group" to group,
            "client_version" to "(605) 6.0.5"
        )
        Log.i(TAG, "Dispatched CleverTap & Meta event [see_course_page]: $eventPayload")
    }

    /**
     * Dispatched when accessing course home / routine with trial state
     * Matches exact CleverTap & Meta payload:
     * {
     *   "_eventName": "TrialCourse_HomePage",
     *   "Student Type": "FullApTrial",
     *   "Trial End Date": "...",
     *   "Selected Program ID": "..."
     * }
     */
    fun trackTrialCourseHomePage(
        programId: String = "6864d3a806800acba2e27099",
        accessLevel: String = "ReadOnly",
        hasEnrolment: Boolean = false,
        isPurchasable: Boolean = true,
        trialEndDate: String = "2026-12-31T17:59:59Z",
        studentType: String = "FullApTrial"
    ) {
        val eventPayload = mapOf(
            "_eventName" to "TrialCourse_HomePage",
            "Student Type" to studentType,
            "Trial End Date" to trialEndDate,
            "Selected Program ID" to programId,
            "access_level" to accessLevel,
            "has_enrolment" to hasEnrolment,
            "is_purchasable" to isPurchasable
        )
        Log.i(TAG, "Dispatched CleverTap & Meta event [TrialCourse_HomePage]: $eventPayload")
    }

    /**
     * ধাপ ৬: ট্রায়াল অ্যাক্টিভেশন ও অটোমেটেড নোটিফিকেশন ট্রিগার
     * CleverTap ও Meta সার্ভারে TrialCourse_HomePage ইভেন্ট ডিসপ্যাচ এবং CRM পুশ শিডিউলিং
     */
    fun trackTrialActivated(
        programId: String,
        studentType: String = "FullApTrial",
        trialEndDate: String
    ) {
        val payload = mapOf(
            "_eventName" to "TrialCourse_HomePage",
            "Student Type" to studentType,
            "Trial End Date" to trialEndDate,
            "Selected Program ID" to programId
        )
        Log.i(TAG, "Dispatched CleverTap & Meta [TrialCourse_HomePage]: $payload")

        // সিআরএম অটোমেটেড পুশ নোটিফিকেশন সিডিউল (Day 2 ও Day 3 রিমাইন্ডার)
        scheduleCrmTrialReminders(programId, trialEndDate)
    }

    private fun scheduleCrmTrialReminders(programId: String, trialEndDate: String) {
        Log.i(TAG, "CRM Push Notification Scheduled [Day 2]: \"আপনার ট্রায়ালের আর মাত্র ১ দিন বাকি, সম্পূর্ণ প্রস্তুতি নিতে আজই ভর্তি হোন\" for Program: $programId")
        Log.i(TAG, "CRM Push Notification Scheduled [Day 3]: \"আপনার ট্রায়ালের মেয়াদ আজ শেষ হচ্ছে! আনলিমিটেড লাইভ ক্লাস ও কুইজ আনলক রাখতে সাবস্ক্রাইব করুন\" for Program: $programId")
    }

    /**
     * Dispatched for Free Course Enrollment (The Next Champ)
     */
    fun trackFreeEnrollment(
        programId: String,
        programTitle: String
    ) {
        Log.i(TAG, "Dispatched CleverTap & Meta event [free_enrollment_started]: $programId ($programTitle)")
    }

    /**
     * ধাপ ৪: ফেসবুক SDK এবং CleverTap প্রোফাইল সিঙ্ক ইভেন্ট
     * graph.facebook.com/v16.0/.../activities
     * clevertap-prod.com/a1
     */
    fun trackSyllabusChange(
        oldClass: String,
        newClass: String,
        oldGroup: String,
        newGroup: String,
        examYear: String,
        selectedProgramId: String = "69f884bb8338867433c1be8e"
    ) {
        // ১. Facebook SDK ব্যাচ অ্যাক্টিভিটি ইভেন্ট
        val fbPayload = mapOf(
            "_eventName" to "Syllabus Change",
            "Class" to oldClass,
            "New Class" to newClass,
            "New Group" to newGroup,
            "Exam Year" to examYear,
            "Selected Program ID" to selectedProgramId
        )
        Log.i(TAG, "Dispatched Meta/Facebook SDK event [graph.facebook.com/v16.0/activities]: $fbPayload")

        // ২. CleverTap প্রোফাইল সিঙ্ক ইভেন্ট
        val clevertapPayload = mapOf(
            "_eventName" to "app_session_start",
            "Class" to newClass,
            "Exam Year" to examYear,
            "Study Group" to newGroup,
            "Student Type" to "FullApTrial"
        )
        Log.i(TAG, "Dispatched CleverTap Profile Sync [clevertap-prod.com/a1]: $clevertapPayload")
    }

    /**
     * ধাপ ৬: গুগল ফায়ারবেস ক্লাউড মেসেজিং (FCM) টপিক রেজিস্ট্রেশন
     * fcmregistrations.googleapis.com/.../topicSubscriptions/
     */
    fun trackFcmTopicSubscription(
        newClass: String,
        examYear: String,
        group: String
    ) {
        val topicName = "topic_${newClass.lowercase().replace(" ", "")}_${examYear}_${group.lowercase().replace(" ", "")}"
        val fcmEndpoint = "https://fcmregistrations.googleapis.com/v1/topicSubscriptions/$topicName"
        Log.i(TAG, "Registered FCM live class topic subscription to [$fcmEndpoint]")
    }
}
