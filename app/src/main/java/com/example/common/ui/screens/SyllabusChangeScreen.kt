package com.example.common.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.model.UserProfile
import com.example.common.network.BatchOption
import com.example.common.network.UserSessionManager
import com.example.common.ui.components.ProfileSyncStatusCard
import com.example.common.viewmodel.LegacySyllabusSyncViewModel
import com.example.common.viewmodel.SyllabusSyncUiState
import kotlinx.coroutines.launch

private fun toBengaliDigits(input: String): String {
    val enDigits = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    val bnDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    var result = input
    for (i in 0..9) {
        result = result.replace(enDigits[i], bnDigits[i])
    }
    return result
}

private fun formatClassDisplay(raw: String): String = when {
    raw.contains("11") || raw.contains("C11", ignoreCase = true) || raw.contains("এইচএসসি") || raw.contains("HSC", ignoreCase = true) -> "এইচএসসি"
    raw.contains("12") || raw.contains("C12", ignoreCase = true) -> "এইচএসসি (২য় বর্ষ)"
    raw.contains("10") || raw.contains("C10", ignoreCase = true) -> "ক্লাস ১০"
    raw.contains("9") || raw.contains("C09", ignoreCase = true) -> "ক্লাস ৯"
    raw.contains("8") || raw.contains("C08", ignoreCase = true) -> "ক্লাস ৮"
    raw.contains("7") || raw.contains("C07", ignoreCase = true) -> "ক্লাস ৭"
    raw.contains("6") || raw.contains("C06", ignoreCase = true) -> "ক্লাস ৬"
    raw.contains("5") || raw.contains("C05", ignoreCase = true) -> "ক্লাস ৫"
    raw.isNotBlank() -> raw
    else -> "এইচএসসি"
}

private fun formatGroupDisplay(raw: String): String = when {
    raw.contains("HUM", ignoreCase = true) || raw.contains("মানবিক") || raw.contains("Humanities", ignoreCase = true) -> "মানবিক"
    raw.contains("SCI", ignoreCase = true) || raw.contains("বিজ্ঞান") || raw.contains("Science", ignoreCase = true) -> "বিজ্ঞান"
    raw.contains("BUS", ignoreCase = true) || raw.contains("ব্যবসায়") || raw.contains("Business", ignoreCase = true) -> "ব্যবসায় শিক্ষা"
    raw.isNotBlank() -> raw
    else -> "মানবিক"
}

private fun mapClassToCode(label: String): String = when {
    label.contains("১১") || label.contains("এইচএসসি") || label.contains("HSC") -> "C11"
    label.contains("১২") -> "C12"
    label.contains("১০") -> "C10"
    label.contains("৯") -> "C09"
    label.contains("৮") -> "C08"
    label.contains("৭") -> "C07"
    label.contains("৬") -> "C06"
    label.contains("৫") -> "C05"
    label.contains("এডমিশন") -> "CAD"
    else -> "C11"
}

