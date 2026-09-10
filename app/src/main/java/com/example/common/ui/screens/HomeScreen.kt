package com.example.common.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.common.ui.components.UserAvatarView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.model.DaySchedule
import com.example.common.model.EnrolledCourse
import com.example.common.model.MonthClassOverview
import com.example.common.model.RoutineClassItem
import com.example.common.network.CourseContentUiState
import com.example.common.network.UserSessionManager
import com.example.common.viewmodel.CourseViewModel

private val DarkNavyHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF060C2A),
        Color(0xFF0B1446),
        Color(0xFF0E1A58)
    )
)

private val ActiveBluePill = Color(0xFF4361EE)
private val LightPillBackground = Color(0xFFEFF5F9)
private val DotCyanColor = Color(0xFF06B6D4)
private val PinkTimeColor = Color(0xFFDB2777)
private val GoldCrownColor = Color(0xFFF59E0B)

private fun generateDefaultWeeklySchedule(): List<DaySchedule> {
    val dayNames = listOf(
        Pair("শনি", "শনিবার"),
        Pair("রবি", "রবিবার"),
        Pair("সোম", "সোমবার"),
        Pair("মঙ্গল", "মঙ্গলবার"),
        Pair("বুধ", "বুধবার"),
        Pair("বৃহঃ", "বৃহস্পতিবার"),
        Pair("শুক্র", "শুক্রবার")
    )
    val dates = listOf("০৫", "০৬", "০৭", "০৮", "০৯", "১০", "১১")
    return dayNames.mapIndexed { index, (shortName, fullName) ->
        DaySchedule(
            dayIndex = index,
            dayNameBn = fullName,
            dayShortBn = shortName,
            dateText = dates.getOrElse(index) { "০১" },
            fullDateBn = "${fullName}, ${dates.getOrElse(index) { "০১" }}/০৯/২০২৬",
            classes = emptyList(),
            exams = emptyList()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onClassClick: (RoutineClassItem) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSyllabusChangeClick: () -> Unit = {},
    onAdmissionInfoClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    viewModel: CourseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val userProfile = UserSessionManager.currentUserProfile
    val courses = remember(UserSessionManager.enrolledCourses, userProfile.studentClass, userProfile.group) {
        UserSessionManager.enrolledCourses.ifEmpty {
            com.example.common.network.SyllabusDatabaseManager.getEnrolledCoursesForProfile(userProfile)
        }
    }
    var selectedCourse by remember(courses) { mutableStateOf(courses.first()) }
    var selectedDayIndex by remember { mutableIntStateOf(4) } // Wednesday (৯) as in screenshot
    var showCourseSwitcherSheet by remember { mutableStateOf(false) }
    var showMonthlyScheduleSheet by remember { mutableStateOf(false) }
    var showProfileOptionsSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val chaptersState by viewModel.chaptersState.collectAsState()

    LaunchedEffect(selectedCourse.id) {
        viewModel.loadChapters()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout",
                    tint = Color(0xFFEF4444)
                )
            },
            title = {
                Text(
                    text = "লগ আউট নিশ্চিতকরণ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "আপনি কি নিশ্চিত যে আপনি আপনার অ্যাকাউন্ট (${userProfile.phone}) থেকে লগ আউট করতে চান?\n\nলগ আউট না করা পর্যন্ত অ্যাপ বন্ধ করলেও লগইন সংরক্ষিত থাকবে।",
                    fontSize = 14.sp,
                    color = Color(0xFF475569)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        UserSessionManager.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("হ্যাঁ, লগ আউট করুন", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    val weeklySchedule = remember(userProfile.studentClass, userProfile.group, selectedCourse.id) {
        com.example.common.network.SyllabusDatabaseManager.getWeeklyRoutineForProfile(userProfile)
    }

    val currentDaySchedule = weeklySchedule.find { it.dayIndex == selectedDayIndex }
        ?: weeklySchedule.first()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060C2A))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Navy Blue Header Section (Exact match with user's uploaded screenshot)
            item {
                NavyHeroHeaderSection(
                    selectedCourse = selectedCourse,
                    onCourseDropdownClick = { showCourseSwitcherSheet = true },
                    onProfileClick = { showProfileOptionsSheet = true }
                )
            }

            // 2. White Curved Container with Routine, 7-Day Bar & Class Cards
            item {
                Surface(
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 22.dp, bottom = 100.dp, start = 18.dp, end = 18.dp)
                    ) {
                        // Routine Header Row
                        RoutineHeaderBar(
                            apiState = chaptersState,
                            onViewAllClick = { showMonthlyScheduleSheet = true }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 7-Day Day Selector Bar
                        SevenDaySelectorScreenshot(
                            days = weeklySchedule,
                            selectedDayIndex = selectedDayIndex,
                            onDaySelected = { selectedDayIndex = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Date & Summary indicators (e.g. বুধবার, ০৯/০৯/২০২৬ • ক্লাস ২ • এক্সাম ০)
                        DateAndSummaryIndicators(
                            daySchedule = currentDaySchedule
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Horizontal Class Cards Carousel (Exact cards from screenshot)
                        AnimatedContent(
                            targetState = currentDaySchedule,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
                            },
                            label = "routine_carousel_content"
                        ) { schedule ->
                            HorizontalClassesCarousel(
                                daySchedule = schedule,
                                onClassClick = onClassClick
                            )
                        }
                    }
                }
            }
        }

        // Course Switcher Bottom Sheet
        if (showCourseSwitcherSheet) {
            CourseSwitcherBottomSheet(
                courses = courses,
                selectedCourse = selectedCourse,
                onCourseSelected = {
                    selectedCourse = it
                    showCourseSwitcherSheet = false
                },
                onDismiss = { showCourseSwitcherSheet = false }
            )
        }

        // Monthly Routine Bottom Sheet ("সব দেখো")
        if (showMonthlyScheduleSheet) {
            MonthlyRoutineBottomSheet(
                courseTitle = selectedCourse.title,
                onDismiss = { showMonthlyScheduleSheet = false }
            )
        }

        // Quick Profile Action Sheet (Screenshot 1: নোটিফিকেশন, প্রোফাইল এডিট, সিলেবাস পরিবর্তন, ভর্তি সম্পর্কিত তথ্য, ইত্যাদি)
        if (showProfileOptionsSheet) {
            ProfileOptionsSheet(
                onDismissRequest = { showProfileOptionsSheet = false },
                onEditProfileClick = onEditProfileClick,
                onSyllabusChangeClick = onSyllabusChangeClick,
                onAdmissionInfoClick = onAdmissionInfoClick,
                onSettingsClick = onProfileClick,
                onLoginClick = onLoginClick,
                onLogoutClick = { showLogoutDialog = true }
            )
        }
    }
}

/**
 * 1. Navy Blue Hero Header Section
 * Matches Screenshot:
 * - Top Bar: Bird shield badge (MaxBird/FAHIM) + Course Dropdown pill
 * - Left: 👑 প্রিমিয়াম badge, হ্যালো, FFMAX 👋, এইচএসসি - বিজ্ঞান - এইচএসসি ২০২৭ • GOJAP...
 * - Right: Profile Avatar with Gold ring, 👑 crown and 🔥 ১ streak badge
 * - Banner Card: "তোমার মজার কিছু ইংরেজি হ্যাকস! রিপোর্ট কার্ড রেডি!"
 */
@Composable
private fun NavyHeroHeaderSection(
    selectedCourse: EnrolledCourse,
    onCourseDropdownClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val userProfile = UserSessionManager.currentUserProfile

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavyHeaderGradient)
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 22.dp)
    ) {
        // App Bar Row: Bird Badge + Course Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Shield Badge (FAHIM / MaxBird)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1A44))
                    .border(1.dp, Color(0xFF4F46E5).copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🦅",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "FAHIM",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center: Interactive Course Dropdown Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .testTag("home_course_dropdown")
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onCourseDropdownClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCourse.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "কোর্স পরিবর্তন",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Profile & Greeting Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 👑 প্রিমিয়াম badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldCrownColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "প্রিমিয়াম",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title: শুভ দিন, {user.name} 👋
                Text(
                    text = "শুভ দিন, ${userProfile.name} 👋",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                if (userProfile.institutionName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🏫 ${userProfile.institutionName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF93C5FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle & Class Badge: {user.className}
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF334155).copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = UserSessionManager.currentUserProfile.studentClass.ifBlank { "এইচএসসি" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    if (UserSessionManager.currentUserProfile.group.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${UserSessionManager.currentUserProfile.group}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    if (UserSessionManager.currentUserProfile.examBatch.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${UserSessionManager.currentUserProfile.examBatch}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // Right: Profile Avatar with Gold ring, 👑 crown, and 🔥 streak badge (Clickable to open profile & settings)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick)
                    .testTag("home_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                // Modern UserAvatarView with CDN support + gradient letter fallback
                UserAvatarView(
                    avatarUrl = UserSessionManager.currentUserProfile.avatarUrl,
                    name = UserSessionManager.currentUserProfile.name,
                    size = 56.dp,
                    fontSize = 22.sp,
                    borderColor = GoldCrownColor,
                    borderWidth = 2.5.dp
                )

                // Crown 👑 badge on top right of avatar
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👑",
                        fontSize = 10.sp
                    )
                }

                // Streak flame badge 🔥 ১ at bottom
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "১",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Floating Banner Card (Report Card Alert)
        ReportCardBanner()
    }
}

/**
 * Report Card Alert Card (matches user's screenshot)
 */
@Composable
private fun ReportCardBanner() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFEEF4FF),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document Icon with A+ stamp
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "A+",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text Lines
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "তোমার মজার কিছু ইংরেজি হ্যাকস!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "রিপোর্ট কার্ড রেডি!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "চলো দেখি কেমন করলে!",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // Arrow Action Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBFDBFE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "দেখো",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Red badge '১' at top-right corner
        Box(
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-5).dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "১",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 2. Routine Header Bar
 * Left: "রুটিন"
 * Right: "সব দেখো" (Soft blue capsule button)
 */
@Composable
private fun RoutineHeaderBar(
    apiState: CourseContentUiState<*>? = null,
    onViewAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "রুটিন",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )

            if (apiState != null) {
                Spacer(modifier = Modifier.width(10.dp))
                when (apiState) {
                    is CourseContentUiState.Loading -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "সিঙ্ক হচ্ছে...",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    is CourseContentUiState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF16A34A))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "লাইভ API",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }
                    }
                    is CourseContentUiState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "অফলাইন মোড",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEEF4FF),
            modifier = Modifier
                .testTag("view_all_routine_button")
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onViewAllClick)
        ) {
            Text(
                text = "সব দেখো",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
            )
        }
    }
}

