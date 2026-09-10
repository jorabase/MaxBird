package com.example.common.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.common.ui.components.UserAvatarView
import com.example.common.model.UserProfile
import com.example.common.network.UserSessionManager
import com.example.common.viewmodel.ProfileEditViewModel

/**
 * Premium 3-Step Profile Edit Screen matching the user's uploaded screenshots
 * (ধাপ ১/৩: ব্যক্তিগত তথ্য / Personal Info,
 *  ধাপ ২/৩: বোর্ড ও রোল ইনফরমেশন / Board & Roll Info,
 *  ধাপ ৩/৩: কলেজ ও অভিভাবকের তথ্য / College & Guardian Info)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: ProfileEditViewModel = remember { ProfileEditViewModel() },
    modifier: Modifier = Modifier
) {
    // Current step: 1 = Personal Info, 2 = Board/Roll Info, 3 = College & Guardian Info
    var currentStep by remember { mutableIntStateOf(1) }

    // State initialized from UserSessionManager
    var profile by remember { mutableStateOf(UserSessionManager.currentUserProfile) }

    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    if (currentStep > 1) {
                                        currentStep -= 1
                                    } else {
                                        onBackClick()
                                    }
                                },
                                modifier = Modifier.testTag("edit_profile_back")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "ফিরে যান",
                                    tint = Color(0xFF1E293B)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (currentStep) {
                                    1 -> "ব্যক্তিগত তথ্য যোগ করো"
                                    2 -> "বোর্ড ও রোল ইনফরমেশন"
                                    else -> "কলেজ ও অভিভাবকের তথ্য"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Step Indicator Text e.g. "ধাপ ১/৩"
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = when (currentStep) {
                                    1 -> "ধাপ ১/৩"
                                    2 -> "ধাপ ২/৩"
                                    else -> "ধাপ ৩/৩"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3-Segment Progress Indicator Bar (matches screenshots 1, 3, 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 1..3) {
                            val isActive = i <= currentStep
                            val barColor = if (isActive) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Persistent Bottom Actions: [ফিরে যাও] and [এগিয়ে যাও / সংরক্ষণ করো]
            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Back Button
                    OutlinedButton(
                        onClick = {
                            if (currentStep > 1) {
                                currentStep -= 1
                            } else {
                                onBackClick()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, Color(0xFF2563EB)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF2563EB)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("step_back_button")
                    ) {
                        Text(
                            text = "ফিরে যাও",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Next / Complete Button
                    Button(
                        onClick = {
                            if (currentStep < 3) {
                                currentStep += 1
                            } else {
                                // Save and finish with live UpdateProfileWithoutUseName mutation
                                viewModel.saveCompleteProfile(
                                    profile = profile,
                                    onSuccess = {
                                        UserSessionManager.saveProfile(profile)
                                        onSaveSuccess()
                                    }
                                )
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E3A8A)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("step_next_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "সংরক্ষণ হচ্ছে...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = if (currentStep < 3) "এগিয়ে যাও" else "সংরক্ষণ করো",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "step_content_transition",
            modifier = Modifier.padding(innerPadding)
        ) { step ->
            when (step) {
                1 -> Step1PersonalInfo(
                    profile = profile,
                    onProfileChange = { profile = it }
                )
                2 -> Step2InstitutionalInfo(
                    profile = profile,
                    onProfileChange = { profile = it }
                )
                3 -> Step3GuardianInfo(
                    profile = profile,
                    viewModel = viewModel,
                    onProfileChange = { profile = it }
                )
            }
        }
    }
}

/**
 * Step 1: ব্যক্তিগত তথ্য (Personal Information)
 * Matches Screenshots 1 & 2
 */
