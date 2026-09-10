package com.example.common.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.common.network.UserSessionManager
import com.example.common.ui.components.UserAvatarView

/**
 * Profile quick action bottom sheet exactly matching Screenshot 1 (Screenshot_20260910_064016.jpg)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileOptionsSheet(
    onDismissRequest: () -> Unit,
    onNotificationsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSyllabusChangeClick: () -> Unit = {},
    onSavedQuestionsClick: () -> Unit = {},
    onVideoDownloadsClick: () -> Unit = {},
    onAdmissionInfoClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val userProfile = UserSessionManager.currentUserProfile

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp)
        ) {
            // Header: Avatar + User Info + Close (X) button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Photo / Dynamic letter fallback
                UserAvatarView(
                    avatarUrl = userProfile.avatarUrl,
                    name = userProfile.name,
                    size = 60.dp,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userProfile.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${userProfile.studentClass} - ${userProfile.group} - ${userProfile.examBatch.take(9)}...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = userProfile.institutionName.uppercase(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("button_close_profile_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "বন্ধ করুন",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 1: নোটিফিকেশন
            ProfileOptionRowItem(
                icon = Icons.Default.Notifications,
                iconBg = Color(0xFFFEF3C7),
                iconTint = Color(0xFFD97706),
                title = "নোটিফিকেশন",
                subtitle = "সর্বশেষ আপডেট দেখো",
                onClick = {
                    onDismissRequest()
                    onNotificationsClick()
                },
                testTag = "sheet_option_notifications"
            )

            // Option 2: প্রোফাইল এডিট
            ProfileOptionRowItem(
                icon = Icons.Default.ManageAccounts,
                iconBg = Color(0xFFE0F2FE),
                iconTint = Color(0xFF0284C7),
                title = "প্রোফাইল এডিট",
                subtitle = "তোমার তথ্য পরিবর্তন করো",
                onClick = {
                    onDismissRequest()
                    onEditProfileClick()
                },
                testTag = "sheet_option_edit_profile"
            )

            // Option 3: সিলেবাস পরিবর্তন
            ProfileOptionRowItem(
                icon = Icons.Default.AutoStories,
                iconBg = Color(0xFFFFEDD5),
                iconTint = Color(0xFFEA580C),
                title = "সিলেবাস পরিবর্তন",
                subtitle = "ক্লাস এবং ব্যাচ পরিবর্তন করো",
                onClick = {
                    onDismissRequest()
                    onSyllabusChangeClick()
                },
                testTag = "sheet_option_syllabus"
            )

            // Option 4: সেভ্ড প্রশ্ন
            ProfileOptionRowItem(
                icon = Icons.Default.Bookmark,
                iconBg = Color(0xFFCCFBF1),
                iconTint = Color(0xFF0D9488),
                title = "সেভ্ড প্রশ্ন",
                subtitle = "সেভ করা প্রশ্নগুলো দেখো",
                onClick = {
                    onDismissRequest()
                    onSavedQuestionsClick()
                },
                testTag = "sheet_option_saved_questions"
            )

            // Option 5: ভিডিও ডাউনলোড
            ProfileOptionRowItem(
                icon = Icons.Default.FileDownload,
                iconBg = Color(0xFFF3E8FF),
                iconTint = Color(0xFF9333EA),
                title = "ভিডিও ডাউনলোড",
                subtitle = "ডাউনলোডেড ভিডিও দেখো",
                onClick = {
                    onDismissRequest()
                    onVideoDownloadsClick()
                },
                testTag = "sheet_option_video_downloads"
            )

            // Option 6: ভর্তি সম্পর্কিত তথ্য (User requested option from Screenshot 1 & 2-4)
            ProfileOptionRowItem(
                icon = Icons.Default.MenuBook,
                iconBg = Color(0xFFFFE4E6),
                iconTint = Color(0xFFE11D48),
                title = "ভর্তি সম্পর্কিত তথ্য",
                subtitle = "ভর্তির বিস্তারিত তথ্য দেখো",
                onClick = {
                    onDismissRequest()
                    onAdmissionInfoClick()
                },
                testTag = "sheet_option_admission_info"
            )

            // Option 7: সেটিংস
            ProfileOptionRowItem(
                icon = Icons.Default.Settings,
                iconBg = Color(0xFFE2E8F0),
                iconTint = Color(0xFF475569),
                title = "সেটিংস",
                subtitle = "হেল্প এবং যোগাযোগ",
                onClick = {
                    onDismissRequest()
                    onSettingsClick()
                },
                testTag = "sheet_option_settings"
            )

            // Option 8: লগইন / অ্যাকাউন্ট পরিবর্তন (Shikho API Auth Login)
            ProfileOptionRowItem(
                icon = Icons.Default.Lock,
                iconBg = Color(0xFFEEF2FF),
                iconTint = Color(0xFF4F46E5),
                title = if (userProfile.isLoggedIn) "লগইনকৃত অ্যাকাউন্ট পরিবর্তন" else "অ্যাকাউন্টে লগইন",
                subtitle = if (userProfile.isLoggedIn) "বর্তমান অ্যাকাউন্ট: ${userProfile.phone}" else "মোবাইল ও পাসওয়ার্ড দিয়ে সাইন-ইন করো",
                onClick = {
                    onDismissRequest()
                    onLoginClick()
                },
                testTag = "sheet_option_login"
            )

            // Option 9: লগ আউট (শুধুমাত্র লগইন থাকা অবস্থায় প্রদর্শিত)
            if (userProfile.isLoggedIn) {
                ProfileOptionRowItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconBg = Color(0xFFFEE2E2),
                    iconTint = Color(0xFFEF4444),
                    title = "লগ আউট করুন",
                    subtitle = "অ্যাকাউন্ট থেকে সাইন আউট করো",
                    onClick = {
                        onDismissRequest()
                        onLogoutClick()
                    },
                    testTag = "sheet_option_logout"
                )
            }
        }
    }
}

/**
 * Single Row item in the Profile Options Sheet matching Screenshot 1
 */
@Composable
private fun ProfileOptionRowItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 11.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon circular container
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title and Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
        }

        // Right chevron
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(22.dp)
        )
    }
}
