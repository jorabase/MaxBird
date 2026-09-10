package com.example.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.common.data.local.UserAcademicProfileEntity
import com.example.common.model.AcademicClass
import com.example.common.model.AcademicConfigResponse
import com.example.common.model.AcademicGroup
import com.example.common.repository.AcademicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Screen modes for Syllabus Module
 */
enum class SyllabusScreenMode {
    SELECTION,
    SUMMARY
}

/**
 * UI State for Syllabus Selection and Summary screens
 * Server-Driven State Machine:
 * - selectedClass: AcademicClass?
 * - selectedBatch: String?
 * - selectedGroup: AcademicGroup?
 * - isSubmitEnabled: Boolean (dynamic validation based on selected class lists)
 */
data class SyllabusUiState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val config: AcademicConfigResponse? = null,
    val savedProfile: UserAcademicProfileEntity? = null,
    val selectedClass: AcademicClass? = null,
    val selectedBatch: String? = null,
    val selectedGroup: AcademicGroup? = null,
    val screenMode: SyllabusScreenMode = SyllabusScreenMode.SUMMARY
) {
    /**
     * Fully dynamic validation logic (100% Server-Driven, no hardcoding):
     * A submission is valid if:
     * 1. A class is selected.
     * 2. If the selected class has batches, a batch year must be selected.
     * 3. If the selected class has groups, a group must be selected.
     */
    val isSubmitEnabled: Boolean
        get() {
            if (isSubmitting) return false
            val currentClass = selectedClass ?: return false
            return (currentClass.batches.isEmpty() || selectedBatch != null) &&
                   (currentClass.groups.isEmpty() || selectedGroup != null)
        }
}

/**
 * ViewModel managing dynamic reactive state for Syllabus Selection & Summary
 */
