package com.example.common.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.model.LectureItem
import com.example.common.network.CourseContentUiState
import com.example.common.network.GqlLessonItem
import com.example.common.network.GqlResourceAttachment
import com.example.common.network.GraphQLCourseService
import com.example.common.network.LessonContentType
import com.example.common.viewmodel.CourseViewModel

/**
 * ChapterDetailScreen with 3 Full-Featured Dynamic Tabs:
 * - Tab 1 (ক্লাস ও লেকচার): Live and recorded classes with recording_url HLS video launcher & topics
 * - Tab 2 (পরীক্ষা ও কুইজ): LiveExam and hw_quiz items with user_activity_state ("Upcoming", "Missed", "Completed")
 * - Tab 3 (রিসোর্স ও শিট): Practice books and lecture sheets with pre-signed AWS S3 download/view action
 */
@Composable
fun ChapterDetailScreen(
    chapterId: String = "ch_03",
    chapterTitle: String = "বায়ান্নর দিনগুলি + ফেব্রুয়ারি ১৯৬৯",
    statusBadge: String = "পড়ানো শেষ",
    programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
    phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID,
    subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID,
    onBackClick: () -> Unit,
    onLectureClick: (LectureItem) -> Unit,
    viewModel: CourseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("ক্লাস ও লেকচার", "পরীক্ষা ও কুইজ", "রিসোর্স ও শিট")

    val lessonsState by viewModel.lessonsState.collectAsState()
    val attachmentsState by viewModel.attachmentsState.collectAsState()

    LaunchedEffect(chapterId) {
        viewModel.loadChapterDetails(
            chapterId = chapterId,
            programId = programId,
            phaseId = phaseId,
            subjectId = subjectId
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // 1. Top App Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chapter_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Column {
                        Text(
                            text = chapterTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "HSC একাডেমিক প্রোগ্রাম",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Top-right status badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (statusBadge == "চলমান" || statusBadge == "Running") Color(0xFFEFF6FF) else Color(0xFFDCFCE7),
                    border = if (statusBadge == "চলমান" || statusBadge == "Running") BorderStroke(1.dp, Color(0xFF93C5FD)) else null,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = statusBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (statusBadge == "চলমান" || statusBadge == "Running") Color(0xFF1D4ED8) else Color(0xFF16A34A),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. 3-Tab Selector Bar
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF4F46E5),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF4F46E5),
                        height = 3.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex == index) Color(0xFF4F46E5) else Color(0xFF64748B)
                            )
                        },
                        modifier = Modifier.testTag("chapter_tab_$index")
                    )
                }
            }
        }

        // 3. Tab Content
        when (selectedTabIndex) {
            0 -> ClassesAndLecturesTab(
                lessonsState = lessonsState,
                chapterTitle = chapterTitle,
                onLectureClick = onLectureClick
            )

            1 -> ExamsAndQuizzesTab(
                lessonsState = lessonsState
            )

            2 -> ResourcesAndSheetsTab(
                attachmentsState = attachmentsState,
                onOpenPdf = { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // Fallback or preview
                    }
                }
            )
        }
    }
}

/**
 * ট্যাব ১ (ক্লাস ও লেকচার): লাইভ ক্লাসের তালিকা ও রেকর্ডিং HLS .m3u8 ভিডিও প্লেয়ার অ্যাকশন
 */
@Composable
private fun ClassesAndLecturesTab(
    lessonsState: CourseContentUiState<List<GqlLessonItem>>,
    chapterTitle: String,
    onLectureClick: (LectureItem) -> Unit
) {
    when (lessonsState) {
        is CourseContentUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        }
        is CourseContentUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = lessonsState.message, color = Color(0xFFDC2626))
            }
        }
        is CourseContentUiState.Success -> {
            val classItems = lessonsState.data.filter { !it.isExam }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Quick Header Cards
                item {
                    QuickActionHeaderRow(modifier = Modifier.padding(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))
                }

                if (classItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "এই অধ্যায়ে কোনো লাইভ বা রেকর্ডেড ক্লাস নেই",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(classItems) { lesson ->
                        LessonRowItem(
                            lesson = lesson,
                            onPlayClick = {
                                val lecture = LectureItem(
                                    id = lesson.id,
                                    titleBn = lesson.title,
                                    typeLabel = if (lesson.liveClass?.isOngoing == true) "লাইভ ক্লাস" else "রেকর্ডেড লেকচার",
                                    dateText = lesson.startTime ?: "০১ সেপ্টেম্বর ২০২৬",
                                    statusBadge = lesson.activityStateBadgeBn,
                                    noticeText = if (lesson.liveClass?.recordingUrl != null) "রেকর্ডিং উপলব্ধ" else "",
                                    isExam = false,
                                    instructorName = "হিরন্ময় বাউলিয়া",
                                    instructorBio = "বাংলা শিক্ষক, ঢাবি '০৮",
                                    instructorExp = "১৬ বছর+ শিক্ষকতার অভিজ্ঞতা",
                                    instructorStudents = "১০ লক্ষ+ শিক্ষার্থী পড়েছেন"
                                )
                                onLectureClick(lecture)
                            }
                        )
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFF1F5F9))
                    }
                }
            }
        }
    }
}

/**
 * ট্যাব ২ (পরীক্ষা ও কুইজ): LiveExam এবং hw_quiz আইটেম
 */