@Composable
private fun Step1PersonalInfo(
    profile: UserProfile,
    onProfileChange: (UserProfile) -> Unit
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onProfileChange(profile.copy(avatarUrl = uri.toString()))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Profile Picture with Camera Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("avatar_picker_box"),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarView(
                            avatarUrl = profile.avatarUrl,
                            name = profile.name,
                            size = 100.dp,
                            fontSize = 36.sp,
                            borderColor = Color(0xFF2563EB),
                            borderWidth = 3.dp
                        )
                    }

                    // Camera Icon Badge
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("avatar_camera_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "ছবি আপলোড",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "তোমার প্রোফাইল পিকচার দাও",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E3A8A)
                )
            }
        }

        // Your Name Field
        item {
            Column {
                Row {
                    Text(
                        text = "তোমার নাম",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.name,
                    onValueChange = { onProfileChange(profile.copy(name = it)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_name")
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Warning note: ৬০ দিনের মধ্যে নাম চেঞ্জ করতে পারবে না।
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "৬০ দিনের মধ্যে নাম চেঞ্জ করতে পারবে না।",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Mobile Number Field (Read-only as per specs)
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "মোবাইল নম্বর",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(অপরিবর্তনযোগ্য)",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.phone,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "লকড",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE2E8F0),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_phone")
                )
            }
        }

        // Birth Date Field
        item {
            Column {
                Row {
                    Text(
                        text = "জন্ম তারিখ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.birthDate,
                    onValueChange = { onProfileChange(profile.copy(birthDate = it)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "তারিখ বাছাই করুন",
                            tint = Color(0xFF64748B)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_dob")
                )
            }
        }

        // Gender Selector Cards (ছাত্র / ছাত্রী) - Matches Screenshots 1 & 2
        item {
            Column {
                Row {
                    Text(
                        text = "তুমি একজন",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ছাত্র Card
                    GenderSelectionCard(
                        title = "ছাত্র",
                        isSelected = profile.gender == "ছাত্র",
                        isMale = true,
                        onClick = { onProfileChange(profile.copy(gender = "ছাত্র")) },
                        modifier = Modifier.weight(1f)
                    )

                    // ছাত্রী Card
                    GenderSelectionCard(
                        title = "ছাত্রী",
                        isSelected = profile.gender == "ছাত্রী",
                        isMale = false,
                        onClick = { onProfileChange(profile.copy(gender = "ছাত্রী")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Step 2: বোর্ড ও রোল ইনফরমেশন (Board & Roll Information)
 * Matches Screenshots 3 & 4 and User Flow Specs
 */
@Composable
private fun Step2InstitutionalInfo(
    profile: UserProfile,
    onProfileChange: (UserProfile) -> Unit
) {
    var expandedSscBoard by remember { mutableStateOf(false) }
    var expandedHscBoard by remember { mutableStateOf(false) }
    var expandedShift by remember { mutableStateOf(false) }

    val boardList = listOf(
        "Jessore", "Dhaka", "Chattogram", "Rajshahi",
        "Dinajpur", "Cumilla", "Barishal", "Sylhet",
        "Mymensingh", "Madrasah", "Technical"
    )
    val shiftList = listOf("প্রযোজ্য নয়", "প্রভাতী (সকাল)", "দিবা (দুপুর)", "সান্ধ্য")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Class and Group Row (side by side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ক্লাস",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.studentClass,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "গ্রুপ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.group,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Exam Batch and Class Shift
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "পরীক্ষার ব্যাচ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.examBatch,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(
                            text = "ক্লাসের শিফট",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Box {
                        OutlinedTextField(
                            value = profile.classShift,
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            trailingIcon = {
                                IconButton(onClick = { expandedShift = true }) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedShift = true }
                        )

                        DropdownMenu(
                            expanded = expandedShift,
                            onDismissRequest = { expandedShift = false }
                        ) {
                            shiftList.forEach { shift ->
                                DropdownMenuItem(
                                    text = { Text(shift) },
                                    onClick = {
                                        onProfileChange(profile.copy(classShift = shift))
                                        expandedShift = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // SSC Board Dropdown
        item {
            Column {
                Row {
                    Text(
                        text = "এসএসসি বোর্ড",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = profile.sscBoard,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedSscBoard = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSscBoard = true }
                    )

                    DropdownMenu(
                        expanded = expandedSscBoard,
                        onDismissRequest = { expandedSscBoard = false }
                    ) {
                        boardList.forEach { board ->
                            DropdownMenuItem(
                                text = { Text(board) },
                                onClick = {
                                    onProfileChange(profile.copy(sscBoard = board))
                                    expandedSscBoard = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // SSC Roll Number
        item {
            Column {
                Row {
                    Text(
                        text = "এসএসসি বোর্ড রোল নাম্বার",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.sscRoll,
                    onValueChange = { onProfileChange(profile.copy(sscRoll = it)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_ssc_roll")
                )
            }
        }

        // HSC Board Dropdown
        item {
            Column {
                Text(
                    text = "এইচএসসি বোর্ড (প্রযোজ্য ক্ষেত্রে)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = profile.hscBoard,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedHscBoard = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedHscBoard = true }
                    )

                    DropdownMenu(
                        expanded = expandedHscBoard,
                        onDismissRequest = { expandedHscBoard = false }
                    ) {
                        boardList.forEach { board ->
                            DropdownMenuItem(
                                text = { Text(board) },
                                onClick = {
                                    onProfileChange(profile.copy(hscBoard = board))
                                    expandedHscBoard = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // HSC Roll Number
        item {
            Column {
                Text(
                    text = "এইচএসসি বোর্ড রোল নাম্বার",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.hscRoll,
                    onValueChange = { onProfileChange(profile.copy(hscRoll = it)) },
                    singleLine = true,
                    placeholder = { Text("রোল নাম্বার দিন", color = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_hsc_roll")
                )
            }
        }

        // Board Registration Number
        item {
            Column {
                Text(
                    text = "বোর্ড রেজিস্ট্রেশন নম্বর",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.boardRegNumber,
                    onValueChange = { onProfileChange(profile.copy(boardRegNumber = it)) },
                    singleLine = true,
                    placeholder = { Text("রেজিস্ট্রেশন নম্বর লিখুন", color = Color(0xFF94A3B8)) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_board_reg")
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Step 3: কলেজ ও অভিভাবকের তথ্য (College & Guardian Information)
 * Matches Screenshots 4 & 5 and User Flow Specs:
 * - Division & District dynamically loaded via REST APIs
 * - College search/select with UpdateUserSchool mutation
 * - Guardian Name & Mobile Number
 * - Other Tutoring Sources chips (Shikho, Coaching, Private Tutor, Self Study)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3GuardianInfo(
    profile: UserProfile,
    viewModel: ProfileEditViewModel,
    onProfileChange: (UserProfile) -> Unit
) {
    var expandedDivision by remember { mutableStateOf(false) }
    var expandedDistrict by remember { mutableStateOf(false) }
    var expandedMedium by remember { mutableStateOf(false) }
    var showSchoolDialog by remember { mutableStateOf(false) }
    var schoolSearchQuery by remember { mutableStateOf("") }

    val divisions by viewModel.divisions.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val schools by viewModel.schools.collectAsStateWithLifecycle()
    val isSearchingSchools by viewModel.isSearchingSchools.collectAsStateWithLifecycle()

    val fallbackDivisions = listOf("Dhaka", "Chattogram", "Rajshahi", "Khulna", "Barishal", "Sylhet", "Rangpur", "Mymensingh")
    val divisionNames = if (divisions.isNotEmpty()) divisions.map { it.name } else fallbackDivisions

    val fallbackDistricts = listOf("Dhaka", "Gazipur", "Narayanganj", "Khagrachari", "Chattogram", "Cox's Bazar", "Cumilla", "Feni", "Sylhet", "Bogura")
    val districtNames = if (districts.isNotEmpty()) districts.map { it.name } else fallbackDistricts

    val mediumList = listOf("কোনটাই নয়", "বাংলা ভার্সন", "ইংরেজি ভার্সন", "ইংলিশ মিডিয়াম", "মাদ্রাসা")
    val tutoringOptions = listOf(
        "SHIKHO" to "শিখো",
        "COACHING" to "কোচিং",
        "PRIVATE_TUTOR" to "প্রাইভেট টিউটর",
        "SELF_STUDY" to "নিজে নিজে"
    )

    if (showSchoolDialog) {
        val matchedDiv = divisions.find { it.name == profile.institutionDivision || it.nameBn == profile.institutionDivision }
        val matchedDist = districts.find { it.name == profile.institutionDistrict || it.nameBn == profile.institutionDistrict }

        AlertDialog(
            onDismissRequest = { showSchoolDialog = false },
            title = {
                Text(
                    text = "স্কুল/কলেজ নির্বাচন করো",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = schoolSearchQuery,
                        onValueChange = { q ->
                            schoolSearchQuery = q
                            if (matchedDiv != null && matchedDist != null) {
                                viewModel.searchSchools(matchedDist.id, matchedDiv.id, q)
                            }
                        },
                        placeholder = { Text("স্কুল বা কলেজের নাম খুঁজুন...") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF2563EB))
                        },
                        trailingIcon = {
                            if (isSearchingSchools) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (schools.isEmpty() && !isSearchingSchools) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (matchedDist == null) "অনুগ্রহ করে আগে জেলা নির্বাচন করুন" else "কোনো স্কুল পাওয়া যায়নি",
                                fontSize = 13.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(schools) { school ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (profile.schoolId == school.id) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        1.dp,
                                        if (profile.schoolId == school.id) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onProfileChange(
                                                profile.copy(
                                                    institutionName = school.name,
                                                    schoolId = school.id
                                                )
                                            )
                                            viewModel.onSchoolSelected(school.id, school.name)
                                            showSchoolDialog = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            tint = if (profile.schoolId == school.id) Color(0xFF2563EB) else Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = school.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (profile.schoolId == school.id) FontWeight.Bold else FontWeight.Normal,
                                            color = Color(0xFF1E293B),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSchoolDialog = false }) {
                    Text("বন্ধ করুন", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Institutional Info Section Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "স্কুল/কলেজের তথ্য",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
        }

        // Division Selector
        item {
            Column {
                Row {
                    Text(
                        text = "প্রতিষ্ঠানের বিভাগ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = profile.institutionDivision,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedDivision = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDivision = true }
                    )

                    DropdownMenu(
                        expanded = expandedDivision,
                        onDismissRequest = { expandedDivision = false }
                    ) {
                        divisionNames.forEach { divName ->
                            DropdownMenuItem(
                                text = { Text(divName) },
                                onClick = {
                                    onProfileChange(
                                        profile.copy(
                                            institutionDivision = divName,
                                            institutionDistrict = "",
                                            institutionName = "",
                                            schoolId = ""
                                        )
                                    )
                                    val matched = divisions.find { it.name == divName || it.nameBn == divName }
                                    if (matched != null) {
                                        viewModel.loadDistricts(matched.id)
                                    }
                                    expandedDivision = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // District Selector
        item {
            Column {
                Row {
                    Text(
                        text = "প্রতিষ্ঠানের জেলা",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = profile.institutionDistrict,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedDistrict = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDistrict = true }
                    )

                    DropdownMenu(
                        expanded = expandedDistrict,
                        onDismissRequest = { expandedDistrict = false }
                    ) {
                        districtNames.forEach { distName ->
                            DropdownMenuItem(
                                text = { Text(distName) },
                                onClick = {
                                    onProfileChange(
                                        profile.copy(
                                            institutionDistrict = distName,
                                            institutionName = "",
                                            schoolId = ""
                                        )
                                    )
                                    val matchedDiv = divisions.find { it.name == profile.institutionDivision || it.nameBn == profile.institutionDivision }
                                    val matchedDist = districts.find { it.name == distName || it.nameBn == distName }
                                    if (matchedDiv != null && matchedDist != null) {
                                        viewModel.searchSchools(matchedDist.id, matchedDiv.id, "")
                                    }
                                    expandedDistrict = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Institution / College Name with Real Search & Update School Mutation
        item {
            Column {
                Row {
                    Text(
                        text = "প্রতিষ্ঠানের নাম (স্কুল/কলেজ)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.institutionName,
                    onValueChange = {
                        onProfileChange(profile.copy(institutionName = it))
                    },
                    singleLine = true,
                    placeholder = { Text("স্কুল বা কলেজের নাম লিখুন বা খুঁজুন") },
                    shape = RoundedCornerShape(14.dp),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val matchedDiv = divisions.find { it.name == profile.institutionDivision || it.nameBn == profile.institutionDivision }
                                val matchedDist = districts.find { it.name == profile.institutionDistrict || it.nameBn == profile.institutionDistrict }
                                if (matchedDiv != null && matchedDist != null) {
                                    viewModel.searchSchools(matchedDist.id, matchedDiv.id, schoolSearchQuery)
                                }
                                showSchoolDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "খুঁজুন",
                                tint = Color(0xFF2563EB)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_profile_institution_name")
                )
            }
        }

        // Education Medium Dropdown
        item {
            Column {
                Row {
                    Text(
                        text = "অন্যান্য শিক্ষা মাধ্যম",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    OutlinedTextField(
                        value = profile.educationMedium,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(onClick = { expandedMedium = true }) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedMedium = true }
                    )

                    DropdownMenu(
                        expanded = expandedMedium,
                        onDismissRequest = { expandedMedium = false }
                    ) {
                        mediumList.forEach { med ->
                            DropdownMenuItem(
                                text = { Text(med) },
                                onClick = {
                                    onProfileChange(profile.copy(educationMedium = med))
                                    expandedMedium = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Guardian Verification Header Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "অভিভাবক ভেরিফিকেশন তথ্য",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A)
                        )
                        Text(
                            text = "জরুরি নোটিফিকেশন ও অগ্রগতি জানতে নম্বর প্রদান করুন",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Guardian Name
        item {
            Column {
                Row {
                    Text(
                        text = "অভিভাবকের নাম",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.guardianName,
                    onValueChange = { onProfileChange(profile.copy(guardianName = it)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_guardian_name")
                )
            }
        }

        // Guardian Mobile Number
        item {
            Column {
                Row {
                    Text(
                        text = "অভিভাবকের মোবাইল নাম্বার",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(text = " *", color = Color.Red, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = profile.guardianPhone,
                    onValueChange = { onProfileChange(profile.copy(guardianPhone = it)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_guardian_phone")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "তোমার এই অভিভাবকের মোবাইল নাম্বার ভেরিফিকেশন করতে হবে",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }
        }

        // Other Tutoring / Private Study Sources
        item {
            Column {
                Text(
                    text = "অন্যান্য প্রাইভেট/টিউটরিং সোর্স",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tutoringOptions.forEach { (key, label) ->
                        val isSelected = profile.otherTutoringSources.contains(key)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val updatedList = if (isSelected) {
                                    profile.otherTutoringSources - key
                                } else {
                                    profile.otherTutoringSources + key
                                }
                                onProfileChange(profile.copy(otherTutoringSources = updatedList))
                            },
                            label = { Text(label, fontSize = 13.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Custom Illustrated Gender Selection Card (matching Screenshots 1 & 2)
 */
@Composable
private fun GenderSelectionCard(
    title: String,
    isSelected: Boolean,
    isMale: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFFE2E8F0)
    val cardBg = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.05f) else Color.White

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("gender_card_$title")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustrated Avatar Box
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMale) Color(0xFFDBEAFE) else Color(0xFFFCE7F3)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMale) Icons.Default.Male else Icons.Default.Female,
                    contentDescription = title,
                    tint = if (isMale) Color(0xFF2563EB) else Color(0xFFEC4899),
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF475569)
                )

                // Radio / Check indicator
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