/**
 * 7-Day Day Selector Bar
 * Days: শনি, রবি, সোম, মঙ্গল, বুধ, বৃহ, শুক্র
 * Inactive: Soft gray background, Bengali numeral date, cyan dots/dash below
 * Active: Vibrant blue capsule (e.g. ৯ for Wednesday), white date, 2 cyan dots
 */
@Composable
private fun SevenDaySelectorScreenshot(
    days: List<DaySchedule>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        days.forEach { day ->
            val isSelected = day.dayIndex == selectedDayIndex

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .testTag("routine_day_${day.dayIndex}")
                    .width(44.dp)
            ) {
                // Day name above capsule: শনি, রবি, etc.
                Text(
                    text = day.dayShortBn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Vertical Capsule Pill
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(
                            if (isSelected) ActiveBluePill else LightPillBackground
                        )
                        .clickable { onDaySelected(day.dayIndex) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Date in Bengali numerals: ৫, ৬, ৭, ৮, ৯, ১০, ১১
                        Text(
                            text = day.dateText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF1E293B)
                        )

                        // Dots / Dash representation
                        DayDotIndicators(
                            dayIndex = day.dayIndex,
                            totalCount = day.totalCount,
                            isSelected = isSelected
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dots indicators under the date in the 7-day capsule
 * e.g. Day 0 (5): '-' dash
 *      Day 1, 2, 3: '...' 3 cyan dots
 *      Day 4 (9): '..' 2 cyan dots
 *      Day 5 (10): 5 cyan dots
 *      Day 6 (11): '.' 1 cyan dot
 */
@Composable
private fun DayDotIndicators(
    dayIndex: Int,
    totalCount: Int,
    isSelected: Boolean
) {
    val dotColor = if (isSelected) Color(0xFF38BDF8) else DotCyanColor

    when {
        totalCount == 0 -> {
            // Dash indicator
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFFCBD5E1))
            )
        }
        totalCount == 1 -> {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        totalCount == 2 -> {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
        totalCount == 3 -> {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
        else -> {
            // 5 dots in a mini cluster
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Date and Summary indicators
 * Left: বুধবার, ০৯/০৯/২০২৬
 * Right: ■ ক্লাস ২   ■ এক্সাম ০
 */
@Composable
private fun DateAndSummaryIndicators(
    daySchedule: DaySchedule
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date text
        Text(
            text = daySchedule.fullDateBn,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155)
        )

        // Class & Exam counters
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Class indicator: ■ ক্লাস ২
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DotCyanColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "ক্লাস ${daySchedule.classes.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }

            // Exam indicator: ■ এক্সাম ০
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFF59E0B))
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "এক্সাম ${daySchedule.exams.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

/**
 * Horizontal Carousel of Class Cards (Exact match with cards from screenshot)
 */
@Composable
private fun HorizontalClassesCarousel(
    daySchedule: DaySchedule,
    onClassClick: (RoutineClassItem) -> Unit = {}
) {
    if (daySchedule.classes.isEmpty() && daySchedule.exams.isEmpty()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF8FAFC),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFF1F5F9)))),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "☕", fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${daySchedule.dayNameBn}-এ কোনো নির্ধারিত ক্লাস বা পরীক্ষা নেই",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Text(
                    text = "রিভিশন এবং পরবর্তী দিনের প্রস্তুতির জন্য সময় নিন",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 6.dp)
        ) {
            items(daySchedule.classes) { classItem ->
                ScreenshotRoutineClassCard(
                    classItem = classItem,
                    onClick = { onClassClick(classItem) }
                )
            }
        }
    }
}