@Composable
private fun ExamsAndQuizzesTab(
    lessonsState: CourseContentUiState<List<GqlLessonItem>>
) {
    when (lessonsState) {
        is CourseContentUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        }
        is CourseContentUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = lessonsState.message, color = Color(0xFFDC2626))
            }
        }
        is CourseContentUiState.Success -> {
            val examItems = lessonsState.data.filter { it.isExam }

            if (examItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "বর্তমানে কোনো নির্ধারিত পরীক্ষা নেই",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(examItems) { exam ->
                        ExamCardItem(exam = exam)
                    }
                }
            }
        }
    }
}

/**
 * ট্যাব ৩ (রিসোর্স ও শিট): প্র্যাকটিস বুক এবং লেকচার শিট PDF
 */
@Composable
private fun ResourcesAndSheetsTab(
    attachmentsState: CourseContentUiState<List<GqlResourceAttachment>>,
    onOpenPdf: (url: String) -> Unit
) {
    when (attachmentsState) {
        is CourseContentUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF4F46E5))
            }
        }
        is CourseContentUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = attachmentsState.message, color = Color(0xFFDC2626))
            }
        }
        is CourseContentUiState.Success -> {
            val attachments = attachmentsState.data

            if (attachments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো লেকচার শিট বা প্র্যাকটিস বুক পাওয়া যায়নি",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(attachments) { resource ->
                        ResourceCardItem(
                            resource = resource,
                            onDownloadClick = { onOpenPdf(resource.url) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lesson Row Item with Play Button and Topics
 */
@Composable
private fun LessonRowItem(
    lesson: GqlLessonItem,
    onPlayClick: () -> Unit
) {
    val liveClass = lesson.liveClass
    val isOngoing = liveClass?.isOngoing == true
    val hasRecording = !liveClass?.recordingUrl.isNullOrBlank()

    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .testTag("lesson_item_${lesson.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                // Play / Live Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isOngoing) Color(0xFFFEE2E2)
                            else Color(0xFFEFF6FF)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOngoing) Icons.Default.SmartDisplay else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isOngoing) Color(0xFFDC2626) else Color(0xFF2563EB),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOngoing) "লাইভ ক্লাস চলছে" else "রেকর্ডেড ক্লাস",
                            fontSize = 12.sp,
                            color = if (isOngoing) Color(0xFFDC2626) else Color(0xFF64748B),
                            fontWeight = if (isOngoing) FontWeight.Bold else FontWeight.Medium
                        )

                        if (isOngoing) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = lesson.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 20.sp
                    )

                    // Topics Chips
                    if (liveClass != null && liveClass.topics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            liveClass.topics.forEach { topic ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF3B82F6))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = topic.name,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    if (!lesson.startTime.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = lesson.startTime,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Play / Stream Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOngoing) Color(0xFFDC2626) else Color(0xFF2563EB),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onPlayClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOngoing) "যোগ দিন" else "প্লে করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Exam Card Component
 */
@Composable
private fun ExamCardItem(
    exam: GqlLessonItem
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (exam.hwQuiz != null) "কুইজ হোমওয়ার্ক" else "লাইভ অধ্যায় পরীক্ষা",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = exam.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Activity state chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when (exam.userActivityState.lowercase()) {
                        "completed" -> Color(0xFFDCFCE7)
                        "missed" -> Color(0xFFFEE2E2)
                        else -> Color(0xFFFEF3C7)
                    }
                ) {
                    Text(
                        text = exam.activityStateBadgeBn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (exam.userActivityState.lowercase()) {
                            "completed" -> Color(0xFF15803D)
                            "missed" -> Color(0xFFB91C1C)
                            else -> Color(0xFFB45309)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (!exam.startTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${exam.startTime}  -  ${exam.endTime ?: "সমাপ্ত"}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { /* Handle exam start */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (exam.userActivityState.equals("completed", ignoreCase = true)) Color(0xFFF1F5F9) else Color(0xFFD97706),
                    contentColor = if (exam.userActivityState.equals("completed", ignoreCase = true)) Color(0xFF334155) else Color.White
                )
            ) {
                Text(
                    text = if (exam.userActivityState.equals("completed", ignoreCase = true)) "ফলাফল ও সমাধান দেখুন" else "পরীক্ষা শুরু করুন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Resource / Lecture Sheet Card Component with Direct S3 Download Link
 */
@Composable
private fun ResourceCardItem(
    resource: GqlResourceAttachment,
    onDownloadClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF2F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = resource.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 20.sp
                    )

                    if (!resource.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = resource.description,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = "AWS S3 Verified PDF",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PDF View / Download Action Button
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("btn_download_pdf_${resource.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PDF ভিউ ও ডাউনলোড",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Top quick-action feature cards for Animated lessons, practice quizzes, and E-Books
 */
@Composable
private fun QuickActionHeaderRow(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickMiniCard(
            title = "অ্যানিমেটেড\nভিডিও",
            icon = Icons.Default.VideoLibrary,
            color = Color(0xFF6366F1),
            bgColor = Color(0xFFEEF2FF),
            modifier = Modifier.weight(1f)
        )
        QuickMiniCard(
            title = "প্র্যাকটিস\nকুইজ",
            icon = Icons.Default.Quiz,
            color = Color(0xFF059669),
            bgColor = Color(0xFFECFDF5),
            modifier = Modifier.weight(1f)
        )
        QuickMiniCard(
            title = "স্মার্ট\nই-বুক",
            icon = Icons.Default.MenuBook,
            color = Color(0xFFD97706),
            bgColor = Color(0xFFFFFBEB),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickMiniCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                lineHeight = 14.sp
            )
        }
    }
}
