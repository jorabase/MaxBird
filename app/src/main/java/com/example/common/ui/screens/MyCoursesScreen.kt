package com.example.common.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.ui.components.CourseCardItem
import com.example.common.ui.components.CourseCardSkeleton
import com.example.common.viewmodel.MyCoursesUiState
import com.example.common.viewmodel.MyCoursesViewModel

/**
 * Dedicated Reactive "আমার কোর্স" (My Courses) Screen.
 * Zero Mock Data Policy:
 * - Displays shimmer loading skeletons when fetching.
 * - Displays empty card with "সিলেবাস পরিবর্তন করুন" CTA when remote API returns empty.
 * - Displays error state with retry.
 * - Renders 100% real dynamic course cards.
 */
@Composable
fun MyCoursesScreen(
    viewModel: MyCoursesViewModel,
    onCourseClick: (String) -> Unit = {},
    onContinueLearning: (String) -> Unit = {},
    onViewDetails: (String) -> Unit = {},
    onEnrollCourse: (String) -> Unit = {},
    onChangeSyllabusClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, ENROLLED, TRIAL

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Top App Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_back_my_courses")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "আমার কোর্স",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "সিলেবাস অনুযায়ী ব্যক্তিগতকৃত কোর্সসমূহ",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refresh() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("btn_refresh_courses")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                }

                // Active Syllabus Banner inside Header
                if (uiState is MyCoursesUiState.Success || uiState is MyCoursesUiState.Empty) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val (classTitle, batchText, groupText) = when (val state = uiState) {
                        is MyCoursesUiState.Success -> Triple(
                            state.classTitleBn,
                            state.batchYear?.let { "ব্যাচ $it" },
                            state.groupTitleBn
                        )
                        is MyCoursesUiState.Empty -> Triple(
                            state.classTitleBn,
                            null,
                            null
                        )
                        else -> Triple("", null, null)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                            .clickable { onChangeSyllabusClick() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E7FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF4F46E5),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = listOfNotNull(classTitle, batchText, groupText).joinToString(" • "),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "সক্রিয় সিলেবাস",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChangeCircle,
                                contentDescription = "Change Syllabus",
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "পরিবর্তন",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Content Body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (val state = uiState) {
                is MyCoursesUiState.Loading -> {
                    // Shimmer skeleton cards
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(3) {
                            CourseCardSkeleton()
                        }
                    }
                }

                is MyCoursesUiState.Empty -> {
                    // Empty state when remote API returns zero courses
                    EmptyCoursesView(
                        message = state.message,
                        onChangeSyllabusClick = onChangeSyllabusClick
                    )
                }

                is MyCoursesUiState.Error -> {
                    // Error state with retry action
                    ErrorCoursesView(
                        errorMessage = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                is MyCoursesUiState.Success -> {
                    val filteredCourses = when (selectedFilterTab) {
                        "ENROLLED" -> state.courses.filter { it.enrollmentType == "ENROLLED" }
                        "TRIAL" -> state.courses.filter { it.enrollmentType != "ENROLLED" }
                        else -> state.courses
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Filter chips header
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    CourseFilterChip(
                                        title = "সবগুলো (${state.courses.size})",
                                        isSelected = selectedFilterTab == "ALL",
                                        onClick = { selectedFilterTab = "ALL" },
                                        testTag = "filter_all"
                                    )
                                }
                                val enrolledCount = state.courses.count { it.enrollmentType == "ENROLLED" }
                                if (enrolledCount > 0) {
                                    item {
                                        CourseFilterChip(
                                            title = "ভর্তি হওয়া ($enrolledCount)",
                                            isSelected = selectedFilterTab == "ENROLLED",
                                            onClick = { selectedFilterTab = "ENROLLED" },
                                            testTag = "filter_enrolled"
                                        )
                                    }
                                }
                                val trialCount = state.courses.count { it.enrollmentType != "ENROLLED" }
                                if (trialCount > 0) {
                                    item {
                                        CourseFilterChip(
                                            title = "ট্রায়াল / অফার ($trialCount)",
                                            isSelected = selectedFilterTab == "TRIAL",
                                            onClick = { selectedFilterTab = "TRIAL" },
                                            testTag = "filter_trial"
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredCourses.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "এই ফিল্টারে কোনো কোর্স পাওয়া যায়নি।",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredCourses, key = { it.courseId }) { cardState ->
                                CourseCardItem(
                                    cardState = cardState,
                                    onCardClick = onCourseClick,
                                    onLeftActionClick = { id ->
                                        if (cardState.enrollmentType == "ENROLLED") {
                                            onContinueLearning(id)
                                        } else {
                                            onViewDetails(id)
                                        }
                                    },
                                    onRightActionClick = { id ->
                                        onEnrollCourse(id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCoursesView(
    message: String,
    onChangeSyllabusClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "কোনো কোর্স পাওয়া যায়নি",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 13.5.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onChangeSyllabusClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_empty_change_syllabus"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00C853),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChangeCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সিলেবাস পরিবর্তন করুন",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorCoursesView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE4E6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "তথ্য লোড করা যায়নি",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "পুনরায় চেষ্টা করুন")
                }
            }
        }
    }
}

@Composable
private fun CourseFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color(0xFF0F172A) else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}
