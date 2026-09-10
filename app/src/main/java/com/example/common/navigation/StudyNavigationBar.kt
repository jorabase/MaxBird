package com.example.common.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ActiveIndigo = Color(0xFF4F46E5)
private val ActivePurple = Color(0xFF7C3AED)
private val InactiveSlate = Color(0xFF64748B)

/**
 * Highly advanced, modern floating pill navigation bar with:
 * - 5 logical tabs: Home, Explore, Courses, AI, Profile
 * - Glassmorphism elevated curved surface
 * - Animated morphing pill background behind active item
 * - Soft bounce scale effect when active
 * - Smart notification/badge indicators
 */
@Composable
fun ModernFloatingNavigationBar(
    currentDestination: StudyDestination,
    onDestinationSelected: (StudyDestination) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<StudyDestination, String> = mapOf(
        StudyDestination.AI_TUTOR to "AI"
    )
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Pill Card
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 18.dp,
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = Color(0xFFE2E8F0).copy(alpha = 0.85f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudyDestination.values().forEach { destination ->
                    val isSelected = destination == currentDestination
                    val badgeText = badges[destination]

                    AdvancedNavPillItem(
                        destination = destination,
                        isSelected = isSelected,
                        badgeText = badgeText,
                        onClick = { onDestinationSelected(destination) },
                        modifier = Modifier.weight(if (isSelected) 1.25f else 1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedNavPillItem(
    destination: StudyDestination,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconVector: ImageVector = when (destination) {
        StudyDestination.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
        StudyDestination.EXPLORE -> if (isSelected) Icons.Filled.Explore else Icons.Outlined.Explore
        StudyDestination.COURSES -> if (isSelected) Icons.Filled.School else Icons.Outlined.School
        StudyDestination.AI_TUTOR -> if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
        StudyDestination.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "nav_item_scale"
    )

    val pillBackgroundBrush = when (destination) {
        StudyDestination.AI_TUTOR -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7))
        )
        else -> Brush.horizontalGradient(
            colors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .testTag(destination.testTag)
            .clip(RoundedCornerShape(26.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Active Morphing Pill Background
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(23.dp))
                    .background(pillBackgroundBrush)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale)
                .padding(horizontal = if (isSelected) 6.dp else 2.dp)
        ) {
            // Icon Container
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = destination.titleBn,
                    tint = if (isSelected) Color.White else InactiveSlate,
                    modifier = Modifier.size(20.dp)
                )

                // Smart Badge (like "AI")
                if (badgeText != null && !isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFEC4899))
                            .padding(horizontal = 3.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Expanding text when selected
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = destination.titleBn,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Desktop / Tablet Side Navigation Rail with premium aesthetic
 */
@Composable
fun ModernAdaptiveNavigationRail(
    currentDestination: StudyDestination,
    onDestinationSelected: (StudyDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // App Mini Branding
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F172A),
                                Color(0xFF4F46E5)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🦅",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            StudyDestination.values().forEach { destination ->
                val isSelected = destination == currentDestination
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        val iconVector = when (destination) {
                            StudyDestination.HOME -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                            StudyDestination.EXPLORE -> if (isSelected) Icons.Filled.Explore else Icons.Outlined.Explore
                            StudyDestination.COURSES -> if (isSelected) Icons.Filled.School else Icons.Outlined.School
                            StudyDestination.AI_TUTOR -> if (isSelected) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome
                            StudyDestination.PROFILE -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                        }
                        Icon(
                            imageVector = iconVector,
                            contentDescription = destination.titleBn,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = destination.titleBn,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = ActiveIndigo,
                        selectedTextColor = ActiveIndigo,
                        indicatorColor = ActiveIndigo.copy(alpha = 0.12f),
                        unselectedIconColor = InactiveSlate,
                        unselectedTextColor = InactiveSlate
                    )
                )
            }
        }
    }
}
