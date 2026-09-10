package com.example.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.common.model.UserProfile
import com.example.common.network.AddressDistrict
import com.example.common.network.AddressDivision
import com.example.common.network.CourseContentUiState
import com.example.common.network.EnrollPaymentPlanItem
import com.example.common.network.GqlAcademicProgram
import com.example.common.network.GqlProgramPhase
import com.example.common.network.ProfileUpdatePayload
import com.example.common.network.SchoolItem
import com.example.common.network.ShikhoServices
import com.example.common.network.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ==========================================
// 1. SYLLABUS VIEW MODEL
// ==========================================

sealed interface SyllabusSyncUiState {
    data object Idle : SyllabusSyncUiState
    data object Loading : SyllabusSyncUiState
    data class Success(val message: String, val programs: List<GqlAcademicProgram>, val phases: List<GqlProgramPhase>) : SyllabusSyncUiState
    data class Error(val message: String) : SyllabusSyncUiState
}

class LegacySyllabusSyncViewModel : ViewModel() {
    private val _syncState = MutableStateFlow<SyllabusSyncUiState>(SyllabusSyncUiState.Idle)
    val syncState: StateFlow<SyllabusSyncUiState> = _syncState.asStateFlow()

    private val _phasesState = MutableStateFlow<List<GqlProgramPhase>>(emptyList())
    val phasesState: StateFlow<List<GqlProgramPhase>> = _phasesState.asStateFlow()

    private val _classesState = MutableStateFlow<List<com.example.common.network.ShikhoClassItem>>(emptyList())
    val classesState: StateFlow<List<com.example.common.network.ShikhoClassItem>> = _classesState.asStateFlow()

    private val _batchOptionsState = MutableStateFlow<List<com.example.common.network.BatchOption>>(emptyList())
    val batchOptionsState: StateFlow<List<com.example.common.network.BatchOption>> = _batchOptionsState.asStateFlow()

    init {
        loadClassList()
    }

    fun loadClassList() {
        viewModelScope.launch {
            val res = ShikhoServices.fetchClassList()
            val list = res.getOrDefault(emptyList())
            _classesState.value = list
        }
    }

    fun loadBatchOptions(classCode: String) {
        viewModelScope.launch {
            val res = ShikhoServices.fetchBatchOptions(classCode)
            val list = res.getOrDefault(emptyList())
            _batchOptionsState.value = list
        }
    }

