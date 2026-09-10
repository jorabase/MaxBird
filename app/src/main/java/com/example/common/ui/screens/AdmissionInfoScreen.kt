package com.example.common.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.network.CourseContentUiState
import com.example.common.network.EnrollPaymentPlanItem
import com.example.common.viewmodel.EnrollmentPaymentViewModel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data representation for an enrolled course in the Admission Info Screen
 */
data class EnrolledCourseInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val bannerTag: String,
    val bannerGradient: List<Color>,
    val validityDate: String,
    val isExpiringSoon: Boolean = false,
    val quarters: List<String> = emptyList(),
    val isFullCourse: Boolean = false
)

/**
 * Modern, High-Craft "কোর্সে ভর্তি" / "ভর্তি সম্পর্কিত তথ্য" Screen.
 * Faithful to Screenshots 2, 3, and 4:
 * - Clean White & Neutral background
 * - Top bar with Back button and title "কোর্সে ভর্তি"
 * - Expandable course cards displaying:
 *   - Thumbnail with distinct program styling
 *   - Course title with expansion chevron
 *   - Quarters / Full course list with [ভর্তি হয়েছো] unlocked badge
 *   - Validity date (ভর্তির মেয়াদ) in green or warm red
 */
@Composable
fun AdmissionInfoScreen(
    onBackClick: () -> Unit,
    onNavigateToQuarter: (String) -> Unit = {},
    viewModel: EnrollmentPaymentViewModel = remember { EnrollmentPaymentViewModel() },
    modifier: Modifier = Modifier
) {
    val paymentState by viewModel.paymentState.collectAsStateWithLifecycle()
    val phasesState by viewModel.phasesState.collectAsStateWithLifecycle()

    val liveQuarters = if (phasesState.isNotEmpty()) {
        phasesState.map { it.title }
    } else {
        listOf(
            "কোয়ার্টার ১",
            "কোয়ার্টার ২",
            "কোয়ার্টার ৩",
            "কোয়ার্টার ৪",
            "কোয়ার্টার ৫"
        )
    }

    val enrolledCourses = remember(phasesState) {
        listOf(
            EnrolledCourseInfo(
                id = "hsc27_science",
                title = "HSC '27",
                subtitle = "বিজ্ঞান - ২য় ...",
                bannerTag = "ACADEMIC PROGRAM\nHSC 27 CLASS 11\nScience",
                bannerGradient = listOf(Color(0xFF881337), Color(0xFF4C0519)),
                validityDate = "০১ সেপ্টেম্বর, ২০২৬",
                isExpiringSoon = false,
                quarters = liveQuarters
            ),
            EnrolledCourseInfo(
                id = "hsc27_ict",
                title = "HSC '27",
                subtitle = "অ্যাডভান্সড ICT",
                bannerTag = "HSC 27\nADVANCED\nICT",
                bannerGradient = listOf(Color(0xFF0284C7), Color(0xFF0F172A)),
                validityDate = "১৫ সেপ্টেম্বর, ২০২৫",
                isExpiringSoon = true,
                isFullCourse = true
            ),
            EnrolledCourseInfo(
                id = "hsc27_humanities",
                title = "HSC '27",
                subtitle = "মানবিক - ২য় ...",
                bannerTag = "ACADEMIC PROGRAM\nHSC 27 CLASS 11\nHumanities",
                bannerGradient = listOf(Color(0xFF831843), Color(0xFF500724)),
                validityDate = "২৩ সেপ্টেম্বর, ২০২৫",
                isExpiringSoon = false,
                quarters = listOf(
                    "কোয়ার্টার ১",
                    "কোয়ার্টার ২",
                    "কোয়ার্টার ৩"
                )
            ),
            EnrolledCourseInfo(
                id = "duranta_hsc27",
                title = "দুরন্ত HSC '27",
                subtitle = "- মানবিক",
                bannerTag = "দুরন্ত\nHSC 27\nHumanities",
                bannerGradient = listOf(Color(0xFF9D174D), Color(0xFF4C0519)),
                validityDate = "১৫ সেপ্টেম্বর, ২০২৫",
                isExpiringSoon = true,
                isFullCourse = true
            ),
            EnrolledCourseInfo(
                id = "hsc27_business",
                title = "HSC '27",
                subtitle = "ব্যবসায় শি...",
                bannerTag = "ACADEMIC PROGRAM\nHSC 27 CLASS 11\nBusiness Studies",
                bannerGradient = listOf(Color(0xFF881337), Color(0xFF3F0B1B)),
                validityDate = "০৮ ডিসেম্বর, ২০২৫",
                isExpiringSoon = false,
                quarters = listOf(
                    "কোয়ার্টার ১",
                    "কোয়ার্টার ২"
                )
            ),
            EnrolledCourseInfo(
                id = "think_ai",
                title = "Think AI",
                subtitle = "Supported by Meta",
                bannerTag = "Think AI\nMeta AI Lab",
                bannerGradient = listOf(Color(0xFF1E3A8A), Color(0xFF0B132B)),
                validityDate = "৩০ এপ্রিল, ২০২৯",
                isExpiringSoon = false,
                isFullCourse = true
            )
        )
    }

    // Expansion states (first course expanded by default as in Screenshot 2)
    val expandedStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            put("hsc27_science", true)
            put("hsc27_ict", true)
        }
    }

    val livePaymentPlans = (paymentState as? CourseContentUiState.Success)?.data

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("admission_info_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color(0xFF1E293B)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "কোর্সে ভর্তি",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(enrolledCourses, key = { it.id }) { course ->
                val isExpanded = expandedStates[course.id] ?: false
                EnrolledCourseCard(
                    course = course,
                    isExpanded = isExpanded,
                    paymentPlans = if (course.id == "hsc27_science") livePaymentPlans else null,
                    onToggleExpand = {
                        expandedStates[course.id] = !isExpanded
                    },
                    onQuarterClick = { quarterTitle ->
                        onNavigateToQuarter("${course.title} $quarterTitle")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

/**
 * Individual course card matching Screenshot 2, 3, and 4
 */
@Composable
private fun EnrolledCourseCard(
    course: EnrolledCourseInfo,
    isExpanded: Boolean,
    paymentPlans: List<EnrollPaymentPlanItem>? = null,
    onToggleExpand: () -> Unit,
    onQuarterClick: (String) -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "expand_chevron_rotation"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF64748B).copy(alpha = 0.08f))
            .testTag("enrolled_course_card_${course.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Thumbnail + Title + Expansion Chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Course Banner Thumbnail
                    Box(
                        modifier = Modifier
                            .size(width = 100.dp, height = 58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(course.bannerGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = course.bannerTag,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.92f),
                            lineHeight = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & Subtitle
                    Column {
                        Text(
                            text = course.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = course.subtitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                }

                // Chevron icon with rotation animation
                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "সংকোচন করো" else "প্রসারিত করো",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            // Expanded content: Quarters / Full Course List & Installments
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    // Dashed / Dotted divider
                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    if (course.isFullCourse) {
                        // Single "ফুল কোর্স" item (Screenshot 3)
                        QuarterRowItem(
                            title = "ফুল কোর্স",
                            onClick = { onQuarterClick("ফুল কোর্স") }
                        )
                    } else {
                        // Multi-quarter items (Screenshot 2: কোয়ার্টার ১, ২, ৩, ৪, ৫)
                        course.quarters.forEach { quarterName ->
                            QuarterRowItem(
                                title = quarterName,
                                onClick = { onQuarterClick(quarterName) }
                            )
                        }
                    }

                    // Installment Details (ListEnrollPaymentPlan)
                    if (paymentPlans != null && paymentPlans.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "কিস্তির বিবরণী (Installment Plans)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        paymentPlans.forEach { plan ->
                            InstallmentRowItem(plan)
                        }
                    }
                }
            }

            // Bottom Divider before Validity
            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Validity Date Row (ভর্তির মেয়াদ)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ভর্তির মেয়াদ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )

                Text(
                    text = course.validityDate,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (course.isExpiringSoon) Color(0xFFE11D48) else Color(0xFF059669)
                )
            }
        }
    }
}

/**
 * Single installment row display
 */
@Composable
private fun InstallmentRowItem(plan: EnrollPaymentPlanItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = plan.titleBn,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = plan.paymentDate ?: plan.dueDate ?: "পরিশোধ সম্পন্ন",
                    fontSize = 11.5.sp,
                    color = Color(0xFF64748B)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val amount = plan.pricePlan?.amount?.toInt() ?: 4000
                Text(
                    text = "৳$amount",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (plan.isPaid) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = if (plan.isPaid) "✓ পরিশোধিত" else "বকেয়া",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (plan.isPaid) Color(0xFF16A34A) else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual Quarter / Full course item matching Screenshot 2 & 3:
 * e.g. "কোয়ার্টার ১" -> [ 🔓 ভর্তি হয়েছো ] >
 */
@Composable
private fun QuarterRowItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quarter Title (e.g. কোয়ার্টার ১ / ফুল কোর্স)
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Unlocked Green Badge: [ 🔓 ভর্তি হয়েছো ]
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF16A34A), // Emerald green
                modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(20.dp), spotColor = Color(0xFF16A34A).copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "ভর্তি হয়েছো",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Arrow Right chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
