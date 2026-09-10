package com.example.common.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.model.ChapterItem
import com.example.common.network.CourseContentUiState
import com.example.common.viewmodel.CourseViewModel

/**
 * Screen matching Screenshot 4 (Subject Chapter Breakdown: বাংলা ১ম পত্র)
 */
@Composable
fun SubjectDetailScreen(
    subjectTitle: String = "বাংলা ১ম পত্র",
    onBackClick: () -> Unit,
    onChapterClick: (ChapterItem) -> Unit,
    viewModel: CourseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val quarters = listOf("কোয়ার্টার ৩", "কোয়ার্টার ৪", "কোয়ার্টার ৫")
    var selectedQuarter by remember { mutableStateOf("কোয়ার্টার ৪") }

    val chaptersState by viewModel.chaptersState.collectAsState()

    LaunchedEffect(subjectTitle) {
        viewModel.loadChapters()
    }

    val chapters = remember(chaptersState) {
        when (val state = chaptersState) {
            is CourseContentUiState.Success -> {
                state.data.map { gql ->
                    val statusBadge = when (gql.status) {
                        "Finished" -> "পড়ানো শেষ"
                        "Running" -> "পড়ানো হচ্ছে"
                        else -> "আসন্ন"
                    }
                    ChapterItem(
                        id = gql.chapterId,
                        titleBn = gql.chapterName,
                        statusBadge = statusBadge,
                        isCompleted = gql.status == "Finished",
                        classesCount = gql.classCounter,
                        examsCount = gql.examCounter
                    )
                }
            }
            else -> emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
    ) {
        // 1. Top App Bar with back button, title, and subject badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("subject_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color(0xFF0F172A)
                    )
                }

                Text(
                    text = subjectTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // Top-right subject crest badge
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF2F2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "অ আ\nক খ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFDC2626),
                    lineHeight = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Quarter Selector Pills (matching Screenshot 4)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(quarters) { quarter ->
                val isSelected = quarter == selectedQuarter

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFF1F5F9),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { selectedQuarter = quarter }
                ) {
                    Text(
                        text = quarter,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar with 0%
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { 0f },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "০%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Quick Action Cards (Animated Lessons, Practice Quiz, E-Book)
        QuickFeatureActionCards(
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(
            thickness = 6.dp,
            color = Color(0xFFF8FAFC)
        )

        // 4. Chapter List (matching Screenshot 4)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(chapters) { chapter ->
                ChapterListItem(
                    chapter = chapter,
                    onClick = { onChapterClick(chapter) }
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color(0xFFF1F5F9)
                )
            }
        }
    }
}

/**
 * 3 Quick Feature Action Cards (matching Screenshot 4 & 5)
 */
@Composable
fun QuickFeatureActionCards(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickFeatureCardItem(
            title = "অ্যানিমেটেড লেসনস",
            icon = Icons.Default.VideoLibrary,
            iconTint = Color(0xFFEC4899),
            iconContainerColor = Color(0xFFFDF2F8),
            modifier = Modifier.weight(1f)
        )

        QuickFeatureCardItem(
            title = "প্র্যাকটিস কুইজ",
            icon = Icons.Default.Quiz,
            iconTint = Color(0xFF06B6D4),
            iconContainerColor = Color(0xFFECFEFF),
            modifier = Modifier.weight(1f)
        )

        QuickFeatureCardItem(
            title = "ই-বুক",
            icon = Icons.Default.MenuBook,
            iconTint = Color(0xFF3B82F6),
            iconContainerColor = Color(0xFFEFF6FF),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickFeatureCardItem(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconContainerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
        ),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF334155),
                maxLines = 1
            )
        }
    }
}

/**
 * Chapter List Item (matching Screenshot 4)
 */
@Composable
private fun ChapterListItem(
    chapter: ChapterItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("chapter_item_${chapter.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left circular book icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and badges
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.titleBn,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Status badge (e.g. পড়ানো শেষ in green, পড়ানো হচ্ছে in purple)
                val isCompleted = chapter.statusBadge == "পড়ানো শেষ"
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCompleted) Color(0xFFDCFCE7) else Color(0xFFF3E8FF)
                ) {
                    Text(
                        text = chapter.statusBadge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF16A34A) else Color(0xFF9333EA),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "ক্লাস: ${chapter.classesCount}টি",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                Text(
                    text = "এক্সাম: ${chapter.examsCount}টি",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Right circular Play button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "শুরু করুন",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
