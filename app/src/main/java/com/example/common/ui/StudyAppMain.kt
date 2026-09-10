package com.example.common.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.common.model.LectureItem
import com.example.common.navigation.ModernAdaptiveNavigationRail
import com.example.common.navigation.ModernFloatingNavigationBar
import com.example.common.navigation.StudyDestination
import com.example.common.ui.screens.AdmissionInfoScreen
import com.example.common.ui.screens.ChapterDetailScreen
import com.example.common.ui.screens.ChapterListScreen
import com.example.common.ui.screens.CoursesScreen
import com.example.common.ui.screens.ExploreScreen
import com.example.common.ui.screens.HomeScreen
import com.example.common.ui.screens.LecturePlayerScreen
import com.example.common.ui.screens.LoginScreen
import com.example.common.ui.screens.MyCoursesScreen
import com.example.common.ui.screens.ProfileEditScreen
import com.example.common.ui.screens.ProfileScreen
import com.example.common.ui.screens.QuarterDetailScreen
import com.example.common.ui.screens.ShikhoAiScreen
import com.example.common.ui.screens.SubjectDetailScreen
import com.example.common.ui.screens.SyllabusChangeScreen
import com.example.common.ui.screens.SyllabusSelectionScreen
import com.example.common.ui.screens.SyllabusSummaryScreen
import com.example.common.viewmodel.MyCoursesViewModel
import com.example.common.viewmodel.SyllabusViewModel

/**
 * Screen navigation states representing root tabs and detail screens:
 * - MainTabs (Bottom nav active)
 * - ProfileSettings (Accessible from AI top bar or Home header avatar)
 * - EditProfile (Step 1-3 Personal, Institutional, Guardian profile editing)
 * - SyllabusChange / SyllabusSummary (Overview card and Class/Batch/Group profile)
 * - SyllabusSelection (Dynamic reactive Class -> Batch -> Group selection flow)
 * - MyCourses (Dynamic reactive course feed bound to syllabus)
 * - AdmissionInfo (User requested enrolled course list matching Screenshots 1-4)
 * - Login (KMP Auth Login screen with Ktor API service)
 * - QuarterDetail, SubjectDetail, ChapterDetail, LecturePlayer
 */
sealed interface ActiveScreenState {
    data object MainTabs : ActiveScreenState
    data object ProfileSettings : ActiveScreenState
    data object EditProfile : ActiveScreenState
    data object SyllabusChange : ActiveScreenState
    data object SyllabusSummary : ActiveScreenState
    data object SyllabusSelection : ActiveScreenState
    data object MyCourses : ActiveScreenState
    data object AdmissionInfo : ActiveScreenState
    data object Login : ActiveScreenState
    data class QuarterDetail(val courseTitle: String) : ActiveScreenState
    data class SubjectDetail(val subjectTitle: String) : ActiveScreenState
    data class ChapterList(val subjectTitle: String = "বাংলা ১ম পত্র", val subjectId: String = "609130954") : ActiveScreenState
    data class ChapterDetail(
        val chapterId: String = "ch_03",
        val chapterTitle: String,
        val statusBadge: String = "পড়ানো শেষ",
        val programId: String = "6864d3a806800acba2e27099",
        val phaseId: String = "6864d4e806800acba2e270e1",
        val subjectId: String = "609130954"
    ) : ActiveScreenState
    data class LecturePlayer(val lecture: LectureItem, val subjectTitle: String = "বাংলা ১ম পত্র") : ActiveScreenState
}

/**
 * The core shared App UI container designed following Compose Multiplatform patterns.
 * Adaptively handles Mobile phone screens and Desktop/Tablet screens.
 */