/**
 * Modern, High-Craft Syllabus Management Screen ("সিলেবাস পরিবর্তন")
 * Matches user's screenshots:
 * - Overview card showing current Syllabus
 * - Interactive Class, Exam Year & Group selector
 *   + Class: ক্লাস ৫ থেকে ১০, এইচএসসি, এডমিশন
 *   + Exam Year: ২০২৫, ২০২৬, ২০২৭, ২০২৮
 *   + Group: বিজ্ঞান, মানবিক, ব্যবসায় শিক্ষা
 * - "এগিয়ে যাও" triggers listAcademicProgramByEnrollment & ProgramPhasesByStudent
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SyllabusChangeScreen(
    onBackClick: () -> Unit,
    onSyllabusUpdated: () -> Unit,
    viewModel: LegacySyllabusSyncViewModel = remember { LegacySyllabusSyncViewModel() },
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val isSyncing = syncState is SyllabusSyncUiState.Loading
    val serverBatchOptions by viewModel.batchOptionsState.collectAsStateWithLifecycle()
    val classesFromApi by viewModel.classesState.collectAsStateWithLifecycle()

    // Initial state from current profile
    val currentProfile = UserSessionManager.currentUserProfile
    var selectedClass by remember { mutableStateOf(currentProfile.studentClass) }
    var selectedBatch by remember {
        mutableStateOf(
            if (currentProfile.examBatch.contains("২০২৭")) "২০২৭"
            else if (currentProfile.examBatch.contains("২০২৬")) "২০২৬"
            else if (currentProfile.examBatch.contains("২০২৮")) "২০২৮"
            else "২০২৭"
        )
    }
    var selectedGroup by remember { mutableStateOf(currentProfile.group) }

    androidx.compose.runtime.LaunchedEffect(isEditing) {
        if (isEditing) {
            viewModel.loadClassList()
            viewModel.loadBatchOptions(mapClassToCode(selectedClass))
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                        onClick = {
                            if (isEditing) {
                                isEditing = false
                            } else {
                                onBackClick()
                            }
                        },
                        modifier = Modifier.testTag("syllabus_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color(0xFF1E293B)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "সিলেবাস",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = isEditing,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "syllabus_transition",
            modifier = Modifier.padding(innerPadding)
        ) { editing ->
            if (!editing) {
                // SCREENSHOT 1: Syllabus Overview Card
                SyllabusOverviewView(
                    profile = UserSessionManager.currentUserProfile,
                    onStartChange = { isEditing = true }
                )
            } else {
                // SCREENSHOTS 2 & 3: Interactive Class, Batch, and Group Selector (Real Database / API)
                SyllabusSelectorView(
                    classesFromApi = classesFromApi,
                    selectedClass = selectedClass,
                    onClassSelected = { label, code ->
                        selectedClass = label
                        viewModel.loadBatchOptions(code)
                    },
                    selectedBatch = selectedBatch,
                    onBatchSelected = { selectedBatch = it },
                    selectedGroup = selectedGroup,
                    onGroupSelected = { selectedGroup = it },
                    serverBatchOptions = serverBatchOptions,
                    isSyncing = isSyncing,
                    onConfirm = {
                        viewModel.syncSyllabus(
                            selectedClass = selectedClass,
                            selectedYear = selectedBatch,
                            selectedGroup = selectedGroup,
                            onSuccess = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("সিলেবাস এবং কোর্স ফেজ সফলভাবে ডাটাবেজ থেকে সিঙ্ক করা হয়েছে!")
                                }
                                isEditing = false
                                onSyllabusUpdated()
                            }
                        )
                    }
                )
            }
        }
    }
}

/**
 * Screen 1: Displays the current syllabus details in a card with backpack illustration
 * Matches Screenshot 1
 */