/**
 * Routine Class Card (Pixel-matching the screenshot card):
 * - Rounded white card with subtle blue/cyan outline
 * - Badges: [রসায়ন ২য় পত্র] in Cyan, [👨‍🏫 লেকচার ক্লাস] in Gray
 * - Title: পর্ব-১২: জৈব রসায়ন (নামকরণ + ...
 * - Timing: 07.00 PM - 08.32 PM • ১ ঘন্টা ৩২ মিনিট (Pink time)
 */
@Composable
private fun ScreenshotRoutineClassCard(
    classItem: RoutineClassItem,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFBAE6FD), Color(0xFFDDD6FE))
            )
        ),
        modifier = Modifier
            .width(290.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag("routine_card_${classItem.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Chips row: Subject pill + Type pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subject pill (e.g. রসায়ন ২য় পত্র in Cyan / Blue)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(classItem.subjectColorHex)
                ) {
                    Text(
                        text = classItem.subject,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Type pill (e.g. 👨‍🏫 লেকচার ক্লাস)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "👨‍🏫", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = classItem.typeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title: পর্ব-১২: জৈব রসায়ন (নামকরণ + ...
            Text(
                text = classItem.chapterOrTopic,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Timing row: 07.00 PM - 08.32 PM • ১ ঘন্টা ৩২ মিনিট
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = classItem.time,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PinkTimeColor
                )

                if (classItem.durationText.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = classItem.durationText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569)
                    )
                }
            }
        }
    }
}

