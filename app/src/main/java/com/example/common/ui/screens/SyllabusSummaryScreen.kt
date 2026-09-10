package com.example.common.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.common.data.local.UserAcademicProfileEntity
import com.example.common.ui.components.DarkTextColor
import com.example.common.ui.components.MutedTextColor
import com.example.common.ui.components.SyllabusSummaryCard
import com.example.common.ui.components.VibrantBrandGreen
import com.example.common.viewmodel.SyllabusViewModel

/**
 * Screen 2: Academic Syllabus Summary View with Profile Preview Card
 */
@Composable
fun SyllabusSummaryScreen(
    viewModel: SyllabusViewModel,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("summary_back_button")
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
                            text = "একাডেমিক প্রোফাইল",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextColor
                        )
                        Text(
                            text = "বর্তমান সিলেবাস ও পরীক্ষার তথ্য",
                            fontSize = 12.sp,
                            color = MutedTextColor
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            val profile = uiState.savedProfile
            if (profile != null) {
                SyllabusSummaryCard(
                    profile = profile,
                    onEditClick = {
                        viewModel.switchToSelectionMode()
                        onEditClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = VibrantBrandGreen,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                // Fallback default entity preview
                val fallbackEntity = UserAcademicProfileEntity(
                    userId = "usr_101",
                    classId = "C11",
                    classTitleBn = "এইচএসসি",
                    batchYear = "2028",
                    groupCode = "HUM",
                    groupTitleBn = "মানবিক"
                )
                SyllabusSummaryCard(
                    profile = fallbackEntity,
                    onEditClick = {
                        viewModel.switchToSelectionMode()
                        onEditClick()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