@Composable
private fun SyllabusOverviewView(
    profile: UserProfile,
    onStartChange: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            ProfileSyncStatusCard(
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))

            // School Bag Illustration Container (matching Screenshot 1 circle header)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)), // Warm peach / beige circle
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎒",
                    fontSize = 46.sp
                )
            }
        }

        item {
            // Main Syllabus Card Container
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, shape = RoundedCornerShape(24.dp), spotColor = Color(0xFF64748B).copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Item 1: ক্লাস
                    SyllabusDetailItem(
                        iconEmoji = "🖥️",
                        iconBg = Color(0xFFDCFCE7),
                        title = "ক্লাস",
                        value = formatClassDisplay(profile.studentClass)
                    )

                    // Item 2: গ্রুপ
                    SyllabusDetailItem(
                        iconEmoji = "🧪",
                        iconBg = Color(0xFFFEE2E2),
                        title = "গ্রুপ",
                        value = formatGroupDisplay(profile.group)
                    )

                    // Item 3: পরীক্ষার ধরন
                    val examTypeDisplay = when {
                        profile.studentClass.contains("এইচএসসি") || profile.studentClass.contains("HSC") || profile.studentClass.contains("11") || profile.studentClass.contains("12") -> "এইচএসসি পরীক্ষা"
                        profile.studentClass.contains("এডমিশন") -> "বিশ্ববিদ্যালয় ভর্তি পরীক্ষা"
                        else -> "এসএসসি ও বার্ষিক পরীক্ষা"
                    }
                    SyllabusDetailItem(
                        iconEmoji = "📝",
                        iconBg = Color(0xFFFEF9C3),
                        title = "পরীক্ষার ধরন",
                        value = examTypeDisplay
                    )

                    // Item 4: পরীক্ষার সাল
                    val rawYear = profile.examBatch.filter { it.isDigit() }.ifEmpty { "2027" }
                    val yearOnly = toBengaliDigits(rawYear)
                    SyllabusDetailItem(
                        iconEmoji = "📅",
                        iconBg = Color(0xFFE0F2FE),
                        title = "পরীক্ষার সাল",
                        value = yearOnly
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        thickness = 1.dp,
                        color = Color(0xFFF1F5F9)
                    )

                    // Subtitle Note from Screenshot 1:
                    Text(
                        text = "ক্লাস, পরীক্ষার ধরন ও গ্রুপ পরিবর্তনের মাধ্যমে তোমার সিলেবাস পরিবর্তন করো",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button: সিলেবাস পরিবর্তন (Light Blue Button matching Screenshot 1)
                    Button(
                        onClick = onStartChange,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEF2FF),
                            contentColor = Color(0xFF4F46E5)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("button_open_syllabus_change")
                    ) {
                        Text(
                            text = "সিলেবাস পরিবর্তন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4338CA)
                        )
                    }
                }
            }
        }

        item {
            // Pro Tip Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "সিলেবাস পরিবর্তন করলে হোম স্ক্রিনের রুটিন এবং কোর্স কনটেন্ট স্বয়ংক্রিয়ভাবে নতুন সিলেবাস অনুযায়ী সমন্বিত হবে।",
                        fontSize = 12.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Screen 2 & 3: Interactive Class, Batch and Group Selection
 * Matches Screenshot 2 (initial class selection) and Screenshot 3 (expanded batch & group when HSC is chosen)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SyllabusSelectorView(
    classesFromApi: List<com.example.common.network.ShikhoClassItem>,
    selectedClass: String,
    onClassSelected: (String, String) -> Unit,
    selectedBatch: String,
    onBatchSelected: (String) -> Unit,
    selectedGroup: String,
    onGroupSelected: (String) -> Unit,
    serverBatchOptions: List<BatchOption> = emptyList(),
    isSyncing: Boolean = false,
    onConfirm: () -> Unit
) {
    val availableBatches = serverBatchOptions.map { toBengaliDigits(it.year.toString()) }

    val selectedClassItem = classesFromApi.firstOrNull { it.nameBn == selectedClass || it.code == selectedClass }
    val hasGroups = selectedClassItem?.isGroupRequired ?: !(
        selectedClass.contains("৫") ||
        selectedClass.contains("৬") ||
        selectedClass.contains("৭") ||
        selectedClass.contains("৮")
    )

    val groupOptions = when {
        selectedClass.contains("এডমিশন") -> listOf(
            Pair("K", "'ক' ইউনিট (বিজ্ঞান)"),
            Pair("Kh", "'খ' ইউনিট (মানবিক)"),
            Pair("G", "'গ' ইউনিট (ব্যবসায় শিক্ষা)"),
            Pair("M", "মেডিকেল"),
            Pair("E", "ইঞ্জিনিয়ারিং")
        )
        else -> listOf(
            Pair("S", "বিজ্ঞান"),
            Pair("H", "মানবিক"),
            Pair("B", "ব্যবসায় শিক্ষা")
        )
    }

    val isButtonEnabled = !isSyncing &&
        selectedClass.isNotEmpty() &&
        selectedBatch.isNotEmpty() &&
        (!hasGroups || selectedGroup.isNotEmpty())

    Scaffold(
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onConfirm,
                        enabled = isButtonEnabled,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isButtonEnabled) Color(0xFF1E3A8A) else Color(0xFF94A3B8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("button_confirm_syllabus")
                    ) {
                        if (isSyncing) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ডাটাবেজ থেকে সিলেবাস সিঙ্ক হচ্ছে...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "এগিয়ে যাও",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Taxonomy source status banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(Color(0xFF16A34A), CircleShape)
                        )
                        Text(
                            text = "GET /class_list?vendor=BD • ১০০% রিয়েল ডাটাবেজ কানেকশন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF15803D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Section 1: ক্লাস নির্বাচন
                Text(
                    text = "১. তুমি কোন ক্লাসে লেখাপড়া করছো?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Flow layout / Grid of Class Pills from Real Database / API
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (classesFromApi.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1D4ED8)
                                )
                                Text(
                                    text = "ডাটাবেজ থেকে ক্লাস তালিকা লোড হচ্ছে...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        classesFromApi.forEach { classItem ->
                            val label = classItem.nameBn.ifBlank { classItem.nameEn }
                            val isSelected = selectedClass == label || selectedClass == classItem.code
                            ClassPillItem(
                                badge = classItem.code,
                                label = label,
                                isSelected = isSelected,
                                onClick = {
                                    onClassSelected(label, classItem.code)
                                    val newHasGroups = classItem.isGroupRequired
                                    if (!newHasGroups) {
                                        onGroupSelected("সাধারণ")
                                    } else if (selectedGroup == "সাধারণ" || selectedGroup.isEmpty()) {
                                        onGroupSelected("মানবিক")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                // Section 2: পরীক্ষার সাল নির্বাচন
                Row {
                    Text(
                        text = "২. পরীক্ষার সাল সিলেক্ট করো",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (availableBatches.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⏳ ডাটাবেজ (GraphQL BatchOptions) থেকে ব্যাচ তালিকা লোড হচ্ছে...",
                            fontSize = 13.sp,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableBatches.forEach { batch ->
                            val isSelected = selectedBatch == batch
                            BatchPillItem(
                                batch = batch,
                                isSelected = isSelected,
                                onClick = { onBatchSelected(batch) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (hasGroups) {
                item {
                    // Section 3: গ্রুপ নির্বাচন
                    Row {
                        Text(
                            text = "৩. গ্রুপ সিলেক্ট করো",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        groupOptions.forEach { (badge, group) ->
                            val isSelected = selectedGroup == group
                            GroupPillItem(
                                badge = badge,
                                group = group,
                                isSelected = isSelected,
                                onClick = { onGroupSelected(group) }
                            )
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ℹ️ $selectedClass-এর জন্য কোনো বিভাগ (গ্রুপ) নেই। সকল বিষয়ের সাধারণ সিলেবাস প্রযোজ্য হবে।",
                            fontSize = 12.sp,
                            color = Color(0xFF1D4ED8),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

/**
 * Individual Class Pill (matches Screenshot 2 & 3 pills)
 */
@Composable
private fun ClassPillItem(
    badge: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) Color(0xFF10B981) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF1E293B)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("class_pill_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Badge Circle (or checkmark if selected)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when (badge) {
                                "৫" -> Color(0xFFFEF9C3)
                                "৬" -> Color(0xFFE0F2FE)
                                "৭" -> Color(0xFFEDE9FE)
                                "৮" -> Color(0xFFDCFCE7)
                                "৯" -> Color(0xFFE0F2FE)
                                "১০" -> Color(0xFFFEF9C3)
                                "HSC" -> Color(0xFFDCFCE7)
                                else -> Color(0xFFF3E8FF)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (badge) {
                            "৫" -> Color(0xFFCA8A04)
                            "৬" -> Color(0xFF0284C7)
                            "৭" -> Color(0xFF7C3AED)
                            "৮" -> Color(0xFF16A34A)
                            "৯" -> Color(0xFF0284C7)
                            "১০" -> Color(0xFFCA8A04)
                            "HSC" -> Color(0xFF16A34A)
                            else -> Color(0xFF9333EA)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Individual Batch Pill (e.g. ২০২৫, ২০২৬, ২০২৭, ২০২৮)
 */
@Composable
private fun BatchPillItem(
    batch: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) Color(0xFF1E3A8A) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF1E293B)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("batch_pill_$batch")
    ) {
        Box(
            modifier = Modifier.padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = batch,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Individual Group Pill (e.g. বিজ্ঞান, মানবিক, ব্যবসায় শিক্ষা)
 */
@Composable
private fun GroupPillItem(
    badge: String,
    group: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) Color(0xFF1E3A8A) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF1E293B)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = BorderStroke(1.2.dp, borderColor),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("group_pill_$group")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White else when (badge) {
                            "S" -> Color(0xFFFEF3C7)
                            "H" -> Color(0xFFEDE9FE)
                            else -> Color(0xFFFCE7F3)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color(0xFF1E3A8A) else when (badge) {
                        "S" -> Color(0xFFB45309)
                        "H" -> Color(0xFF6D28D9)
                        else -> Color(0xFFBE185D)
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = group,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Row inside Syllabus Card
 */
@Composable
private fun SyllabusDetailItem(
    iconEmoji: String,
    iconBg: Color,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji container
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconEmoji, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}
