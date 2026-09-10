package com.example.common.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.common.ui.components.UserAvatarView
import com.example.common.model.UserProfile
import com.example.common.network.AuthService
import com.example.common.network.LoginResult
import com.example.common.network.SendSmsResult
import com.example.common.network.UserCheckResult
import com.example.common.network.UserSessionManager
import com.example.common.network.VerifyOtpResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 2-Step Dynamic Authentication State Machine
 */
sealed interface AuthState {
    data object EnterPhone : AuthState
    data class EnterPin(val phone: String) : AuthState
    data class EnterOtp(val phone: String, val message: String? = null) : AuthState
    data class Loading(val message: String, val previousState: AuthState) : AuthState
    data class Success(val profile: UserProfile, val accessToken: String) : AuthState
    data class Error(val message: String, val returnState: AuthState) : AuthState
}

/**
 * Modern & Responsive 2-Step Dynamic Authentication Screen (Compose Multiplatform):
 * - Step 1: POST https://api.shikho.com/auth/v2/user/check
 *           If status 200 and pin_exist == true -> EnterPin
 *           If status 404 or pin_exist == false -> Auto call Send SMS -> EnterOtp
 * - Step 2-A: Existing User -> POST https://api.shikho.com/auth/v2/login
 * - Step 2-B: New User / OTP -> POST https://api.shikho.com/auth/v2/verify/otp
 * - Step 3: AuthRepository Bearer token storage & UserSessionManager.currentUserProfile sync.
 */
