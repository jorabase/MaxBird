package com.example.common.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Modern User Avatar Component:
 * 1. Directly renders image from CDN (user.avatarUrl) if present with https protocol.
 * 2. If image is missing, empty, or fails to load, gracefully falls back to an initial-letter
 *    avatar rendered on a vibrant, eye-safe gradient background (e.g., "F" for "Fahim").
 */
@Composable
fun UserAvatarView(
    avatarUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    fontSize: TextUnit = 20.sp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp
) {
    val cleanName = name.trim().ifEmpty { "Student" }
    val initialChar = cleanName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val safeAvatarUrl = remember(avatarUrl) {
        avatarUrl?.trim()?.takeIf { it.isNotBlank() }?.replace("http://", "https://")
    }

    val gradientBrush = remember(cleanName) {
        val hash = cleanName.hashCode()
        when (kotlin.math.abs(hash) % 4) {
            0 -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5))) // Indigo
            1 -> Brush.linearGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))) // Blue
            2 -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))) // Purple
            else -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF0284C7))) // Sky Blue
        }
    }

    val context = LocalContext.current
    val imageRequest = remember(safeAvatarUrl) {
        if (!safeAvatarUrl.isNullOrBlank()) {
            ImageRequest.Builder(context)
                .data(safeAvatarUrl)
                .crossfade(true)
                .build()
        } else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) Modifier.border(borderWidth, borderColor, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageRequest != null) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    FallbackLetterAvatar(
                        initialChar = initialChar,
                        gradientBrush = gradientBrush,
                        fontSize = fontSize
                    )
                },
                error = {
                    FallbackLetterAvatar(
                        initialChar = initialChar,
                        gradientBrush = gradientBrush,
                        fontSize = fontSize
                    )
                }
            )
        } else {
            FallbackLetterAvatar(
                initialChar = initialChar,
                gradientBrush = gradientBrush,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun FallbackLetterAvatar(
    initialChar: String,
    gradientBrush: Brush,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initialChar,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
