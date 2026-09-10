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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.network.AcademicProgramFilterParams
import com.example.common.network.AcademicProgramItem
import com.example.common.network.AcademicProgramsCatalog
import com.example.common.network.AnalyticsTracker
import com.example.common.network.CourseContentUiState
import com.example.common.network.UserSessionManager
import com.example.common.viewmodel.CourseViewModel

/**
 * Screen matching the technical blueprint for the "কোর্স" (Courses) tab:
 * 1. Filtered strictly via user's JWT metadata "C11,HUM" (Class 11, Humanities).
 * 2. Status determination based on API data:
 *    - HSC '27 মানবিক - ২য় বর্ষ প্রস্তুতি (FullApTrial, expired 2025-12-11) -> "ফ্রিতে শেখা শেষ", "বিস্তারিত দেখো"
 *    - Think AI (Paid, active till 2029) -> "ভর্তি হয়েছো", "শেখা চালিয়ে যাও"
 *    - The Next Champ – HSC ‘27 (is_free: true) -> "সম্পূর্ণ ফ্রি!", "সম্পূর্ণ ফ্রি'তে শুরু করো"
 *    - অন্যান্য পেইড কোর্সসমূহ -> "বিস্তারিত দেখো"
 * 3. Exact 3-section division:
 *    - আমার কোর্স (My Courses)
 *    - ফ্রি কোর্স (Free Courses)
 *    - সকল কোর্স (All Courses) (Filtered to exclude blacklisted programs to avoid duplicates)
 * 4. Background data loading & activity tracking telemetry dispatched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    viewModel: CourseViewModel = remember { CourseViewModel() },
    onCourseClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentProfile = UserSessionManager.currentUserProfile
    val currentParams = remember(currentProfile.studentClass, currentProfile.examBatch, currentProfile.group) {
        CourseViewModel.currentFilterParamsFromProfile()
    }

    LaunchedEffect(currentParams) {
        viewModel.loadAcademicPrograms(currentParams)
    }

    val programsState by viewModel.academicProgramsState.collectAsState()

    // Selected program for detail bottom sheet
    var selectedProgramForDetail by remember { mutableStateOf<AcademicProgramItem?>(null) }
    var showFreeEnrollSuccessDialog by remember { mutableStateOf<AcademicProgramItem?>(null) }
    var showTrialSuccessDialog by remember { mutableStateOf<AcademicProgramItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFD))
            .statusBarsPadding()
    ) {
        // Top App Bar
        TopCourseBar(
            batchLabel = currentParams.batchId,
            onBackClick = onBackClick,
            onRefresh = { viewModel.loadAcademicPrograms(currentParams) }
        )

        // Metadata & Cloud Filter Banner (e.g. "C11,HUM" -> HSC 2028 Humanities)
        CloudFilterBanner(
            profile = currentProfile,
            params = currentParams
        )

        // "৩ দিন সবকিছু ফ্রি!" Dynamic Trial Banner (Conditioned on Class Eligibility)
        if (com.example.common.network.TrialManager.isClassEligible(currentProfile.studentClass)) {
            val hasActiveTrial = (programsState as? CourseContentUiState.Success)?.data?.enrolledPrograms?.any { it.isTrialActive } == true
            DynamicTrialHeaderBanner(
                profile = currentProfile,
                hasActiveTrial = hasActiveTrial
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content Body
        when (val state = programsState) {
            is CourseContentUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1E3A8A),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(38.dp)
                        )
                        Text(
                            text = "HSC মানবিক সিলেবাস ও ট্রায়াল সিঙ্ক হচ্ছে...",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = state.message,
                            fontSize = 14.sp,
                            color = Color(0xFFDC2626),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadAcademicPrograms() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A))
                        ) {
                            Text("পুনরায় চেষ্টা করুন")
                        }
                    }
                }
            }

            is CourseContentUiState.Success -> {
                val catalog = state.data
                CourseCatalogList(
                    catalog = catalog,
                    onProgramAction = { program ->
                        when {
                            program.trialEnabled && !program.hasEnrolment -> {
                                // "৩ দিন ফ্রিতে শেখো" CTA Clicked -> Trigger Trial Activation
                                viewModel.activateTrialForCourse(program) {
                                    showTrialSuccessDialog = program
                                }
                            }
                            program.isTrialExpired -> {
                                // "বিস্তারিত দেখো" on expired trial course opens Phase Validation & Purchase Sheet
                                selectedProgramForDetail = program
                            }
                            program.isEnrolled -> {
                                // "শেখা চালিয়ে যাও"
                                onCourseClick(program.title)
                            }
                            program.isFree -> {
                                // "সম্পূর্ণ ফ্রি'তে শুরু করো"
                                viewModel.enrollInFreeCourse(program)
                                showFreeEnrollSuccessDialog = program
                            }
                            else -> {
                                // "বিস্তারিত দেখো" on other paid programs
                                selectedProgramForDetail = program
                            }
                        }
                    }
                )
            }
        }
    }

    // Modal Bottom Sheet for Course Detail & Purchase Option
    if (selectedProgramForDetail != null) {
        val program = selectedProgramForDetail!!
        ModalBottomSheet(
            onDismissRequest = { selectedProgramForDetail = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            CourseDetailBottomSheetContent(
                program = program,
                onDismiss = { selectedProgramForDetail = null },
                onPurchaseConfirm = {
                    viewModel.purchaseCourse(program)
                    selectedProgramForDetail = null
                },
                onToggleTrialSimulation = {
                    if (program.isTrialExpired) {
                        viewModel.activateTrialForCourse(program) {}
                    } else {
                        viewModel.expireTrialForCourse(program.id)
                    }
                    selectedProgramForDetail = null
                },
                onNavigateToRoutine = {
                    selectedProgramForDetail = null
                    onCourseClick(program.title)
                }
            )
        }
    }

    // Success Dialog for Free Course Enrollment
    if (showFreeEnrollSuccessDialog != null) {
        val course = showFreeEnrollSuccessDialog!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFreeEnrollSuccessDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(42.dp)
                )
            },
            title = {
                Text(
                    text = "অভিনন্দন, ফাহিম মিয়া!",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            },
            text = {
                Text(
                    text = "\"${course.title}\"-এ আপনার ফ্রি এনরোলমেন্ট সফলভাবে সম্পন্ন হয়েছে। কোর্সটি এখন 'আমার কোর্স' সেকশনে সক্রিয় রয়েছে।",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showFreeEnrollSuccessDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Success Celebration Dialog for 3-Day Free Trial Activation
    if (showTrialSuccessDialog != null) {
        val course = showTrialSuccessDialog!!
        AlertDialog(
            onDismissRequest = { showTrialSuccessDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = Color(0xFFDB2777),
                    modifier = Modifier.size(46.dp)
                )
            },
            title = {
                Text(
                    text = "৩ দিন সবকিছু ফ্রি সক্রিয় হয়েছে! 🎉",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "অভিনন্দন, ${currentProfile.name}! \"${course.title}\"-এ আগামী ৭২ ঘণ্টার জন্য আপনার ফ্রি ট্রায়াল সক্রিয় হয়েছে।",
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        lineHeight = 19.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFDF2F8),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFBCFE8))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFDB2777),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "৭২ ঘণ্টা ফুল অ্যাপ এক্সেস উন্মুক্ত",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9D174D)
                                )
                            }
                            Text(
                                text = "• সকল লাইভ ক্লাস ও পূর্ণাঙ্গ সিলেবাস\n• বিগত চ্যাপ্টারের রেকর্ডেড লেকচার\n• ডিজিটাল নোট ও বোর্ড প্রশ্ন সল্যুশন",
                                fontSize = 12.sp,
                                color = Color(0xFF831843),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val title = course.title
                        showTrialSuccessDialog = null
                        onCourseClick(title)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
                ) {
                    Text("ক্লাস শুরু করুন", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Dynamic Trial Header Banner:
 * "৩ দিন সবকিছু ফ্রি!" এবং "৩ দিন ফ্রিতে শেখো"
 * Server configuration, Class Eligibility ও Enrollment State Machine অনুযায়ী ডাইনামিক
 */