    fun syncSyllabus(
        selectedClass: String,
        selectedYear: String,
        selectedGroup: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _syncState.value = SyllabusSyncUiState.Loading

            // Map class code for Shikho query
            val classCode = when {
                selectedClass.contains("১১") || selectedClass.contains("এইচএসসি") || selectedClass.contains("HSC") -> "C11"
                selectedClass.contains("১২") -> "C12"
                selectedClass.contains("১০") -> "C10"
                selectedClass.contains("৯") -> "C09"
                selectedClass.contains("৮") -> "C08"
                selectedClass.contains("৭") -> "C07"
                selectedClass.contains("৬") -> "C06"
                selectedClass.contains("৫") -> "C05"
                selectedClass.contains("এডমিশন") -> "CAD"
                else -> "C11"
            }

            val groupCode = when {
                selectedGroup.contains("মানবিক") -> "HUM"
                selectedGroup.contains("বিজ্ঞান") -> "SCI"
                selectedGroup.contains("ব্যবসায়") -> "BUS"
                else -> "GEN"
            }

            val groupName = when {
                selectedGroup.contains("মানবিক") -> "Humanities"
                selectedGroup.contains("বিজ্ঞান") -> "Science"
                selectedGroup.contains("ব্যবসায়") -> "BusinessStudies"
                else -> "General"
            }

            val rawDigits = selectedYear.filter { it.isDigit() }
            val batchYear = if (rawDigits.isNotEmpty()) rawDigits else "2027"
            val batchId = if (classCode == "C11") "HSC $batchYear" else "$selectedClass $batchYear"

            val targetProgramId = when {
                classCode == "C11" && batchYear == "2027" && groupCode == "HUM" -> "6864d3a806800acba2e27099" // HSC '27 মানবিক - ২য় বর্ষ প্রস্তুতি
                classCode == "C11" && batchYear == "2028" && groupCode == "HUM" -> "69f884bb8338867433c1be8e" // দুরন্ত HSC '28 মানবিক
                classCode == "C05" -> "c05_prog_1"
                else -> "6864d3a806800acba2e27099"
            }

            // ধাপ ১: রিয়েল সার্ভার মিউটেশন ChangeSyllabus এক্সিকিউট করা
            // এই মিউটেশনের মাধ্যমে সার্ভার থেকে নতুন JWT access_token, id_token ও refresh_token পাওয়া যায়
            ShikhoServices.changeSyllabusMutation(
                studyGroup = if (groupCode == "GEN") null else groupName,
                userClass = classCode
            )

            // ধাপ ২: রিয়েল সার্ভার মিউটেশন UpdateExamYear এক্সিকিউট করা
            ShikhoServices.updateExamYearMutation(
                passingYear = batchYear,
                type = "student"
            )

            // ধাপ ৩: সার্ভার থেকে রিয়েল ইউজার প্রোফাইল রিফ্রেশ (GetProfile)
            UserSessionManager.refreshUserProfileFromServer()

            // ধাপ ৪: ইভেন্ট-ড্রিভেন টেলিমেট্রি ও অ্যানালিটিক্স সিঙ্ক
            com.example.common.network.AnalyticsTracker.trackSyllabusChange(
                oldClass = UserSessionManager.currentUserProfile.studentClass,
                newClass = classCode,
                oldGroup = UserSessionManager.currentUserProfile.group,
                newGroup = groupName,
                examYear = batchYear,
                selectedProgramId = targetProgramId
            )

            com.example.common.network.AnalyticsTracker.trackFcmTopicSubscription(
                newClass = classCode,
                examYear = batchYear,
                group = groupName
            )

            // ধাপ ৫: নতুন সিলেবাস অনুযায়ী কোর্স ক্যাটালগ পুনর্নির্মাণ (GetAcademicProgram)
            val filterParams = com.example.common.network.AcademicProgramFilterParams(
                batchId = batchId,
                className = classCode,
                group = groupName,
                vendor = "BD"
            )

            // 1. listAcademicProgramByEnrollment
            val programsResult = ShikhoServices.getAcademicProgramsByEnrollment(classCode)
            val programs = programsResult.getOrDefault(emptyList())

            // 2. ProgramPhasesByStudent
            val phasesResult = ShikhoServices.getProgramPhasesByStudent(targetProgramId)
            val phases = phasesResult.getOrDefault(emptyList())
            _phasesState.value = phases

            // Update user profile in persistent session with newly chosen class/batch/group
            val currentProfile = UserSessionManager.currentUserProfile
            val updatedProfile = currentProfile.copy(
                studentClass = selectedClass,
                examBatch = if (selectedYear.isNotBlank()) selectedYear else batchYear,
                group = selectedGroup
            )
            UserSessionManager.saveProfile(updatedProfile)
            UserSessionManager.enrolledCourses = com.example.common.network.SyllabusDatabaseManager.getEnrolledCoursesForProfile(updatedProfile)

            // Reload CourseViewModel shared instance
            CourseViewModel.shared.loadAcademicPrograms(filterParams)

            _syncState.value = SyllabusSyncUiState.Success(
                message = "সিলেবাস সফলভাবে সার্ভারের সাথে হালনাগাদ করা হয়েছে",
                programs = programs,
                phases = phases
            )
            onSuccess()
        }
    }
}

// ==========================================
// 2. PROFILE MULTI-STEP EDIT VIEW MODEL
// ==========================================

sealed interface ProfileEditUiState {
    data object Idle : ProfileEditUiState
    data object Saving : ProfileEditUiState
    data class Success(val message: String = "প্রোফাইল সফলভাবে সংরক্ষিত হয়েছে") : ProfileEditUiState
    data class Error(val message: String) : ProfileEditUiState
}

class ProfileEditViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Idle)
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _divisions = MutableStateFlow<List<AddressDivision>>(emptyList())
    val divisions: StateFlow<List<AddressDivision>> = _divisions.asStateFlow()

    private val _districts = MutableStateFlow<List<AddressDistrict>>(emptyList())
    val districts: StateFlow<List<AddressDistrict>> = _districts.asStateFlow()

    private val _schools = MutableStateFlow<List<SchoolItem>>(emptyList())
    val schools: StateFlow<List<SchoolItem>> = _schools.asStateFlow()

    private val _isSearchingSchools = MutableStateFlow(false)
    val isSearchingSchools: StateFlow<Boolean> = _isSearchingSchools.asStateFlow()

    init {
        loadDivisions()
    }

    fun loadDivisions() {
        viewModelScope.launch {
            val result = ShikhoServices.getDivisions()
            _divisions.value = result.getOrDefault(emptyList())
        }
    }

    fun loadDistricts(divisionId: String) {
        viewModelScope.launch {
            val result = ShikhoServices.getDistricts(divisionId)
            val distList = result.getOrDefault(emptyList())
            _districts.value = distList
            _schools.value = emptyList()
        }
    }

    fun searchSchools(districtId: String, divisionId: String, query: String = "") {
        viewModelScope.launch {
            _isSearchingSchools.value = true
            try {
                val result = ShikhoServices.searchSchoolV1(
                    district = districtId,
                    division = divisionId,
                    limit = 100,
                    offset = 0,
                    name = query
                )
                _schools.value = result.getOrDefault(emptyList())
            } finally {
                _isSearchingSchools.value = false
            }
        }
    }

    fun onSchoolSelected(schoolId: String, schoolName: String = "") {
        viewModelScope.launch {
            ShikhoServices.updateUserSchool(schoolId, "student")
            val curr = UserSessionManager.currentUserProfile
            if (schoolName.isNotBlank()) {
                UserSessionManager.saveProfile(curr.copy(institutionName = schoolName, schoolId = schoolId))
            }
        }
    }

    fun updateSchool(schoolId: String, userType: String = "student", onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            ShikhoServices.updateUserSchool(schoolId, userType)
            onSuccess()
        }
    }

    fun saveCompleteProfile(
        profile: UserProfile,
        onSuccess: () -> Unit
    ) {
        val payload = ProfileUpdatePayload(
            dob = profile.birthDate,
            gender = profile.gender,
            shift = profile.classShift,
            sscBoardName = profile.sscBoard,
            hscBoardName = profile.hscBoard,
            boardRollNumber = profile.sscRoll,
            hscBoardRollNumber = profile.hscRoll,
            boardRegNumber = profile.boardRegNumber,
            otherTutoringSource = profile.otherTutoringSources,
            guardianName = profile.guardianName,
            guardianMobile = profile.guardianPhone
        )
        saveCompleteProfile(
            payload = payload,
            schoolId = profile.schoolId,
            schoolName = profile.institutionName,
            divisionName = profile.institutionDivision,
            districtName = profile.institutionDistrict,
            onSuccess = onSuccess
        )
    }

    fun saveCompleteProfile(
        payload: ProfileUpdatePayload,
        schoolId: String? = null,
        schoolName: String?,
        divisionName: String?,
        districtName: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileEditUiState.Saving
            _isSaving.value = true
            try {
                // 1. Update School if selected
                if (!schoolId.isNullOrBlank()) {
                    ShikhoServices.updateUserSchool(schoolId, "student")
                }

                // 2. Call GraphQL mutation UpdateProfileWithoutUseName
                ShikhoServices.updateProfileWithoutUserName(payload)

                // 3. Persist updated values to local session
                val curr = UserSessionManager.currentUserProfile
                val updated = curr.copy(
                    birthDate = payload.dob ?: curr.birthDate,
                    gender = payload.gender ?: curr.gender,
                    classShift = payload.shift ?: curr.classShift,
                    sscBoard = payload.sscBoardName ?: curr.sscBoard,
                    sscRoll = payload.boardRollNumber ?: curr.sscRoll,
                    hscBoard = payload.hscBoardName ?: curr.hscBoard,
                    hscRoll = payload.hscBoardRollNumber ?: curr.hscRoll,
                    boardRegNumber = payload.boardRegNumber ?: curr.boardRegNumber,
                    otherTutoringSources = payload.otherTutoringSource.ifEmpty { curr.otherTutoringSources },
                    institutionDivision = divisionName ?: curr.institutionDivision,
                    institutionDistrict = districtName ?: curr.institutionDistrict,
                    institutionName = schoolName ?: curr.institutionName,
                    schoolId = schoolId ?: curr.schoolId,
                    guardianName = payload.guardianName ?: curr.guardianName,
                    guardianPhone = payload.guardianMobile ?: curr.guardianPhone
                )
                UserSessionManager.saveProfile(updated)

                // 4. Re-fetch clean verified profile from server
                UserSessionManager.refreshUserProfileFromServer()

                _uiState.value = ProfileEditUiState.Success()
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = ProfileEditUiState.Error(e.message ?: "সংরক্ষণ ব্যর্থ হয়েছে")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

// ==========================================
// 3. ENROLLMENT & PAYMENT PLANS VIEW MODEL
// ==========================================

class EnrollmentPaymentViewModel : ViewModel() {
    private val _paymentState = MutableStateFlow<CourseContentUiState<List<EnrollPaymentPlanItem>>>(CourseContentUiState.Loading)
    val paymentState: StateFlow<CourseContentUiState<List<EnrollPaymentPlanItem>>> = _paymentState.asStateFlow()

    private val _phasesState = MutableStateFlow<List<GqlProgramPhase>>(emptyList())
    val phasesState: StateFlow<List<GqlProgramPhase>> = _phasesState.asStateFlow()

    init {
        loadPaymentPlansAndPhases()
    }

    fun loadPaymentPlansAndPhases(programId: String = "6864d3a806800acba2e27099") {
        viewModelScope.launch {
            _paymentState.value = CourseContentUiState.Loading

            // 1. Load payment installments via ListEnrollPaymentPlan
            val planResult = ShikhoServices.listEnrollPaymentPlan(programId, true)
            if (planResult.isSuccess) {
                _paymentState.value = CourseContentUiState.Success(planResult.getOrThrow())
            } else {
                _paymentState.value = CourseContentUiState.Error("পেমেন্ট তথ্য লোড করা যায়নি")
            }

            // 2. Load program phases via ProgramPhasesByStudent
            val phaseResult = ShikhoServices.getProgramPhasesByStudent(programId)
            if (phaseResult.isSuccess) {
                _phasesState.value = phaseResult.getOrThrow()
            }
        }
    }
}
