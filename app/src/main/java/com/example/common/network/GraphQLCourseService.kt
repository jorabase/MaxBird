package com.example.common.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * GraphQL API Service for Shikho Academic Programs & Course Hierarchy
 * Fully compliant with Shikho (605) 6.0.5 API specifications.
 */
class GraphQLCourseService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build(),
    private val authRepository: AuthRepository = InMemoryAuthRepository.shared
) {

    companion object {
        const val GRAPHQL_ENDPOINT = "https://api.shikho.com/graphql"

        // Default Program and Phase IDs from HAR capture for Bangla 1st Paper
        const val DEFAULT_PROGRAM_ID = "6864d3a806800acba2e27099"
        const val DEFAULT_PHASE_ID = "6864d4e806800acba2e270e1"
        const val DEFAULT_SUBJECT_ID = "609130954"

        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        const val QUERY_PHASE_WISE_CHAPTERS = """
            query PhaseWiseChapters(${'$'}program_id: String!,${'$'}phase_id: String!, ${'$'}subject_id: String!) {
              listAcademicProgramChapters(program_id:${'$'}program_id, phase_id: ${'$'}phase_id, subject_id:${'$'}subject_id, show_chapter_progress_bar: true) {
                data {
                  chapter_id
                  chapter_name
                  chapter_no
                  status
                  class_counter
                  exam_counter
                  chapters_progress_percentage
                }
              }
            }
        """

        const val QUERY_UPCOMING_LESSONS = """
            query GetUpcomingLessonsPhaseWise(${'$'}chapter_id: String!,${'$'}program_id: String!, ${'$'}phase_id: String!) {
              studentSpecificLessons(program_id:${'$'}program_id, chapter_id: ${'$'}chapter_id, phase_id:${'$'}phase_id) {
                data {
                  id
                  title
                  content_type
                  user_activity_state
                  start_time
                  end_time
                  hw_type
                  live_class {
                    id
                    chapter_name
                    recording_url
                    is_on_going
                    type
                    topics {
                      id
                      name
                    }
                  }
                  hw_quiz {
                    id
                    title
                  }
                  hw_animated_video {
                    id
                    topic_name
                  }
                }
              }
            }
        """

        const val QUERY_RESOURCE_ATTACHMENTS = """
            query ResourceAttachmentsOfChapter(${'$'}subject_id: String!,${'$'}module_id: String!, ${'$'}phase_id: String,${'$'}module_name: AttachmentModuleEnum!, ${'$'}chapter_ids: [String]!) {
              attachmentList(module_id:${'$'}module_id, subject_id: ${'$'}subject_id, phase_id:${'$'}phase_id, module_name: ${'$'}module_name, chapter_ids:${'$'}chapter_ids) {
                data {
                  id
                  title
                  description
                  url
                }
              }
            }
        """

        const val QUERY_GET_ACADEMIC_PROGRAM = """
            query GetAcademicProgram(${'$'}batch_id: String, ${'$'}className: AcademicProgramClassEnum, ${'$'}group: StudyGroupTypeEnum, ${'$'}vendor: VendorEnum, ${'$'}classes: [AcademicProgramClassEnum]) {
              listAcademicProgramByEnrollment(batch_id: ${'$'}batch_id, class: ${'$'}className, group: ${'$'}group, vendor: ${'$'}vendor, classes: ${'$'}classes) {
                enrolled_programs {
                  id
                  classes
                  title_bn
                  facebook_group_url
                  banner_url
                  color
                  course_feature_list
                  has_animated_video
                  is_free
                  phase_pricing
                  trial_enabled
                  trial_duration
                  serial
                  subjects {
                    code
                    display
                    display_bn
                    color_code
                    icon
                  }
                  quarter_discount_price
                  full_program_discount_price
                  enrollment_details {
                    expiry_date
                    type
                    created_at
                    batch_id
                    is_on_installment
                    is_active
                    trial_end_date
                    consumable_resources
                    is_qr
                    tag
                  }
                  banner {
                    sub_title
                    url
                    title
                    url_masking
                  }
                }
                other_programs {
                  id
                  classes
                  title_bn
                  facebook_group_url
                  banner_url
                  phase_pricing
                  is_free
                  has_animated_video
                  full_program_discount_price
                  pricing {
                    sale_price_quarterly
                    sale_price_full
                  }
                  trial_enabled
                  trial_duration
                  is_free
                }
                trial_eligibility {
                  classes
                }
                blacklisted_programs {
                  programs
                }
              }
            }
        """
    }

    /**
     * Helper to build HTTP Request with mandatory Shikho headers
     */
    private fun buildRequest(payloadJson: String): Request {
        val requestBuilder = Request.Builder()
            .url(GRAPHQL_ENDPOINT)
            .post(payloadJson.toRequestBody(JSON_MEDIA_TYPE))
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-User-Timezone", "Asia/Dhaka")
            .addHeader("Build-Version", "(605) 6.0.5")
            .addHeader("User-Agent", "Shikho/(605) 6.0.5 (Android 12; V2029; vivo 2027; en; WIFI; )")

        // Include Bearer token if logged in
        val token = UserSessionManager.getSavedAccessToken() ?: authRepository.accessToken
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return requestBuilder.build()
    }

    /**
     * ক. অধ্যায় তালিকা ফেচ (PhaseWiseChapters)
     */
    suspend fun fetchPhaseWiseChapters(
        programId: String = DEFAULT_PROGRAM_ID,
        phaseId: String = DEFAULT_PHASE_ID,
        subjectId: String = DEFAULT_SUBJECT_ID
    ): Result<List<GqlChapterItem>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("operationName", "PhaseWiseChapters")
                put("query", QUERY_PHASE_WISE_CHAPTERS.trimIndent())
                put("variables", JSONObject().apply {
                    put("program_id", programId)
                    put("phase_id", phaseId)
                    put("subject_id", subjectId)
                })
            }

            val request = buildRequest(payload.toString())
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful && bodyString.isNotEmpty()) {
                    val rootJson = JSONObject(bodyString)
                    val dataObj = rootJson.optJSONObject("data")
                    val listObj = dataObj?.optJSONObject("listAcademicProgramChapters")
                    val chaptersArray = listObj?.optJSONArray("data")

                    if (chaptersArray != null && chaptersArray.length() > 0) {
                        val items = mutableListOf<GqlChapterItem>()
                        for (i in 0 until chaptersArray.length()) {
                            val cObj = chaptersArray.getJSONObject(i)
                            items.add(
                                GqlChapterItem(
                                    chapterId = cObj.optString("chapter_id", ""),
                                    chapterName = cObj.optString("chapter_name", "অধ্যায় ${i + 1}"),
                                    chapterNo = cObj.optString("chapter_no", "${i + 1}"),
                                    status = cObj.optString("status", "Finished"),
                                    classCounter = cObj.optInt("class_counter", 0),
                                    examCounter = cObj.optInt("exam_counter", 0),
                                    progressPercentage = cObj.optDouble("chapters_progress_percentage", 0.0)
                                )
                            )
                        }
                        return@withContext Result.success(items)
                    }
                }
            }
            // If network response was empty or error, return empty list
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * খ. অধ্যায়ের ভেতরের ক্লাস ও পরীক্ষা ফেচ (GetUpcomingLessonsPhaseWise)
     */
    suspend fun fetchUpcomingLessons(
        chapterId: String,
        programId: String = DEFAULT_PROGRAM_ID,
        phaseId: String = DEFAULT_PHASE_ID
    ): Result<List<GqlLessonItem>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("operationName", "GetUpcomingLessonsPhaseWise")
                put("query", QUERY_UPCOMING_LESSONS.trimIndent())
                put("variables", JSONObject().apply {
                    put("chapter_id", chapterId)
                    put("program_id", programId)
                    put("phase_id", phaseId)
                })
            }

            val request = buildRequest(payload.toString())
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful && bodyString.isNotEmpty()) {
                    val rootJson = JSONObject(bodyString)
                    val dataObj = rootJson.optJSONObject("data")
                    val lessonsObj = dataObj?.optJSONObject("studentSpecificLessons")
                    val lessonsArray = lessonsObj?.optJSONArray("data")

                    if (lessonsArray != null && lessonsArray.length() > 0) {
                        val items = mutableListOf<GqlLessonItem>()
                        for (i in 0 until lessonsArray.length()) {
                            val lObj = lessonsArray.getJSONObject(i)

                            // Parse live_class
                            var liveClass: GqlLiveClass? = null
                            val lcObj = lObj.optJSONObject("live_class")
                            if (lcObj != null) {
                                val topicsList = mutableListOf<GqlTopic>()
                                val topicsArray = lcObj.optJSONArray("topics")
                                if (topicsArray != null) {
                                    for (t in 0 until topicsArray.length()) {
                                        val tObj = topicsArray.getJSONObject(t)
                                        topicsList.add(
                                            GqlTopic(
                                                id = tObj.optString("id", ""),
                                                name = tObj.optString("name", "")
                                            )
                                        )
                                    }
                                }
                                liveClass = GqlLiveClass(
                                    id = lcObj.optString("id", ""),
                                    chapterName = lcObj.optString("chapter_name", ""),
                                    recordingUrl = lcObj.optString("recording_url", "").ifEmpty { null },
                                    isOngoing = lcObj.optBoolean("is_on_going", false),
                                    type = lcObj.optString("type", ""),
                                    topics = topicsList
                                )
                            }

                            // Parse hw_quiz
                            var hwQuiz: GqlQuiz? = null
                            val qObj = lObj.optJSONObject("hw_quiz")
                            if (qObj != null) {
                                hwQuiz = GqlQuiz(
                                    id = qObj.optString("id", ""),
                                    title = qObj.optString("title", "")
                                )
                            }

                            // Parse hw_animated_video
                            var hwAnimated: GqlAnimatedVideo? = null
                            val aObj = lObj.optJSONObject("hw_animated_video")
                            if (aObj != null) {
                                hwAnimated = GqlAnimatedVideo(
                                    id = aObj.optString("id", ""),
                                    topicName = aObj.optString("topic_name", "")
                                )
                            }

                            items.add(
                                GqlLessonItem(
                                    id = lObj.optString("id", ""),
                                    title = lObj.optString("title", "লেকচার"),
                                    contentType = lObj.optString("content_type", "LiveClass"),
                                    userActivityState = lObj.optString("user_activity_state", "Upcoming"),
                                    startTime = lObj.optString("start_time", "").ifEmpty { null },
                                    endTime = lObj.optString("end_time", "").ifEmpty { null },
                                    hwType = lObj.optString("hw_type", "").ifEmpty { null },
                                    liveClass = liveClass,
                                    hwQuiz = hwQuiz,
                                    hwAnimatedVideo = hwAnimated
                                )
                            )
                        }
                        return@withContext Result.success(items)
                    }
                }
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * গ. লেকচার শিট ও প্র্যাকটিস বুক PDF ফেচ (ResourceAttachmentsOfChapter)
     */
    suspend fun fetchResourceAttachments(
        chapterId: String,
        subjectId: String = DEFAULT_SUBJECT_ID,
        moduleId: String = DEFAULT_PROGRAM_ID,
        phaseId: String = DEFAULT_PHASE_ID
    ): Result<List<GqlResourceAttachment>> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("operationName", "ResourceAttachmentsOfChapter")
                put("query", QUERY_RESOURCE_ATTACHMENTS.trimIndent())
                put("variables", JSONObject().apply {
                    put("subject_id", subjectId)
                    put("module_id", moduleId)
                    put("phase_id", phaseId)
                    put("module_name", "AcademicProgram")
                    put("chapter_ids", JSONArray().apply { put(chapterId) })
                })
            }

            val request = buildRequest(payload.toString())
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful && bodyString.isNotEmpty()) {
                    val rootJson = JSONObject(bodyString)
                    val dataObj = rootJson.optJSONObject("data")
                    val attachListObj = dataObj?.optJSONObject("attachmentList")
                    val dataArray = attachListObj?.optJSONArray("data")

                    if (dataArray != null && dataArray.length() > 0) {
                        val items = mutableListOf<GqlResourceAttachment>()
                        for (i in 0 until dataArray.length()) {
                            val aObj = dataArray.getJSONObject(i)
                            items.add(
                                GqlResourceAttachment(
                                    id = aObj.optString("id", ""),
                                    title = aObj.optString("title", "রিসোর্স ফাইল"),
                                    description = aObj.optString("description", "").ifEmpty { null },
                                    url = aObj.optString("url", "")
                                )
                            )
                        }
                        return@withContext Result.success(items)
                    }
                }
            }
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ঘ. একাডেমিক প্রোগ্রাম ও কোর্স ক্যাটালগ ফেচ (GetAcademicProgram)
     * প্যারামিটার: batch_id="HSC 2027", className="C11", group="Humanities", vendor="BD"
     * ইউজারের JWT টোকেন থেকে প্রাপ্ত "C11,HUM" মেটাডেটা অনুযায়ী ক্লাউড ফিল্টারিং হয়।
     */
    suspend fun fetchAcademicPrograms(
        params: AcademicProgramFilterParams = AcademicProgramFilterParams()
    ): Result<AcademicProgramsCatalog> = withContext(Dispatchers.IO) {
        try {
            // CleverTap ও Meta/Facebook অ্যানালিটিক্স ইভেন্ট ডিসপ্যাচ
            AnalyticsTracker.trackSeeCoursePage(
                batchId = params.batchId,
                className = params.className,
                group = params.group
            )

            val mappedClass = when {
                params.className.contains("11") || params.className.contains("এইচএসসি") -> "C11"
                params.className.contains("12") -> "C12"
                params.className.contains("10") -> "C10"
                params.className.contains("9") -> "C9"
                params.className.contains("8") -> "C8"
                params.className.contains("7") -> "C7"
                params.className.contains("6") -> "C6"
                else -> if (params.className.isNotBlank()) params.className else "C11"
            }

            val mappedGroup = when {
                params.group.contains("মানবিক") || params.group.contains("HUM", ignoreCase = true) -> "Humanities"
                params.group.contains("বিজ্ঞান") || params.group.contains("SCI", ignoreCase = true) -> "Science"
                params.group.contains("ব্যবসায়") || params.group.contains("BS", ignoreCase = true) || params.group.contains("COMMERCE", ignoreCase = true) -> "BusinessStudies"
                else -> "Humanities"
            }

            val mappedBatch = if (params.batchId.isNotBlank()) params.batchId else "HSC 2027"

            val payload = JSONObject().apply {
                put("operationName", "GetAcademicProgram")
                put("query", QUERY_GET_ACADEMIC_PROGRAM.trimIndent())
                put("variables", JSONObject().apply {
                    put("batch_id", mappedBatch)
                    put("className", mappedClass)
                    put("group", mappedGroup)
                    put("vendor", "BD")
                    put("classes", JSONArray().apply { put(mappedClass) })
                })
            }

            val request = buildRequest(payload.toString())
            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (response.isSuccessful && bodyString.isNotEmpty()) {
                    val rootJson = JSONObject(bodyString)
                    val dataObj = rootJson.optJSONObject("data")
                    val progObj = dataObj?.optJSONObject("listAcademicProgramByEnrollment")
                        ?: dataObj?.optJSONObject("getAcademicProgram")
                    if (progObj != null) {
                        val enrolledJson = progObj.optJSONArray("enrolled_programs")
                        val otherJson = progObj.optJSONArray("other_programs")
                        val blacklistJson = progObj.optJSONObject("blacklisted_programs")?.optJSONArray("programs")
                            ?: progObj.optJSONArray("blacklisted_programs")

                        val blacklistedList = mutableListOf<String>()
                        if (blacklistJson != null) {
                            for (i in 0 until blacklistJson.length()) {
                                blacklistedList.add(blacklistJson.getString(i))
                            }
                        }

                        val enrolledList = parseProgramList(enrolledJson)
                        val otherList = parseProgramList(otherJson)

                        // If user has real enrolled programs, update UserSessionManager
                        if (enrolledList.isNotEmpty()) {
                            UserSessionManager.enrolledCourses = enrolledList.map { p ->
                                com.example.common.model.EnrolledCourse(
                                    id = p.id,
                                    title = p.title,
                                    badge = p.badge.ifBlank { "ভর্তি সম্পন্ন" },
                                    instructor = "Shikho Master Teachers",
                                    totalClasses = 520,
                                    completedClasses = 1,
                                    colorPrimaryHex = 0xFF4338CA,
                                    colorSecondaryHex = 0xFF6366F1
                                )
                            }
                        }

                        // ক্লায়েন্ট-সাইড ফিল্টারিং ও সেকশন বিভাজন:
                        // ১. আমার কোর্স -> enrolledList
                        // ২. ফ্রি কোর্স -> otherList where is_free == true
                        // ৩. সকল কোর্স -> otherList where !is_free and id !in blacklistedList
                        val freeList = otherList.filter { it.isFree }
                        val allList = otherList.filter { !it.isFree && !blacklistedList.contains(it.id) }

                        if (enrolledList.isNotEmpty() || freeList.isNotEmpty() || allList.isNotEmpty()) {
                            return@withContext Result.success(
                                AcademicProgramsCatalog(
                                    enrolledPrograms = enrolledList,
                                    freePrograms = freeList,
                                    allCoursesPrograms = allList,
                                    blacklistedPrograms = blacklistedList
                                )
                            )
                        }
                    }
                }
            }
            // Database-driven fallback for student's selected class and syllabus
            val fallbackCatalog = SyllabusDatabaseManager.getCatalogForParams(params)
            Result.success(fallbackCatalog)
        } catch (e: Exception) {
            // Provide database-driven fallback so student always sees their class courses
            val fallbackCatalog = SyllabusDatabaseManager.getCatalogForParams(params)
            Result.success(fallbackCatalog)
        }
    }

    private fun parseProgramList(jsonArray: JSONArray?): List<AcademicProgramItem> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<AcademicProgramItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val pId = obj.optString("id", "")
            val enrollDetails = obj.optJSONObject("enrollment_details")
            val pType = enrollDetails?.optString("type") ?: obj.optString("type", "Paid")
            val pIsFree = obj.optBoolean("is_free", false)
            val titleBn = obj.optString("title_bn").ifBlank { obj.optString("title", "কোর্স") }
            val rawBanner = obj.optString("banner_url").ifBlank { obj.optJSONObject("banner")?.optString("url", "") ?: "" }
            val cleanBanner = if (rawBanner.isNotBlank()) rawBanner.replace("http://", "https://") else null

            val badgeText = when {
                pType.contains("Trial", ignoreCase = true) -> "৩ দিন ফ্রি ট্রায়াল"
                pIsFree -> "ফ্রি কোর্স"
                enrollDetails != null -> "ভর্তি সম্পন্ন"
                else -> obj.optString("badge", "")
            }

            list.add(
                AcademicProgramItem(
                    id = pId,
                    title = titleBn,
                    type = pType,
                    trialEndDate = enrollDetails?.optString("trial_end_date", "")?.ifEmpty { null } ?: obj.optString("trial_end_date", "").ifEmpty { null },
                    hasEnrolment = enrollDetails != null || obj.optBoolean("has_enrolment", false),
                    isFree = pIsFree,
                    isActive = enrollDetails?.optBoolean("is_active", true) ?: obj.optBoolean("is_active", true),
                    expiryDate = enrollDetails?.optString("expiry_date", "")?.ifEmpty { null } ?: obj.optString("expiry_date", "").ifEmpty { null },
                    phasePricing = obj.optInt("phase_pricing", obj.optInt("full_program_discount_price", 0)),
                    trialEnabled = obj.optBoolean("trial_enabled", false),
                    trialDuration = obj.optInt("trial_duration", 3),
                    badge = badgeText,
                    bannerUrl = cleanBanner
                )
            )
        }
        return list
    }
}