@Composable
private fun DynamicTrialHeaderBanner(
    profile: com.example.common.model.UserProfile,
    hasActiveTrial: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFDF2F8), // Soft Rose
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFBCFE8))
        ),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFFEC4899), Color(0xFFDB2777))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "৩ দিন সবকিছু ফ্রি!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF9D174D)
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (hasActiveTrial) Color(0xFF059669) else Color(0xFFBE185D)
                        ) {
                            Text(
                                text = if (hasActiveTrial) "সক্রিয় ট্রায়াল" else "৭২ ঘণ্টা",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = if (hasActiveTrial) "আপনার ৩ দিনের ট্রায়াল চলছে! সকল ক্লাস ও লেকচার উন্মুক্ত।"
                        else "কোনো পেমেন্ট ছাড়াই ৩ দিন প্রিমিয়াম ক্লাস ও লেকচার শিট উপভোগ করো",
                        fontSize = 11.sp,
                        color = Color(0xFF831843),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar with back button, title, and live sync indicator
 */
@Composable
private fun TopCourseBar(
    batchLabel: String,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag("courses_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "ফিরে যান",
                tint = Color(0xFF0F172A)
            )
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "কোর্স",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0E7FF)
            ) {
                Text(
                    text = batchLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3730A3),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "রিফ্রেশ",
                tint = Color(0xFF475569)
            )
        }
    }
}

