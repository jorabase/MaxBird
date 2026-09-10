package com.example.common.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayLesson
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.network.CourseContentUiState
import com.example.common.network.GqlChapterItem
import com.example.common.network.GraphQLCourseService
import com.example.common.viewmodel.CourseViewModel

/**
 * ChapterListScreen:
 * Displays dynamic academic chapters fetched from GraphQL (PhaseWiseChapters)
 * - Highlights "Running" chapters with vivid status badge
 * - Shows class & exam counters
 * - Shows chapter progress percentage bar
 * - Navigates to ChapterDetailScreen
 */
@Composable
fun ChapterListScreen(
    subjectTitle: String = "বাংলা ১ম পত্র",
    subjectId: String = GraphQLCourseService.DEFAULT_SUBJECT_ID,
    programId: String = GraphQLCourseService.DEFAULT_PROGRAM_ID,
    phaseId: String = GraphQLCourseService.DEFAULT_PHASE_ID,
    onBackClick: () -> Unit,
    onChapterClick: (GqlChapterItem) -> Unit,
    viewModel: CourseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val quarters = listOf("কোয়ার্টার ৩", "কোয়ার্টার ৪", "কোয়ার্টার ৫")
    var selectedQuarter by remember { mutableStateOf("কোয়ার্টার ৪") }

    val chaptersState by viewModel.chaptersState.collectAsState()

    LaunchedEffect(subjectId, phaseId, programId) {
        viewModel.loadChapters(programId, phaseId, subjectId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
    ) {
        // 1. Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chapter_list_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    Column {
                        Text(
                            text = subjectTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "অধ্যায়ভিত্তিক পাঠ পরিকল্পনা ও রিসোর্স",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.loadChapters(programId, phaseId, subjectId) },
                    modifier = Modifier.testTag("refresh_chapters_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "রিফ্রেশ",
                        tint = Color(0xFF4F46E5)
                    )
                }
            }
        }

        // 2. Quarter Selector Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(quarters) { quarter ->
                val isSelected = quarter == selectedQuarter
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF1E3A8A) else Color.White,
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedQuarter = quarter }
                ) {
                    Text(
                        text = quarter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 3. Chapters Content State
        when (val state = chaptersState) {
            is CourseContentUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF4F46E5))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "অধ্যায়ের তালিকা লোড হচ্ছে...",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            is CourseContentUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4F46E5),
                            modifier = Modifier.clickable {
                                viewModel.loadChapters(programId, phaseId, subjectId)
                            }
                        ) {
                            Text(
                                text = "পুনরায় চেষ্টা করুন",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            is CourseContentUiState.Success -> {
                val chapterList = state.data
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chapterList) { chapter ->
                        ChapterCardItem(
                            chapter = chapter,
                            onClick = { onChapterClick(chapter) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Individual Chapter Card with Running Highlight, Counters, and Progress Bar
 */
@Composable
fun ChapterCardItem(
    chapter: GqlChapterItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRunning = chapter.isRunning

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(
            width = if (isRunning) 1.5.dp else 1.dp,
            color = if (isRunning) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRunning) 4.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("chapter_card_${chapter.chapterId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Chapter No + Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Chapter number badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRunning) Brush.linearGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                                ) else Brush.linearGradient(
                                    listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chapter.chapterNo.ifEmpty { "১" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRunning) Color.White else Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = chapter.chapterName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Badge (Highlight if Running)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isRunning -> Color(0xFFEFF6FF)
                        chapter.status.equals("Finished", ignoreCase = true) -> Color(0xFFDCFCE7)
                        else -> Color(0xFFF1F5F9)
                    },
                    border = if (isRunning) BorderStroke(1.dp, Color(0xFF93C5FD)) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRunning) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2563EB))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = chapter.statusBadgeBn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isRunning -> Color(0xFF1D4ED8)
                                chapter.status.equals("Finished", ignoreCase = true) -> Color(0xFF15803D)
                                else -> Color(0xFF475569)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Counters: Classes counter + Exam counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Class counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayLesson,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${chapter.classCounter} টি ক্লাস",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Exam counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${chapter.examCounter} টি পরীক্ষা",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar & Percentage
            val progressInt = chapter.progressPercentage.toInt().coerceIn(0, 100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { (progressInt / 100f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (progressInt == 100) Color(0xFF16A34A) else Color(0xFF3B82F6),
                    trackColor = Color(0xFFF1F5F9)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "$progressInt%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (progressInt == 100) Color(0xFF16A34A) else Color(0xFF0F172A)
                )
            }
        }
    }
}
