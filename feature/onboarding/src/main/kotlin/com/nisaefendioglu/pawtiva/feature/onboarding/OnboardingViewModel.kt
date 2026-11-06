package com.nisaefendioglu.pawtiva.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.AppLanguageController
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.HabitIcons
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the first-run flow. Every choice is written to DataStore as soon as it is made, so
 * leaving the app half-way keeps what was picked. The current step survives process death.
 * The optional first-habit step creates a real habit; going back and forward edits it instead of
 * creating a duplicate.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val languageController: AppLanguageController,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: ReminderScheduler,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            step = savedStateHandle.get<String>(KEY_STEP)
                ?.let { name -> OnboardingStep.entries.firstOrNull { it.name == name } }
                ?: OnboardingStep.WELCOME,
            language = languageController.current(),
            firstHabitName = savedStateHandle[KEY_FIRST_HABIT_NAME] ?: "",
            firstHabitIcon = savedStateHandle[KEY_FIRST_HABIT_ICON] ?: HabitIcons.DEFAULT,
        ),
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val stored = preferences.preferences.first()
            _uiState.update {
                // Never overwrite a choice the user already made while this was loading.
                it.copy(
                    petType = it.petType ?: stored.petType,
                    petName = it.petName.ifEmpty { stored.petName.orEmpty() },
                    themeMode = if (it.themeMode == ThemeMode.SYSTEM) stored.themeMode else it.themeMode,
                    remindersEnabled = it.remindersEnabled || stored.remindersEnabled,
                )
            }
        }
    }

    fun onNext() {
        val state = _uiState.value
        if (!state.canContinue) return
        if (state.step == OnboardingStep.FIRST_HABIT) {
            onCreateFirstHabit()
            return
        }
        if (state.step == OnboardingStep.NAME) {
            viewModelScope.launch { preferences.setPetName(state.petName) }
        }
        state.step.next()?.let(::goTo)
    }

    fun onBack() {
        _uiState.value.step.previous()?.let(::goTo)
    }

    fun onSelectPet(type: PetType) {
        _uiState.update { it.copy(petType = type) }
        viewModelScope.launch { preferences.setPetType(type) }
    }

    fun onPetNameChange(value: String) {
        val clean = value.replace("\n", "").take(UserPreferences.PET_NAME_MAX_LENGTH)
        _uiState.update { it.copy(petName = clean) }
    }

    fun onSkipName() {
        _uiState.update { it.copy(petName = "") }
        viewModelScope.launch { preferences.setPetName(null) }
        OnboardingStep.NAME.next()?.let(::goTo)
    }

    fun onSelectLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        languageController.apply(language)
    }

    fun onSelectTheme(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    /** [granted] is false when the user skips or declines the system permission dialog. */
    fun onNotificationsDecided(granted: Boolean) {
        _uiState.update { it.copy(remindersEnabled = granted) }
        viewModelScope.launch { preferences.setReminders(granted, UserPreferences.DEFAULT_REMINDER_TIME) }
        if (granted) reminderScheduler.schedule(UserPreferences.DEFAULT_REMINDER_TIME) else reminderScheduler.cancel()
        OnboardingStep.NOTIFICATIONS.next()?.let(::goTo)
    }

    fun onFirstHabitNameChange(value: String) {
        val clean = value.replace("\n", "").take(HabitValidator.NAME_MAX_LENGTH)
        savedStateHandle[KEY_FIRST_HABIT_NAME] = clean
        _uiState.update { it.copy(firstHabitName = clean, firstHabitFailed = false) }
    }

    /** A suggestion chip fills in both the name and a matching icon. */
    fun onFirstHabitSuggestion(name: String, icon: String) {
        onFirstHabitNameChange(name)
        savedStateHandle[KEY_FIRST_HABIT_ICON] = icon
        _uiState.update { it.copy(firstHabitIcon = icon) }
    }

    fun onCreateFirstHabit() {
        val state = _uiState.value
        if (!state.canContinue || state.isSavingFirstHabit) return
        _uiState.update { it.copy(isSavingFirstHabit = true, firstHabitFailed = false) }
        viewModelScope.launch {
            try {
                val name = state.firstHabitName.trim()
                val existingId = savedStateHandle.get<Long>(KEY_FIRST_HABIT_ID)
                if (existingId != null) {
                    habitRepository.updateHabit(existingId, name, "", state.firstHabitIcon, FIRST_HABIT_COLOR)
                } else {
                    val id = habitRepository.createHabit(name, "", state.firstHabitIcon, FIRST_HABIT_COLOR)
                    savedStateHandle[KEY_FIRST_HABIT_ID] = id
                }
                _uiState.update { it.copy(isSavingFirstHabit = false, firstHabitCreated = true) }
                OnboardingStep.FIRST_HABIT.next()?.let(::goTo)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (failure: Exception) {
                _uiState.update { it.copy(isSavingFirstHabit = false, firstHabitFailed = true) }
            }
        }
    }

    fun onSkipFirstHabit() {
        OnboardingStep.FIRST_HABIT.next()?.let(::goTo)
    }

    fun onFinish() {
        if (_uiState.value.isFinishing) return
        _uiState.update { it.copy(isFinishing = true) }
        viewModelScope.launch { preferences.setOnboardingCompleted(true) }
    }

    private fun goTo(step: OnboardingStep) {
        savedStateHandle[KEY_STEP] = step.name
        _uiState.update { it.copy(step = step) }
    }

    private companion object {
        const val KEY_STEP = "onboarding_step"
        const val KEY_FIRST_HABIT_NAME = "onboarding_first_habit_name"
        const val KEY_FIRST_HABIT_ICON = "onboarding_first_habit_icon"
        const val KEY_FIRST_HABIT_ID = "onboarding_first_habit_id"
        val FIRST_HABIT_COLOR = HabitColor.MINT
    }
}

enum class OnboardingStep {
    WELCOME, INTRO, PET, NAME, LANGUAGE, THEME, NOTIFICATIONS, FIRST_HABIT, DONE;

    fun next(): OnboardingStep? = entries.getOrNull(ordinal + 1)
    fun previous(): OnboardingStep? = entries.getOrNull(ordinal - 1)

    /** Optional steps show a "Skip" action. */
    val isOptional: Boolean get() = this == NAME || this == NOTIFICATIONS || this == FIRST_HABIT
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val petType: PetType? = null,
    val petName: String = "",
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val remindersEnabled: Boolean = false,
    val isFinishing: Boolean = false,
    val firstHabitName: String = "",
    val firstHabitIcon: String = HabitIcons.DEFAULT,
    val isSavingFirstHabit: Boolean = false,
    val firstHabitCreated: Boolean = false,
    val firstHabitFailed: Boolean = false,
) {
    val canContinue: Boolean
        get() = when (step) {
            OnboardingStep.PET -> petType != null
            OnboardingStep.FIRST_HABIT -> HabitValidator.validateName(firstHabitName) == null && !isSavingFirstHabit
            else -> true
        }
    val stepNumber: Int get() = step.ordinal + 1
    val stepCount: Int get() = OnboardingStep.entries.size
}