/**
 * Visual indicator showing the active cloud filtering parameters
 */
@Composable
private fun CloudFilterBanner(
    profile: com.example.common.model.UserProfile,
    params: AcademicProgramFilterParams
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEEF2FF),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC7D2FE))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = null,
                    tint = Color(0xFF4338CA),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "সিলেবাস: ${profile.studentClass} • ${profile.group} (${params.className}, ${params.group.take(3).uppercase()})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3730A3)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(Color(0xFF10B981), CircleShape)
                )
                Text(
                    text = "API কানেক্টেড",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857)
                )
            }
        }
    }
}

/**
 * 3-Section Course Catalog List:
 * 1. আমার কোর্স (My Courses)
 * 2. ফ্রি কোর্স (Free Courses)
 * 3. সকল কোর্স (All Courses) (Filtered without blacklisted programs)
 */
@Composable
private fun CourseCatalogList(
    catalog: AcademicProgramsCatalog,
    onProgramAction: (AcademicProgramItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // ----------------------------------------------------
        // সেকশন ১: আমার কোর্স (My Courses)
        // ----------------------------------------------------
        item {
            SectionHeader(
                title = "আমার কোর্স",
                count = catalog.enrolledPrograms.size,
                badgeColor = Color(0xFF1E3A8A)
            )
        }

        items(catalog.enrolledPrograms, key = { it.id }) { program ->
            MyCourseCard(
                program = program,
                onActionClick = { onProgramAction(program) }
            )
        }

        // ----------------------------------------------------
        // সেকশন ২: ফ্রি কোর্স (Free Courses)
        // ----------------------------------------------------
        if (catalog.freePrograms.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "ফ্রি কোর্স",
                    count = catalog.freePrograms.size,
                    badgeColor = Color(0xFF059669),
                    tagText = "সম্পূর্ণ ফ্রি!"
                )
            }

            items(catalog.freePrograms, key = { it.id }) { program ->
                FreeCourseCard(
                    program = program,
                    onActionClick = { onProgramAction(program) }
                )
            }
        }

        // ----------------------------------------------------
        // সেকশন ৩: সকল কোর্স (All Courses)
        // ----------------------------------------------------
        if (catalog.allCoursesPrograms.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "সকল কোর্স",
                    count = catalog.allCoursesPrograms.size,
                    badgeColor = Color(0xFF475569),
                    tagText = "মানবিক ক্যাটালগ"
                )
            }

            items(catalog.allCoursesPrograms, key = { it.id }) { program ->
                AllCourseCatalogCard(
                    program = program,
                    onActionClick = { onProgramAction(program) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    badgeColor: Color,
    tagText: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A)
            )
            Surface(
                shape = CircleShape,
                color = badgeColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = "$count",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        if (tagText != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFDCFCE7)
            ) {
                Text(
                    text = tagText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Card for "আমার কোর্স" (My Courses)
 * Distinctly renders:
 * 1. "HSC '27 মানবিক - ২য় বর্ষ প্রস্তুতি": ট্যাগ "ফ্রিতে শেখা শেষ", বাটন "বিস্তারিত দেখো"
 * 2. "Think AI": ট্যাগ "ভর্তি হয়েছো", বাটন "শেখা চালিয়ে যাও"
 */
@Composable
private fun MyCourseCard(
    program: AcademicProgramItem,
    onActionClick: () -> Unit
) {
    val gradientColors = listOf(Color(program.colorPrimaryHex), Color(program.colorSecondaryHex))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("my_course_card_${program.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row: Status Badge + Expiry/State Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Badge
                    val badgeBgColor = when {
                        program.isTrialExpired -> Color(0xFFFEE2E2)
                        program.isTrialActive -> Color(0xFFFDF2F8)
                        else -> Color(0xFFDCFCE7)
                    }
                    val badgeTextColor = when {
                        program.isTrialExpired -> Color(0xFFB91C1C)
                        program.isTrialActive -> Color(0xFFBE185D)
                        else -> Color(0xFF15803D)
                    }
                    val badgeIcon = when {
                        program.isTrialExpired -> Icons.Default.LockClock
                        program.isTrialActive -> Icons.Default.Bolt
                        else -> Icons.Default.CheckCircle
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBgColor
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = program.tagText, // "ফ্রিতে শেখা শেষ", "৩ দিন ফ্রি ট্রায়াল সক্রিয়", বা "ভর্তি হয়েছো"
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                    }

                    // Program Category Tag
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = program.programTag,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                // Middle: Course Title & Category/Batch
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = program.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )
                    Text(
                        text = program.classTag,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF93C5FD)
                    )
                }

                // Notice Box for Active Trial, Expired Trial or Active Subscription
                if (program.isTrialActive) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.55f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF472B6).copy(alpha = 0.6f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFF472B6),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "🎉 ৩ দিন সবকিছু ফ্রি চলছে! আগামী ৭২ ঘণ্টা সকল প্রিমিয়াম লাইভ ক্লাস ও হ্যান্ডনোট আনলক রয়েছে।",
                                fontSize = 11.sp,
                                color = Color(0xFFFCE7F3),
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else if (program.isTrialExpired) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444).copy(alpha = 0.4f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️ ট্রায়ালের মেয়াদ ১১ ডিসেম্বর ২০২৫-এ শেষ হয়েছে। কোর্সটি কিনতে বা বিস্তারিত সিলেবাস দেখতে নিচের বাটনে চাপুন।",
                                fontSize = 11.sp,
                                color = Color(0xFFFECACA),
                                lineHeight = 16.sp
                            )
                        }
                    }
                } else if (program.expiryDate != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "মেয়াদ: ৩০ এপ্রিল ২০২৯ পর্যন্ত সক্রিয় অ্যাক্সেস",
                            fontSize = 11.sp,
                            color = Color(0xFFBBF7D0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Action Button: "বিস্তারিত দেখো" or "শেখা চালিয়ে যাও"
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (program.isTrialExpired) Color(0xFFFCA5A5) else Color.White
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (program.isTrialExpired) Color(0xFFEF4444).copy(alpha = 0.2f) else Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("action_button_${program.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = program.buttonText, // "বিস্তারিত দেখো" or "শেখা চালিয়ে যাও"
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card for "ফ্রি কোর্স" (Free Courses)
 * Renders "The Next Champ – HSC ‘27" with "সম্পূর্ণ ফ্রি!" tag & "সম্পূর্ণ ফ্রি'তে শুরু করো" button
 */
@Composable
private fun FreeCourseCard(
    program: AcademicProgramItem,
    onActionClick: () -> Unit
) {
    val gradientColors = listOf(Color(program.colorPrimaryHex), Color(program.colorSecondaryHex))

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .testTag("free_course_card_${program.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF08A)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF854D0E),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "সম্পূর্ণ ফ্রি!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF854D0E)
                            )
                        }
                    }

                    // Price Tag: ৳২,৫০০ -> ০ টাকা
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "৳২,৫০০",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "০ টাকা",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFDE047)
                        )
                    }
                }

                // Title & Subtitle
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = program.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "এইচএসসি ২০২৭ মেগা স্কলারশিপ ও স্পেশাল চ্যাম্পিয়নশিপ ব্যাচ",
                        fontSize = 12.sp,
                        color = Color(0xFFBAE6FD)
                    )
                }

                // Bullet features
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    program.features.take(2).forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = feat,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Button: "সম্পূর্ণ ফ্রি'তে শুরু করো"
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("free_enroll_button")
                ) {
                    Text(
                        text = "সম্পূর্ণ ফ্রি'তে শুরু করো",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Card for "সকল কোর্স" (All Courses)
 * Clean cards for non-free, non-blacklisted HSC '27 Humanities courses
 */
@Composable
private fun AllCourseCatalogCard(
    program: AcademicProgramItem,
    onActionClick: () -> Unit
) {
    val isEligibleForTrial = program.trialEnabled && !program.hasEnrolment &&
            com.example.common.network.TrialManager.isClassEligible(UserSessionManager.currentUserProfile.studentClass)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isEligibleForTrial) Color(0xFFFBCFE8) else Color(0xFFE2E8F0)
            )
        ),
        shadowElevation = if (isEligibleForTrial) 2.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("all_course_card_${program.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Category Badge + Price Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEligibleForTrial) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFDF2F8),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFBCFE8))
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFDB2777),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "৩ দিন সবকিছু ফ্রি!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBE185D)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = program.badge.ifEmpty { "মানবিক স্পেশাল" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isEligibleForTrial) Color(0xFFFDF2F8) else Color(0xFFEFF6FF)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        if (isEligibleForTrial) {
                            Text(
                                text = "৳%,d".format(program.phasePricing),
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = "৩ দিন ০ টাকা",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFBE185D)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "৳%,d".format(program.phasePricing),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }
                }
            }

            // Title
            Text(
                text = program.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                lineHeight = 22.sp
            )

            // Features preview
            if (program.features.isNotEmpty()) {
                Text(
                    text = program.features.first(),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }

            // Action Button
            if (isEligibleForTrial) {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .testTag("trial_enroll_button_${program.id}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "৩ দিন ফ্রিতে শেখো",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(10.dp),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Text(
                        text = "বিস্তারিত দেখো",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A8A)
                    )
                }
            }
        }
    }
}

