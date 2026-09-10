package com.example.common.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AiPersona(val titleBn: String, val icon: String, val tag: String) {
    GENERAL("সাধারণ সাহায্যকারী", "🤖", "সব বিষয়"),
    MATH_SOLVER("গণিত শিক্ষক", "📐", "গণিত ও সমীকরণ"),
    PHYSICS_EXPERT("পদার্থ গবেষক", "⚡", "ফিজিক্স কনসেপ্ট"),
    BANGLA_LITERATURE("বাংলা পণ্ডিত", "📚", "বাংলা সাহিত্য ও ব্যাকরণ"),
    EXAM_PREP("পরীক্ষা ট্রেইনার", "🎯", "এমসিকিউ ও সিকিউ টিপস")
}

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "এখন",
    val persona: AiPersona = AiPersona.GENERAL,
    val suggestedPrompts: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShikhoAiScreen(
    onOpenProfileSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPersona by remember { mutableStateOf(AiPersona.GENERAL) }
    var inputMessage by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "welcome_1",
                text = "হ্যালো ফাহিম! 👋 আমি তোমার ব্যক্তিগত AI স্টাডি টিউটর। এইচএসসি সিলেবাস, পদার্থবিজ্ঞান, গণিতের কঠিন সমস্যা, কিংবা বাংলা সাহিত্যের যেকোনো প্রশ্নে আমাকে নির্দ্বিধায় জিজ্ঞাসা করো!",
                isUser = false,
                timestamp = "১০:৩০ AM",
                persona = AiPersona.GENERAL,
                suggestedPrompts = listOf(
                    "ফেব্রুয়ারি ১৯৬৯ কবিতার মূলভাব ব্যাখ্যা করো",
                    "গতিবিদ্যার প্রাস সংক্রান্ত গাণিতিক সূত্র ও নিয়মাবলী",
                    "পর্যায় সারণির পর্যায়বৃত্ত ধর্মগুলোর শর্টকাট মনে রাখার উপায়"
                )
            )
        )
    }

    val quickQuestions = listOf(
        "ফেব্রুয়ারি ১৯৬৯ কবিতার মূলভাব",
        "প্রাসের সর্বোচ্চ উচ্চতা সূত্র",
        "জৈব রসায়নের মারকনিকভ নিয়ম",
        "ত্রিকোণমিতিক রূপান্তর সূত্রাবলী",
        "পরীক্ষার জন্য সময় বণ্টন কৌশল"
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val sendMessage: (String) -> Unit = { textToSend ->
        val trimmed = textToSend.trim()
        if (trimmed.isNotEmpty()) {
            val userMsgId = "user_${System.currentTimeMillis()}"
            messages.add(
                ChatMessage(
                    id = userMsgId,
                    text = trimmed,
                    isUser = true,
                    timestamp = "এখন",
                    persona = selectedPersona
                )
            )
            inputMessage = ""
            isGenerating = true

            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
                delay(1200) // Realistic interactive AI generation feel

                val aiResponseText = when {
                    trimmed.contains("ফেব্রুয়ারি", ignoreCase = true) || trimmed.contains("কবিতা", ignoreCase = true) ->
                        "📖 **'ফেব্রুয়ারি ১৯৬৯' - শামসুর রাহমান:**\n\nএই কবিতায় কবি ১৯৬৯-এর গণঅভ্যুত্থানের প্রেক্ষাপটে ১৯৫২ সালের ভাষা আন্দোলনের স্মৃতিকে একসূত্রে গেঁথেছেন। এখানে কৃষ্ণচূড়া ফুল হয়ে উঠেছে বীর শহীদদের রক্তের প্রতীক। বরকত, সালাম প্রমুখের আত্মত্যাগ কিভাবে স্বাধিকারের দাবীকে তীব্র করেছিলো তা ফুটে উঠেছে।"

                    trimmed.contains("প্রাস", ignoreCase = true) || trimmed.contains("গতিবিদ্যা", ignoreCase = true) ->
                        "⚡ **প্রাসের গতিবিদ্যা (Projectile Motion):**\n\n• সর্বোচ্চ উচ্চতা: H = (u² sin²θ) / (2g)\n• উড্ডয়নকাল: T = (2u sinθ) / g\n• সর্বাধিক অনুভূমিক পাল্লা: R = (u² sin2θ) / g (যখন θ = 45°)\n\nমনে রাখবে অনুভূমিক বরাবর কোনো ত্বরণ থাকে না (ax = 0) এবং উল্লম্ব বরাবর ত্বরণ ay = -g।"

                    trimmed.contains("মারকনিকভ", ignoreCase = true) || trimmed.contains("রসায়ন", ignoreCase = true) ->
                        "🧪 **মারকনিকভের নিয়ম (Markovnikov's Rule):**\n\nঅপ্রতিসম অ্যালকিনের সাথে অপ্রতিসম বিকারকের (যেমন HBr) যুত বিক্রিয়ায় বিকারকের ঋণাত্মক অংশটি সেই দ্বি-বন্ধনযুক্ত কার্বনে যুক্ত হয় যাতে হাইড্রোজেন সংখ্যা কম থাকে ('তেলে মাথায় তেল দেওয়া')।"

                    else ->
                        "🤖 **${selectedPersona.titleBn} বিশ্লেষণ:**\n\nতোমার প্রশ্ন: \"$trimmed\"\n\nএটি একটি চমৎকার একাডেমিক জিজ্ঞাসা! এটি আয়ত্ত করার জন্য ৩টি ধাপ অনুসরণ করো:\n১. মূল সংজ্ঞা ও কনসেপ্ট রিভিশন\n২. সম্পর্কিত বিগত ৫ বছরের বোর্ড প্রশ্ন সমাধান\n৩. নিজের ভাষায় নোট তৈরি করা। আরও কোনো সুনির্দিষ্ট উদাহরণ বা গাণিতিক টার্ম বুঝতে চাইলে বলো!"
                }

                messages.add(
                    ChatMessage(
                        id = "ai_${System.currentTimeMillis()}",
                        text = aiResponseText,
                        isUser = false,
                        timestamp = "এখন",
                        persona = selectedPersona,
                        suggestedPrompts = listOf("আরও বিস্তারিত ব্যাখ্যা করো", "একটি গাণিতিক উদাহরণ দাও")
                    )
                )
                isGenerating = false
                delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFF070E2F),
                tonalElevation = 6.dp,
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
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "MaxBird AI টিউটর",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ONLINE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "তোমার সার্বক্ষণিক স্মার্ট স্টাডি সঙ্গী",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Right: Settings & Full Profile Options Button
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onOpenProfileSettings)
                                .testTag("ai_profile_settings_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "প্রোফাইল সেটিংস",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "প্রোফাইল",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persona Selector
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(AiPersona.values()) { persona ->
                            val isSelected = persona == selectedPersona
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) Color(0xFF4F46E5) else Color.White.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedPersona = persona }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = persona.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = persona.titleBn,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFFCBD5E1)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            // Chat Input Bar
            Surface(
                color = Color.White,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 85.dp) // space for floating bottom bar
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Quick prompts row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        items(quickQuestions) { question ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { sendMessage(question) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = question,
                                        fontSize = 11.sp,
                                        color = Color(0xFF334155),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Input Field and Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputMessage,
                            onValueChange = { inputMessage = it },
                            placeholder = {
                                Text(
                                    text = "প্রশ্নটি লিখুন বা টপিক বলুন...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4F46E5),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage(inputMessage) }),
                            maxLines = 3,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_chat_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { sendMessage(inputMessage) },
                            enabled = inputMessage.isNotBlank() && !isGenerating,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (inputMessage.isNotBlank()) Color(0xFF4F46E5) else Color(0xFFE2E8F0)
                                )
                                .testTag("ai_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "পাঠান",
                                tint = if (inputMessage.isNotBlank()) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    onPromptClick = { prompt -> sendMessage(prompt) }
                )
            }

            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF4F46E5),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "উওর তৈরি হচ্ছে...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onPromptClick: (String) -> Unit
) {
    val isUser = message.isUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = message.persona.icon, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) Color(0xFF4F46E5) else Color.White,
                shadowElevation = if (isUser) 0.dp else 2.dp,
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (!isUser) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = message.persona.titleBn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6366F1)
                            )
                        }
                    }

                    Text(
                        text = message.text,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = if (isUser) Color.White else Color(0xFF1E293B),
                        fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                    )

                    // Suggested Follow-up Prompts
                    if (message.suggestedPrompts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            message.suggestedPrompts.forEach { prompt ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEEF2FF),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onPromptClick(prompt) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = prompt,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF4F46E5)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