@Composable
fun StudyAppMain(
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.example.common.network.UserSessionManager.init(context)
    }

    val userProfile = com.example.common.network.UserSessionManager.currentUserProfile
    val isLoggedIn = userProfile.isLoggedIn

    if (!isLoggedIn) {
        LoginScreen(
            onBackClick = null,
            onLoginSuccess = {
                // Session is now authenticated and populated
            }
        )
        return
    }

    val syllabusViewModel: SyllabusViewModel = remember {
        val db = com.example.common.data.local.AppDatabase.getInstance(context)
        val networkSource = com.example.common.data.remote.AcademicNetworkDataSource()
        val repo = com.example.common.repository.AcademicRepository.getInstance(
            networkDataSource = networkSource,
            profileDao = db.userAcademicProfileDao(),
            configDao = db.cachedAcademicConfigDao()
        )
        SyllabusViewModel(repo)
    }

    val myCoursesViewModel: MyCoursesViewModel = remember {
        val db = com.example.common.data.local.AppDatabase.getInstance(context)
        val apiService = com.example.common.data.remote.CourseApiClientFactory.createService()
        val repo = com.example.common.repository.CourseRepositoryImpl.getInstance(
            apiService = apiService,
            cachedMyCoursesDao = db.cachedMyCoursesDao(),
            profileDao = db.userAcademicProfileDao()
        )
        MyCoursesViewModel(repo)
    }

    var currentDestination by rememberSaveable { mutableStateOf(StudyDestination.HOME) }
    var screenBackstack by remember { mutableStateOf(listOf<ActiveScreenState>(ActiveScreenState.MainTabs)) }

    val currentScreen = screenBackstack.last()
    val canGoBack = screenBackstack.size > 1

    val navigateBack: () -> Unit = {
        if (canGoBack) {
            screenBackstack = screenBackstack.dropLast(1)
        }
    }

    // Android Hardware/System Back Button
    BackHandler(enabled = canGoBack) {
        navigateBack()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Desktop / Laptop / Tablet Layout: Side Navigation Rail + Content Area
            Row(modifier = Modifier.fillMaxSize()) {
                if (currentScreen is ActiveScreenState.MainTabs) {
                    ModernAdaptiveNavigationRail(
                        currentDestination = currentDestination,
                        onDestinationSelected = { currentDestination = it },
                        modifier = Modifier.statusBarsPadding()
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    RenderScreen(
                        currentScreen = currentScreen,
                        currentDestination = currentDestination,
                        syllabusViewModel = syllabusViewModel,
                        myCoursesViewModel = myCoursesViewModel,
                        onNavigate = { newScreen -> screenBackstack = screenBackstack + newScreen },
                        onNavigateBack = navigateBack
                    )
                }
            }
        } else {
            // Mobile Layout: Full-bleed Screen Content with Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF070E2F))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    RenderScreen(
                        currentScreen = currentScreen,
                        currentDestination = currentDestination,
                        syllabusViewModel = syllabusViewModel,
                        myCoursesViewModel = myCoursesViewModel,
                        onNavigate = { newScreen -> screenBackstack = screenBackstack + newScreen },
                        onNavigateBack = navigateBack
                    )
                }

                // Show floating bottom navigation bar only when on main tabs
                if (currentScreen is ActiveScreenState.MainTabs) {
                    ModernFloatingNavigationBar(
                        currentDestination = currentDestination,
                        onDestinationSelected = { currentDestination = it },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderScreen(
    currentScreen: ActiveScreenState,
    currentDestination: StudyDestination,
    syllabusViewModel: SyllabusViewModel,
    myCoursesViewModel: MyCoursesViewModel,
    onNavigate: (ActiveScreenState) -> Unit,
    onNavigateBack: () -> Unit
) {
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(180))
        },
        label = "active_screen_transition"
    ) { screen ->
        when (screen) {
            is ActiveScreenState.MainTabs -> {
                Crossfade(
                    targetState = currentDestination,
                    animationSpec = tween(durationMillis = 200),
                    label = "main_tab_crossfade"
                ) { destination ->
                    when (destination) {
                        StudyDestination.HOME -> HomeScreen(
                            onClassClick = { classItem ->
                                val lecture = LectureItem(
                                    id = classItem.id,
                                    titleBn = classItem.chapterOrTopic,
                                    typeLabel = classItem.typeLabel,
                                    dateText = "০১ সেপ্টেম্বর ২০২৬",
                                    statusBadge = "মিসড",
                                    instructorName = classItem.instructor
                                )
                                onNavigate(ActiveScreenState.LecturePlayer(lecture, classItem.subject))
                            },
                            onProfileClick = {
                                onNavigate(ActiveScreenState.ProfileSettings)
                            },
                            onEditProfileClick = {
                                onNavigate(ActiveScreenState.EditProfile)
                            },
                            onSyllabusChangeClick = {
                                onNavigate(ActiveScreenState.SyllabusChange)
                            },
                            onAdmissionInfoClick = {
                                onNavigate(ActiveScreenState.AdmissionInfo)
                            },
                            onLoginClick = {
                                onNavigate(ActiveScreenState.Login)
                            }
                        )

                        StudyDestination.EXPLORE -> ExploreScreen(
                            subjects = com.example.common.network.SyllabusDatabaseManager.getSubjectsForProfile(
                                com.example.common.network.UserSessionManager.currentUserProfile
                            ),
                            onSubjectClick = { subject ->
                                onNavigate(ActiveScreenState.SubjectDetail(subject.titleBn))
                            }
                        )

                        StudyDestination.COURSES -> MyCoursesScreen(
                            viewModel = myCoursesViewModel,
                            onCourseClick = { courseId ->
                                onNavigate(ActiveScreenState.QuarterDetail("কোর্স বিস্তারিত"))
                            },
                            onContinueLearning = { courseId ->
                                onNavigate(ActiveScreenState.ChapterList())
                            },
                            onViewDetails = { courseId ->
                                onNavigate(ActiveScreenState.AdmissionInfo)
                            },
                            onEnrollCourse = { courseId ->
                                onNavigate(ActiveScreenState.AdmissionInfo)
                            },
                            onChangeSyllabusClick = {
                                onNavigate(ActiveScreenState.SyllabusSelection)
                            },
                            onBackClick = onNavigateBack
                        )

                        // 4th Tab: Interactive AI Tutor
                        StudyDestination.AI_TUTOR -> ShikhoAiScreen(
                            onOpenProfileSettings = {
                                onNavigate(ActiveScreenState.ProfileSettings)
                            }
                        )

                        // 5th Tab: Full Profile & Settings
                        StudyDestination.PROFILE -> ProfileScreen(
                            onBackClick = null,
                            onEditProfileClick = {
                                onNavigate(ActiveScreenState.EditProfile)
                            },
                            onSyllabusChangeClick = {
                                onNavigate(ActiveScreenState.SyllabusChange)
                            },
                            onAdmissionInfoClick = {
                                onNavigate(ActiveScreenState.AdmissionInfo)
                            },
                            onLoginClick = {
                                onNavigate(ActiveScreenState.Login)
                            }
                        )
                    }
                }
            }

            // Profile + Settings Screen (accessed via sub-navigation like AI header button or Home header avatar)
            is ActiveScreenState.ProfileSettings -> {
                ProfileScreen(
                    onBackClick = onNavigateBack,
                    onEditProfileClick = {
                        onNavigate(ActiveScreenState.EditProfile)
                    },
                    onSyllabusChangeClick = {
                        onNavigate(ActiveScreenState.SyllabusChange)
                    },
                    onAdmissionInfoClick = {
                        onNavigate(ActiveScreenState.AdmissionInfo)
                    },
                    onLoginClick = {
                        onNavigate(ActiveScreenState.Login)
                    }
                )
            }

            // User requested 3-step Profile Edit Flow (Screenshots 1 - 5)
            is ActiveScreenState.EditProfile -> {
                ProfileEditScreen(
                    onBackClick = onNavigateBack,
                    onSaveSuccess = onNavigateBack
                )
            }

            // Academic Syllabus Summary View & Selection Flow
            is ActiveScreenState.SyllabusChange, is ActiveScreenState.SyllabusSummary -> {
                SyllabusSummaryScreen(
                    viewModel = syllabusViewModel,
                    onBackClick = onNavigateBack,
                    onEditClick = {
                        onNavigate(ActiveScreenState.SyllabusSelection)
                    }
                )
            }

            is ActiveScreenState.SyllabusSelection -> {
                SyllabusSelectionScreen(
                    viewModel = syllabusViewModel,
                    onBackClick = onNavigateBack,
                    onSubmitSuccess = onNavigateBack
                )
            }

            is ActiveScreenState.MyCourses -> {
                MyCoursesScreen(
                    viewModel = myCoursesViewModel,
                    onCourseClick = { courseId ->
                        onNavigate(ActiveScreenState.QuarterDetail("কোর্স বিস্তারিত"))
                    },
                    onContinueLearning = { courseId ->
                        onNavigate(ActiveScreenState.ChapterList())
                    },
                    onViewDetails = { courseId ->
                        onNavigate(ActiveScreenState.AdmissionInfo)
                    },
                    onEnrollCourse = { courseId ->
                        onNavigate(ActiveScreenState.AdmissionInfo)
                    },
                    onChangeSyllabusClick = {
                        onNavigate(ActiveScreenState.SyllabusSelection)
                    },
                    onBackClick = onNavigateBack
                )
            }

            // User requested Admission Info Flow (Screenshots 1 - 4: "ভর্তি সম্পর্কিত তথ্য" / "কোর্সে ভর্তি")
            is ActiveScreenState.AdmissionInfo -> {
                AdmissionInfoScreen(
                    onBackClick = onNavigateBack,
                    onNavigateToQuarter = { courseTitle ->
                        onNavigate(ActiveScreenState.QuarterDetail(courseTitle))
                    }
                )
            }

            // User requested KMP Auth Login Screen with Ktor API Service
            is ActiveScreenState.Login -> {
                LoginScreen(
                    onBackClick = onNavigateBack,
                    onLoginSuccess = { token ->
                        // Automatically navigate back or stay as confirmed
                    }
                )
            }

            // Screenshot 3: Quarter Detail Screen
            is ActiveScreenState.QuarterDetail -> {
                QuarterDetailScreen(
                    courseTitle = screen.courseTitle,
                    onBackClick = onNavigateBack,
                    onSubjectClick = { subject ->
                        onNavigate(ActiveScreenState.SubjectDetail(subject.titleBn))
                    }
                )
            }

            // Screenshot 4: Subject Detail Screen
            is ActiveScreenState.SubjectDetail -> {
                SubjectDetailScreen(
                    subjectTitle = screen.subjectTitle,
                    onBackClick = onNavigateBack,
                    onChapterClick = { chapter ->
                        onNavigate(
                            ActiveScreenState.ChapterDetail(
                                chapterId = chapter.id,
                                chapterTitle = chapter.titleBn,
                                statusBadge = chapter.statusBadge
                            )
                        )
                    }
                )
            }

            // Academic Chapter List Screen (GraphQL PhaseWiseChapters)
            is ActiveScreenState.ChapterList -> {
                ChapterListScreen(
                    subjectTitle = screen.subjectTitle,
                    subjectId = screen.subjectId,
                    onBackClick = onNavigateBack,
                    onChapterClick = { gqlChapter ->
                        onNavigate(
                            ActiveScreenState.ChapterDetail(
                                chapterId = gqlChapter.chapterId,
                                chapterTitle = gqlChapter.chapterName,
                                statusBadge = gqlChapter.statusBadgeBn
                            )
                        )
                    }
                )
            }

            // Screenshot 5: Chapter Detail Screen (3 Tabs: Classes & Lectures, Exams, Resources)
            is ActiveScreenState.ChapterDetail -> {
                ChapterDetailScreen(
                    chapterId = screen.chapterId,
                    chapterTitle = screen.chapterTitle,
                    statusBadge = screen.statusBadge,
                    programId = screen.programId,
                    phaseId = screen.phaseId,
                    subjectId = screen.subjectId,
                    onBackClick = onNavigateBack,
                    onLectureClick = { lecture ->
                        onNavigate(ActiveScreenState.LecturePlayer(lecture))
                    }
                )
            }

            // Screenshot 6: Lecture Video & Resources Player Screen
            is ActiveScreenState.LecturePlayer -> {
                LecturePlayerScreen(
                    lecture = screen.lecture,
                    subjectTitle = screen.subjectTitle,
                    onBackClick = onNavigateBack
                )
            }
        }
    }
}