/**
 * Course Switcher Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseSwitcherBottomSheet(
    courses: List<EnrolledCourse>,
    selectedCourse: EnrolledCourse,
    onCourseSelected: (EnrolledCourse) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "তোমার যুক্ত হওয়া কোর্সসমূহ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "কোর্স পরিবর্তন করতে একটি কোর্স সিলেক্ট করো",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "বন্ধ করুন",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            courses.forEach { course ->
                val isSelected = course.id == selectedCourse.id

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            if (isSelected) listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                            else listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onCourseSelected(course) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color(0xFF3B82F6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = course.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF1D4ED8) else Color(0xFF0F172A)
                                )
                                Text(
                                    text = "${course.badge} • ${course.completedClasses}/${course.totalClasses} ক্লাস সম্পন্ন",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Monthly Routine Bottom Sheet ("সব দেখো")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyRoutineBottomSheet(
    courseTitle: String,
    onDismiss: () -> Unit
) {
    val months = listOf("সেপ্টেম্বর ২০২৬", "অক্টোবর ২০২৬", "নভেম্বর ২০২৬")
    var selectedMonth by remember { mutableStateOf("সেপ্টেম্বর ২০২৬") }
    var filterType by remember { mutableStateOf("সব") }

    val allMonthlyItems = remember(courseTitle) {
        emptyList<MonthClassOverview>()
    }

    val filteredItems = remember(filterType, allMonthlyItems) {
        when (filterType) {
            "ক্লাস" -> allMonthlyItems.filter { !it.isExam }
            "পরীক্ষা" -> allMonthlyItems.filter { it.isExam }
            else -> allMonthlyItems
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "মাসিক ক্লাস ও পরীক্ষার রুটিন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = courseTitle,
                        fontSize = 12.sp,
                        color = Color(0xFF2563EB),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(months) { month ->
                    val isSelected = month == selectedMonth
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMonth = month },
                        label = { Text(month, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2563EB),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filter by: All, Classes, Exams
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("সব", "ক্লাস", "পরীক্ষা").forEach { f ->
                    val isSelected = filterType == f
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFEEF4FF) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { filterType = f }
                    ) {
                        Text(
                            text = f,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF2563EB) else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Month Classes & Exams
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems) { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (item.isExam) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (item.isExam) listOf(Color(0xFFFCA5A5), Color(0xFFFECACA))
                                else listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(60.dp)
                            ) {
                                Text(
                                    text = item.dateString,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isExam) Color(0xFFDC2626) else Color(0xFF2563EB)
                                )
                                Text(
                                    text = item.dayNameBn,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.subject,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = item.topic,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "সময়: ${item.time}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (item.isExam) Color(0xFFEF4444) else Color(0xFFEEF4FF)
                            ) {
                                Text(
                                    text = item.typeName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isExam) Color.White else Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
