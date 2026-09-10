package com.example.common.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.data.local.UserAcademicProfileEntity

/**
 * Brand color palette adhering strictly to specifications:
 * - Selected State: Solid Green border (Color(0xFF00C853)), green background tint (Color(0xFFE8F5E9))
 * - Checkmark: White checkmark inside green circle (Color(0xFF00C853))
 * - Disabled state: Gray (Color(0xFFBDBDBD))
 */
val VibrantBrandGreen = Color(0xFF00C853)
val VibrantGreenTint = Color(0xFFE8F5E9)
val VibrantGreenDark = Color(0xFF009624)
val SoftGrayBorder = Color(0xFFE2E8F0)
val DisabledGray = Color(0xFFBDBDBD)
val DarkTextColor = Color(0xFF1E293B)
val MutedTextColor = Color(0xFF64748B)

/**
 * Custom SelectionChip complying with exact specifications:
 * - Shape: 50.dp capsule/pill shape
 * - Selected State: Solid Green border (Color(0xFF00C853)), green background tint, white checkmark inside green circle replacing the badge
 * - Unselected State: 1.dp soft gray border, white background, circular badge on the left (showing number/HSC/cap icon) and Bengali title on the right
 */
@Composable
fun SelectionChip(
    title: String,
    badgeText: String? = null,
    badgeIcon: ImageVector? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "selection_chip"
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) VibrantBrandGreen else SoftGrayBorder,
        animationSpec = tween(200),
        label = "chip_border"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) VibrantGreenTint else Color.White,
        animationSpec = tween(200),
        label = "chip_bg"
    )
    val badgeBgColor by animateColorAsState(
        targetValue = if (isSelected) VibrantBrandGreen else Color(0xFFF1F5F9),
        animationSpec = tween(200),
        label = "badge_bg"
    )
    val badgeContentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF334155),
        animationSpec = tween(200),
        label = "badge_text"
    )
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = tween(150),
        label = "chip_scale"
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(50.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = VibrantBrandGreen),
                onClick = onClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Left Circular Badge or Checkmark inside Green Circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "নির্বাচিত",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (badgeIcon != null) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeContentColor,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = badgeText ?: "",
                        fontSize = if ((badgeText?.length ?: 0) > 2) 10.sp else 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Bengali Text
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DarkTextColor else Color(0xFF334155),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Batch Selection Chip (Capsule / Pill shape)
 */
@Composable
fun BatchChip(
    year: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "batch_chip"
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) VibrantBrandGreen else SoftGrayBorder,
        animationSpec = tween(200),
        label = "batch_border"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) VibrantGreenTint else Color.White,
        animationSpec = tween(200),
        label = "batch_bg"
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(50.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = VibrantBrandGreen),
                onClick = onClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(VibrantBrandGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = "$year ব্যাচ",
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) VibrantGreenDark else DarkTextColor
            )
        }
    }
}

/**
 * Group Selection Chip (Science, Humanities, Business Studies)
 */
@Composable
fun GroupChip(
    title: String,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "group_chip"
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) VibrantBrandGreen else SoftGrayBorder,
        animationSpec = tween(200),
        label = "group_border"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) VibrantGreenTint else Color.White,
        animationSpec = tween(200),
        label = "group_bg"
    )
    val badgeBgColor by animateColorAsState(
        targetValue = if (isSelected) VibrantBrandGreen else Color(0xFFEEF2FF),
        animationSpec = tween(200),
        label = "group_badge_bg"
    )
    val badgeContentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF4F46E5),
        animationSpec = tween(200),
        label = "group_badge_text"
    )

    Surface(
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(
                BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                RoundedCornerShape(50.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = VibrantBrandGreen),
                onClick = onClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "নির্বাচিত",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = badgeText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DarkTextColor else Color(0xFF334155)
            )
        }
    }
}

/**
 * Summary Card displaying academic overview
 */
@Composable
fun SyllabusSummaryCard(
    profile: UserAcademicProfileEntity,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("syllabus_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Avatar / Illustration Icon with Accent
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFECFDF5))
                    .border(3.dp, VibrantBrandGreen.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "একাডেমিক প্রোফাইল",
                    tint = VibrantBrandGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "বর্তমান একাডেমিক সিলেবাস",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "আপনার নির্বাচিত ক্লাস ও পরীক্ষার তথ্য নিচে দেওয়া হলো",
                fontSize = 12.sp,
                color = MutedTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // 4 Summary Rows
            SummaryDetailRow(
                icon = Icons.Outlined.MenuBook,
                label = "ক্লাস",
                value = profile.classTitleBn.ifBlank { "এইচএসসি" },
                badgeColor = Color(0xFFEFF6FF),
                iconColor = Color(0xFF2563EB)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryDetailRow(
                icon = Icons.Outlined.Person,
                label = "গ্রুপ",
                value = profile.groupTitleBn ?: "প্রযোজ্য নয়",
                badgeColor = Color(0xFFFDF2F8),
                iconColor = Color(0xFFDB2777)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val examType = when {
                profile.classId.contains("12") || profile.classId.contains("ADMISSION") -> "বিশ্ববিদ্যালয় ভর্তি পরীক্ষা"
                profile.classId.contains("11") || profile.classTitleBn.contains("HSC") || profile.classTitleBn.contains("এইচএসসি") -> "এইচএসসি পরীক্ষা"
                profile.classId.contains("9") || profile.classId.contains("10") || profile.classTitleBn.contains("১০") || profile.classTitleBn.contains("৯") -> "এসএসসি ও বার্ষিক পরীক্ষা"
                else -> "বার্ষিক মেধা অন্বেষণ পরীক্ষা"
            }

            SummaryDetailRow(
                icon = Icons.Outlined.CheckCircle,
                label = "পরীক্ষার ধরন",
                value = examType,
                badgeColor = Color(0xFFF0FDF4),
                iconColor = Color(0xFF16A34A)
            )

            Spacer(modifier = Modifier.height(12.dp))

            SummaryDetailRow(
                icon = Icons.Outlined.DateRange,
                label = "পরীক্ষার সাল",
                value = if (!profile.batchYear.isNullOrBlank()) "${profile.batchYear} ব্যাচ" else "চলতি বছর",
                badgeColor = Color(0xFFFFFBEB),
                iconColor = Color(0xFFD97706)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Edit Action Button
            Button(
                onClick = onEditClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("edit_syllabus_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantBrandGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "সিলেবাস পরিবর্তন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SummaryDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    badgeColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MutedTextColor
            )
        }

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = DarkTextColor
        )
    }
}

