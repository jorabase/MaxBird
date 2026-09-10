package com.example.common.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.model.ClassItem
import com.example.common.model.AcademicGroup
import com.example.common.ui.components.DarkTextColor
import com.example.common.ui.components.DisabledGray
import com.example.common.ui.components.GroupChip
import com.example.common.ui.components.MutedTextColor
import com.example.common.ui.components.SelectionChip
import com.example.common.ui.components.VibrantBrandGreen
import com.example.common.viewmodel.SyllabusUiState
import com.example.common.viewmodel.SyllabusViewModel

/**
 * 100% Server-Driven Syllabus Selection Screen (Class -> Batch -> Group)
 * Directly renders dynamic Class grid, Batch selection row, Group selection, and Bottom Action Bar.
 */
@Composable
fun SyllabusSelectionScreen(
    viewModel: SyllabusViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug logging for reactive state diagnosis
    Log.d("DEBUG_SYLLABUS", "Selected Class: ${uiState.selectedClass?.titleBn}, Batches: ${uiState.selectedClass?.batches}")

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SyllabusSelectionTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            SyllabusBottomActionBar(
                isSubmitEnabled = uiState.isSubmitEnabled,
                isSubmitting = uiState.isSubmitting,
                onSubmit = {
                    viewModel.submitSyllabus(onSuccess = onSubmitSuccess)
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            SyllabusLoadingView(modifier = Modifier.padding(innerPadding))
        } else if (uiState.config == null && uiState.errorMessage != null) {
            SyllabusErrorView(
                errorMessage = uiState.errorMessage ?: "তথ্য লোড করা যায়নি",
                onRetry = { viewModel.loadData() },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            SyllabusSelectionContent(
                uiState = uiState,
                onClassSelected = { viewModel.onClassSelected(it) },
                onBatchSelected = { viewModel.onBatchSelected(it) },
                onGroupSelected = { viewModel.onGroupSelected(it) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun SyllabusSelectionTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("syllabus_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "ফিরে যান",
                    tint = DarkTextColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "সিলেবাস পরিবর্তন",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextColor
                )
                Text(
                    text = "তোমার বর্তমান শ্রেণি বা পরীক্ষার ধাপ নির্বাচন করো",
                    fontSize = 12.sp,
                    color = MutedTextColor
                )
            }
        }
    }
}

@Composable
private fun SyllabusSelectionContent(
    uiState: SyllabusUiState,
    onClassSelected: (ClassItem) -> Unit,
    onBatchSelected: (String) -> Unit,
    onGroupSelected: (AcademicGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val config = uiState.config ?: return
    val selectedClass = uiState.selectedClass

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Step 1: Class Selection (3-Column Grid, 8.dp gap)
        SelectionSectionHeader(
            stepNumber = "১",
            title = "ক্লাস সিলেক্ট করো*",
            subtitle = "তোমার বর্তমান শ্রেণি বা পরীক্ষার ধাপ বেছে নাও"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dynamic 3-column Grid layout
        val classes = config
        val rows = (classes.size + 2) / 3

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (colIndex in 0 until 3) {
                        val itemIndex = rowIndex * 3 + colIndex
                        if (itemIndex < classes.size) {
                            val classItem = classes[itemIndex]
                            val isSelected = selectedClass?.id == classItem.id
                            SelectionChip(
                                title = classItem.titleBn,
                                badgeText = classItem.badge,
                                badgeIcon = if (classItem.badgeType.equals("ICON", ignoreCase = true) && classItem.badge != "🎓") Icons.Default.School else null,
                                isSelected = isSelected,
                                onClick = { onClassSelected(classItem) },
                                modifier = Modifier.weight(1f),
                                testTag = "class_chip_${classItem.id}"
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Step 2: Batch Selection (Direct rendering whenever batches exist on the selected class)
        uiState.selectedClass?.let { currentClass ->
            if (currentClass.batches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "তোমার ${currentClass.titleBn} পরীক্ষার ব্যাচ সিলেক্ট করো*",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal row of batch chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(currentClass.batches) { batch ->
                        val isSelected = uiState.selectedBatch == batch
                        FilterChip(
                            selected = isSelected,
                            onClick = { onBatchSelected(batch) },
                            label = {
                                Text(
                                    text = batch,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00C853),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF00C853),
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (isSelected) Color(0xFF00C853) else Color(0xFFE0E0E0),
                                borderWidth = if (isSelected) 1.5.dp else 1.dp,
                                enabled = true,
                                selected = isSelected
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("batch_chip_$batch")
                        )
                    }
                }
            }
        }

        // Step 3: Group Selection (Direct rendering whenever groups exist on the selected class)
        uiState.selectedClass?.let { currentClass ->
            if (currentClass.groups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "গ্রুপ সিলেক্ট করো*",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextColor
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentClass.groups.forEach { groupItem ->
                        val isSelected = uiState.selectedGroup?.code == groupItem.code
                        GroupChip(
                            title = groupItem.titleBn,
                            badgeText = groupItem.badge,
                            isSelected = isSelected,
                            onClick = { onGroupSelected(groupItem) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "group_chip_${groupItem.code}"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun SelectionSectionHeader(
    stepNumber: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = VibrantBrandGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextColor
            )
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = MutedTextColor
            )
        }
    }
}

@Composable
private fun SyllabusBottomActionBar(
    isSubmitEnabled: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onSubmit,
                enabled = isSubmitEnabled && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_syllabus_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantBrandGreen,
                    contentColor = Color.White,
                    disabledContainerColor = DisabledGray,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সংরক্ষণ করা হচ্ছে...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "এগিয়ে যাও",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SyllabusLoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = VibrantBrandGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "সিলেবাস কনফিগারেশন লোড হচ্ছে...",
                fontSize = 13.5.sp,
                color = MutedTextColor
            )
        }
    }
}

@Composable
private fun SyllabusErrorView(
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ত্রুটি ঘটেছে",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = MutedTextColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantBrandGreen),
                    shape = RoundedCornerShape(10.dp)
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