/**
 * Bottom Sheet Content for Phase Validation & Course Purchase
 * Corresponds to ProgramPhasesByStudent & GetStudentSpecificLessons with access_level: ReadOnly
 */
@Composable
private fun CourseDetailBottomSheetContent(
    program: AcademicProgramItem,
    onDismiss: () -> Unit,
    onPurchaseConfirm: () -> Unit,
    onToggleTrialSimulation: () -> Unit,
    onNavigateToRoutine: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (program.isTrialExpired) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
            ) {
                Text(
                    text = program.tagText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (program.isTrialExpired) Color(0xFFB91C1C) else Color(0xFF15803D),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "ID: ${program.id.take(12)}...",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // Title & Price
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = program.title,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "এইচএসসি ২০২৭ • মানবিক বিভাগ (C11, HUM)",
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )
            if (program.phasePricing > 0) {
                Text(
                    text = "কোর্স ফি: ৳%,d (এককালীন / কিস্তিতে প্রদেয়)".format(program.phasePricing),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFF1F5F9))

        // Expired Trial Warning Notice & Program Phases (Quarters 1-5 validation)
        if (program.isTrialExpired) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF2F2),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFECACA))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "অ্যাক্সেস লেভেল: ReadOnly (সীমাবদ্ধ)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "আপনার ৩ দিনের ফ্রি ট্রায়ালের মেয়াদ শেষ হয়েছে। সম্পূর্ণ লাইভ ক্লাস ও পরীক্ষা আনলক করতে নিচে ভর্তি সম্পন্ন করুন।",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Text(
                text = "কোয়ার্টারভিত্তিক সিলেবাস স্ট্যাটাস (ProgramPhasesByStudent):",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Triple("কোয়ার্টার ১", "অক্টোবর'২৫ - ডিসেম্বর'২৫", "পড়ানো শেষ"),
                    Triple("কোয়ার্টার ২", "জানুয়ারি'২৬ - মার্চ'২৬", "পড়ানো শেষ"),
                    Triple("কোয়ার্টার ৩", "এপ্রিল'২৬ - জুন'২৬", "পড়ানো শেষ"),
                    Triple("কোয়ার্টার ৪", "জুলাই'২৬ - সেপ্টেম্বর'২৬", "চলতি কোয়ার্টার"),
                    Triple("কোয়ার্টার ৫", "অক্টোবর'২৬ - ডিসেম্বর'২৬", "আসন্ন")
                ).forEach { (qName, qDuration, qStatus) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = qName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text(text = qDuration, fontSize = 10.sp, color = Color(0xFF64748B))
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (qStatus == "চলতি কোয়ার্টার") Color(0xFFDBEAFE) else Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = qStatus,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (qStatus == "চলতি কোয়ার্টার") Color(0xFF1D4ED8) else Color(0xFF475569),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // Features
            Text(
                text = "কোর্সের প্রধান বৈশিষ্ট্যসমূহ:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                program.features.forEach { feat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = feat,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPurchaseConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "ভর্তি নিশ্চিত করুন (${if (program.phasePricing > 0) "৳%,d".format(program.phasePricing) else "ফ্রি"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onNavigateToRoutine,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "সিলেবাস ও রুটিন দেখুন (ReadOnly মোড)",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            }

            OutlinedButton(
                onClick = onToggleTrialSimulation,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = if (program.isTrialExpired) "🔄 ট্রায়াল পুনরায় সক্রিয় করুন (টেস্টিং)" else "⏳ ট্রায়াল এক্সপায়ার করুন (টেস্টিং)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
