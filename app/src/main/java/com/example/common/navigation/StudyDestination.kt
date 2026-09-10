package com.example.common.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Modern 5-tab core navigation destinations:
 * 1. Home (হোম)
 * 2. Explore (এক্সপ্লোর)
 * 3. Courses (কোর্স)
 * 4. AI Tutor (শিখো AI / AI)
 * 5. Profile (প্রোফাইল)
 */
enum class StudyDestination(
    val titleEn: String,
    val titleBn: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        titleEn = "Home",
        titleBn = "হোম",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    EXPLORE(
        titleEn = "Explore",
        titleBn = "এক্সপ্লোর",
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_tab_explore"
    ),
    COURSES(
        titleEn = "Courses",
        titleBn = "কোর্স",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "nav_tab_courses"
    ),
    AI_TUTOR(
        titleEn = "AI Tutor",
        titleBn = "AI",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_tab_ai"
    ),
    PROFILE(
        titleEn = "Profile",
        titleBn = "প্রোফাইল",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_tab_profile"
    )
}