class SyllabusViewModel(
    private val repository: AcademicRepository,
    private val userId: String = "usr_101"
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeActiveSyllabus()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val configResult = repository.getAcademicConfig()
            val existingProfile = repository.getActiveSyllabus(userId)

            configResult.fold(
                onSuccess = { config ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            config = config,
                            savedProfile = existingProfile,
                            screenMode = if (existingProfile != null) SyllabusScreenMode.SUMMARY else SyllabusScreenMode.SELECTION
                        )
                    }
                    if (existingProfile != null) {
                        applyProfileToSelection(existingProfile, config)
                    } else if (config.classes.isNotEmpty()) {
                        val initialClass = config.classes.first()
                        onClassSelected(initialClass)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "কনফিগারেশন লোড করা সম্ভব হয়নি।"
                        )
                    }
                }
            )
        }
    }

    private fun observeActiveSyllabus() {
        viewModelScope.launch {
            repository.observeActiveSyllabus(userId).collect { profile ->
                _uiState.update { it.copy(savedProfile = profile) }
            }
        }
    }

    /**
     * Handles academic class selection.
     * Dynamically sets or resets batches and groups based purely on the class object's internal lists.
     */
    fun onClassSelected(classItem: AcademicClass) {
        _uiState.update { state ->
            val nextBatch = if (classItem.batches.isNotEmpty()) {
                if (state.selectedBatch != null && classItem.batches.contains(state.selectedBatch)) {
                    state.selectedBatch
                } else {
                    classItem.batches.firstOrNull()
                }
            } else {
                null
            }

            val nextGroup = if (classItem.groups.isNotEmpty()) {
                if (state.selectedGroup != null && classItem.groups.any { it.code == state.selectedGroup.code }) {
                    classItem.groups.find { it.code == state.selectedGroup.code }
                } else {
                    classItem.groups.firstOrNull()
                }
            } else {
                null
            }

            state.copy(
                selectedClass = classItem,
                selectedBatch = nextBatch,
                selectedGroup = nextGroup,
                errorMessage = null
            )
        }
    }

    fun onClassSelect(classItem: AcademicClass) = onClassSelected(classItem)

    fun onBatchSelected(batch: String) {
        _uiState.update { it.copy(selectedBatch = batch, errorMessage = null) }
    }

    fun onBatchSelect(batch: String) = onBatchSelected(batch)

    fun onGroupSelected(group: AcademicGroup) {
        _uiState.update { it.copy(selectedGroup = group, errorMessage = null) }
    }

    fun onGroupSelect(group: AcademicGroup) = onGroupSelected(group)

    fun switchToSelectionMode() {
        val currentProfile = _uiState.value.savedProfile
        val config = _uiState.value.config
        if (currentProfile != null && config != null) {
            applyProfileToSelection(currentProfile, config)
        }
        _uiState.update { it.copy(screenMode = SyllabusScreenMode.SELECTION) }
    }

    fun switchToSummaryMode() {
        _uiState.update { it.copy(screenMode = SyllabusScreenMode.SUMMARY) }
    }

    /**
     * Pre-selects user profile data so that:
     * - selectedClass = classes.find { it.id == userProfile.classId }
     * - selectedBatch = userProfile.batchYear (if matching batch exists in selectedClass.batches)
     * - selectedGroup = selectedClass.groups.find { it.code == userProfile.groupCode }
     */
    private fun applyProfileToSelection(profile: UserAcademicProfileEntity, config: AcademicConfigResponse) {
        val matchedClass = config.classes.find { 
            it.id.equals(profile.classId, ignoreCase = true) ||
            it.titleBn.equals(profile.classTitleBn, ignoreCase = true)
        } ?: config.classes.firstOrNull()

        val matchedBatch = if (matchedClass != null && matchedClass.batches.isNotEmpty()) {
            val userBatch = profile.batchYear
            if (userBatch != null) {
                val banglaDigits = userBatch.map { char ->
                    when (char) {
                        '0' -> '০'; '1' -> '১'; '2' -> '২'; '3' -> '৩'; '4' -> '৪'
                        '5' -> '৫'; '6' -> '৬'; '7' -> '৭'; '8' -> '৮'; '9' -> '৯'
                        else -> char
                    }
                }.joinToString("")
                val englishDigits = userBatch.map { char ->
                    when (char) {
                        '০' -> '0'; '১' -> '1'; '২' -> '2'; '৩' -> '3'; '৪' -> '4'
                        '৫' -> '5'; '৬' -> '6'; '৭' -> '7'; '৮' -> '8'; '৯' -> '9'
                        else -> char
                    }
                }.joinToString("")
                matchedClass.batches.find { b -> 
                    b == userBatch || b == banglaDigits || b == englishDigits || b.contains(userBatch) || userBatch.contains(b) 
                } ?: matchedClass.batches.firstOrNull()
            } else {
                matchedClass.batches.firstOrNull()
            }
        } else {
            null
        }

        val matchedGroup = if (matchedClass != null && matchedClass.groups.isNotEmpty()) {
            matchedClass.groups.find {
                it.code.equals(profile.groupCode, ignoreCase = true) ||
                it.titleBn.equals(profile.groupTitleBn, ignoreCase = true)
            } ?: matchedClass.groups.firstOrNull()
        } else {
            null
        }

        _uiState.update {
            it.copy(
                selectedClass = matchedClass,
                selectedBatch = matchedBatch,
                selectedGroup = matchedGroup
            )
        }
    }

    /**
     * Saves / Submits the selected syllabus
     */
    fun submitSyllabus(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        val selectedClass = state.selectedClass ?: return

        val batchYear = if (selectedClass.batches.isNotEmpty()) state.selectedBatch else null
        val groupCode = if (selectedClass.groups.isNotEmpty()) state.selectedGroup?.code else null

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            
            val result = repository.updateSyllabus(
                userId = userId,
                classId = selectedClass.id,
                batchYear = batchYear,
                groupCode = groupCode
            )

            result.fold(
                onSuccess = { savedEntity ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            savedProfile = savedEntity,
                            successMessage = "সিলেবাস সফলভাবে আপডেট করা হয়েছে",
                            screenMode = SyllabusScreenMode.SUMMARY
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = error.localizedMessage ?: "সিলেবাস পরিবর্তন ব্যর্থ হয়েছে।"
                        )
                    }
                }
            )
        }
    }

    fun onSave(onSuccess: () -> Unit = {}) = submitSyllabus(onSuccess)

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    class Factory(
        private val repository: AcademicRepository,
        private val userId: String = "usr_101"
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SyllabusViewModel(repository, userId) as T
        }
    }
}