@Composable
fun LoginScreen(
    onBackClick: (() -> Unit)? = null,
    onLoginSuccess: (accessToken: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val authService = remember { AuthService() }

    // State machine
    var authState by remember { mutableStateOf<AuthState>(AuthState.EnterPhone) }

    // Input States
    var phoneNumberState by remember { mutableStateOf("") }
    var pinCodeState by remember { mutableStateOf("") }
    var otpCodeState by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }

    // OTP Resend cooldown timer
    var resendCooldown by remember { mutableIntStateOf(60) }

    LaunchedEffect(authState) {
        if (authState is AuthState.EnterOtp) {
            resendCooldown = 60
            while (resendCooldown > 0) {
                delay(1000L)
                resendCooldown--
            }
        }
    }

    // Handlers for step transitions
    fun onCheckUserSubmit() {
        focusManager.clearFocus()
        val phone = phoneNumberState.trim()
        if (phone.length < 11) return

        val previous = AuthState.EnterPhone
        authState = AuthState.Loading(message = "ইউজার অস্তিত্ব যাচাই করা হচ্ছে...", previousState = previous)

        coroutineScope.launch {
            when (val result = authService.checkUser(phone)) {
                is UserCheckResult.ExistingUser -> {
                    authState = AuthState.EnterPin(phone = phone)
                }
                is UserCheckResult.NewUser -> {
                    // New user or no PIN: automatically send SMS OTP
                    authState = AuthState.Loading(message = "ওটিপি পাঠানো হচ্ছে...", previousState = previous)
                    when (val smsResult = authService.sendSms(phone)) {
                        is SendSmsResult.Success -> {
                            authState = AuthState.EnterOtp(
                                phone = phone,
                                message = smsResult.message
                            )
                        }
                        is SendSmsResult.Error -> {
                            authState = AuthState.Error(
                                message = smsResult.message,
                                returnState = AuthState.EnterPhone
                            )
                        }
                    }
                }
                is UserCheckResult.Error -> {
                    authState = AuthState.Error(
                        message = result.message,
                        returnState = AuthState.EnterPhone
                    )
                }
            }
        }
    }

    fun onPinLoginSubmit(phone: String) {
        focusManager.clearFocus()
        val pin = pinCodeState.trim()
        if (pin.isEmpty()) return

        val previous = AuthState.EnterPin(phone)
        authState = AuthState.Loading(message = "লগইন সম্পন্ন করা হচ্ছে...", previousState = previous)

        coroutineScope.launch {
            when (val result = authService.login(phoneNumber = phone, otpOrPassword = pin)) {
                is LoginResult.Success -> {
                    val profile = (result.userProfile ?: UserSessionManager.currentUserProfile).copy(isLoggedIn = true)
                    UserSessionManager.saveSession(
                        accessToken = result.accessToken,
                        profile = profile
                    )
                    UserSessionManager.enrolledCourses = com.example.common.network.SyllabusDatabaseManager.getEnrolledCoursesForProfile(profile)
                    com.example.common.viewmodel.CourseViewModel.shared.loadAcademicPrograms()
                    authState = AuthState.Success(profile = profile, accessToken = result.accessToken)
                    onLoginSuccess(result.accessToken)
                }
                is LoginResult.Error -> {
                    authState = AuthState.Error(
                        message = result.message,
                        returnState = AuthState.EnterPin(phone)
                    )
                }
            }
        }
    }

    fun onVerifyOtpSubmit(phone: String) {
        focusManager.clearFocus()
        val otp = otpCodeState.trim()
        if (otp.length < 4) return

        val previous = AuthState.EnterOtp(phone)
        authState = AuthState.Loading(message = "ওটিপি যাচাই করা হচ্ছে...", previousState = previous)

        coroutineScope.launch {
            when (val result = authService.verifyOtp(phoneNumber = phone, otpInput = otp)) {
                is VerifyOtpResult.Success -> {
                    val profile = (result.userProfile ?: UserSessionManager.currentUserProfile).copy(isLoggedIn = true)
                    UserSessionManager.saveSession(
                        accessToken = result.accessToken,
                        profile = profile
                    )
                    UserSessionManager.enrolledCourses = com.example.common.network.SyllabusDatabaseManager.getEnrolledCoursesForProfile(profile)
                    com.example.common.viewmodel.CourseViewModel.shared.loadAcademicPrograms()
                    authState = AuthState.Success(profile = profile, accessToken = result.accessToken)
                    onLoginSuccess(result.accessToken)
                }
                is VerifyOtpResult.Error -> {
                    authState = AuthState.Error(
                        message = result.message,
                        returnState = AuthState.EnterOtp(phone)
                    )
                }
            }
        }
    }

    fun onSwitchToOtp(phone: String) {
        val previous = AuthState.EnterPin(phone)
        authState = AuthState.Loading(message = "ওটিপি পাঠানো হচ্ছে...", previousState = previous)
        coroutineScope.launch {
            when (val smsResult = authService.sendSms(phone)) {
                is SendSmsResult.Success -> {
                    authState = AuthState.EnterOtp(phone = phone, message = smsResult.message)
                }
                is SendSmsResult.Error -> {
                    authState = AuthState.Error(message = smsResult.message, returnState = AuthState.EnterPin(phone))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (onBackClick != null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp,
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
                            onClick = onBackClick,
                            modifier = Modifier.testTag("login_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ফিরে যান",
                                tint = Color(0xFF1E293B)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "অ্যাকাউন্টে প্রবেশ",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = Color(0xFF64748B).copy(alpha = 0.12f)
                    )
                    .testTag("auth_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = authState,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "auth_state_transition"
                    ) { state ->
                        when (state) {
                            is AuthState.EnterPhone -> {
                                PhoneInputContent(
                                    phoneNumber = phoneNumberState,
                                    onPhoneChange = { input ->
                                        // Allow only digits, max 11 digits
                                        val filtered = input.filter { it.isDigit() }
                                        if (filtered.length <= 11) {
                                            phoneNumberState = filtered
                                        }
                                    },
                                    onSubmit = { onCheckUserSubmit() }
                                )
                            }
                            is AuthState.EnterPin -> {
                                PinInputContent(
                                    phone = state.phone,
                                    pin = pinCodeState,
                                    isPinVisible = isPinVisible,
                                    onPinChange = { pinCodeState = it },
                                    onToggleVisibility = { isPinVisible = !isPinVisible },
                                    onSubmit = { onPinLoginSubmit(state.phone) },
                                    onChangePhone = { authState = AuthState.EnterPhone },
                                    onLoginWithOtp = { onSwitchToOtp(state.phone) }
                                )
                            }
                            is AuthState.EnterOtp -> {
                                OtpInputContent(
                                    phone = state.phone,
                                    infoMessage = state.message,
                                    otp = otpCodeState,
                                    resendCooldown = resendCooldown,
                                    onOtpChange = { input ->
                                        val filtered = input.filter { it.isDigit() }
                                        if (filtered.length <= 6) {
                                            otpCodeState = filtered
                                        }
                                    },
                                    onSubmit = { onVerifyOtpSubmit(state.phone) },
                                    onResend = {
                                        coroutineScope.launch {
                                            resendCooldown = 60
                                            authService.sendSms(state.phone)
                                        }
                                    },
                                    onChangePhone = { authState = AuthState.EnterPhone }
                                )
                            }
                            is AuthState.Loading -> {
                                LoadingStateContent(message = state.message)
                            }
                            is AuthState.Success -> {
                                SuccessStateContent(
                                    profile = state.profile,
                                    accessToken = state.accessToken,
                                    onProceed = { onBackClick?.invoke() }
                                )
                            }
                            is AuthState.Error -> {
                                ErrorStateContent(
                                    message = state.message,
                                    onRetry = { authState = state.returnState }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 1: Phone number input content
 */
@Composable
private fun PhoneInputContent(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "স্বাগতম!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "আপনার ১১ ডিজিটের মোবাইল নম্বর প্রদান করে এগিয়ে যান",
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phone_number_input"),
            label = { Text("মোবাইল নম্বর") },
            placeholder = { Text("01XXXXXXXXX") },
            leadingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+88",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "|",
                        color = Color(0xFFCBD5E1),
                        fontSize = 14.sp
                    )
                }
            },
            trailingIcon = {
                if (phoneNumber.isNotEmpty()) {
                    IconButton(onClick = { onPhoneChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "মুছে ফেলুন",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { if (phoneNumber.length == 11) onSubmit() }
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedLabelColor = Color(0xFF4F46E5),
                cursorColor = Color(0xFF4F46E5)
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        val isValid = phoneNumber.trim().length == 11
        Button(
            onClick = onSubmit,
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_next_phone"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5),
                disabledContainerColor = Color(0xFFE2E8F0),
                contentColor = Color.White,
                disabledContentColor = Color(0xFF94A3B8)
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "পরবর্তী",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Real security & privacy assurance note
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "আপনার মোবাইল নম্বরটি সুরক্ষিত এবং শুধুমাত্র ওটিপি ও অথেনটিকেশনের জন্য ব্যবহৃত হবে।",
                    fontSize = 12.sp,
                    color = Color(0xFF475569),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Step 2-A: Existing User PIN Login Content
 */
@Composable
private fun PinInputContent(
    phone: String,
    pin: String,
    isPinVisible: Boolean,
    onPinChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onChangePhone: () -> Unit,
    onLoginWithOtp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Password,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "পিন / পাসওয়ার্ড লিখুন",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Phone chip with change option
        Surface(
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+88$phone",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                    onClick = onChangePhone,
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = "পরিবর্তন",
                        fontSize = 12.sp,
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = onPinChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pin_input"),
            label = { Text("পিন / পাসওয়ার্ড") },
            placeholder = { Text("আপনার পিন নম্বর") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF4F46E5)
                )
            },
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPinVisible) "লুকান" else "দেখান",
                        tint = Color(0xFF64748B)
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (pin.isNotBlank()) onSubmit() }
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4F46E5),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedLabelColor = Color(0xFF4F46E5),
                cursorColor = Color(0xFF4F46E5)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            enabled = pin.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_submit_pin"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4F46E5),
                disabledContainerColor = Color(0xFFE2E8F0),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "লগইন করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // OTP alternative button
        OutlinedButton(
            onClick = onLoginWithOtp,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                tint = Color(0xFF4F46E5),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ওটিপি দিয়ে লগইন করুন",
                fontSize = 13.sp,
                color = Color(0xFF334155),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Step 2-B: OTP Verification Content
 */
@Composable
private fun OtpInputContent(
    phone: String,
    infoMessage: String?,
    otp: String,
    resendCooldown: Int,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onResend: () -> Unit,
    onChangePhone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF059669), Color(0xFF10B981))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ওটিপি কোড যাচাই",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "আপনার +88$phone নম্বরে পাঠানো কোডটি লিখুন",
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onChangePhone,
            modifier = Modifier.height(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFF4F46E5)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "নম্বর পরিবর্তন করুন",
                    fontSize = 12.sp,
                    color = Color(0xFF4F46E5),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!infoMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFA7F3D0))
            ) {
                Text(
                    text = infoMessage,
                    color = Color(0xFF065F46),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input"),
            label = { Text("ওটিপি কোড (৪-৬ ডিজিট)") },
            placeholder = { Text("যেমন: 1234") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Password,
                    contentDescription = null,
                    tint = Color(0xFF059669)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (otp.length >= 4) onSubmit() }
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF059669),
                unfocusedBorderColor = Color(0xFFCBD5E1),
                focusedLabelColor = Color(0xFF059669),
                cursorColor = Color(0xFF059669)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Countdown or resend button
        if (resendCooldown > 0) {
            Text(
                text = "পুনরায় ওটিপি পাঠান (${resendCooldown}s)",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
        } else {
            TextButton(onClick = onResend) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "পুনরায় ওটিপি পাঠান",
                        fontSize = 13.sp,
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onSubmit,
            enabled = otp.trim().length >= 4,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_verify_otp"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF059669),
                disabledContainerColor = Color(0xFFE2E8F0),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "যাচাই করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Loading state content
 */
@Composable
private fun LoadingStateContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF4F46E5),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF334155),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Success state content with student profile preview card
 */
@Composable
private fun SuccessStateContent(
    profile: UserProfile,
    accessToken: String,
    onProceed: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "লগইন সফল হয়েছে!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF15803D)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "আপনার প্রোফাইল সফলভাবে সংযুক্ত হয়েছে",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Student Profile Preview Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile avatar with fallback letter & gradient
                UserAvatarView(
                    avatarUrl = profile.avatarUrl,
                    name = profile.name,
                    size = 56.dp,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    if (profile.institutionName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = profile.institutionName,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${profile.studentClass} • ${profile.group}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = onProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
        ) {
            Text(
                text = "হোম পেজে যান",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Error state content
 */
@Composable
private fun ErrorStateContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFDC2626),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "লগইন ব্যর্থ হয়েছে",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF991B1B)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 13.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
        ) {
            Text(
                text = "পুনরায় চেষ্টা করুন",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
