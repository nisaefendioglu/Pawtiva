package com.nisaefendioglu.pawtiva.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.AppLanguageController
import com.nisaefendioglu.pawtiva.core.data.DataManagementRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val progress: Progress = Progress(),
    val language: AppLanguage = AppLanguage.SYSTEM,
    val isLoading: Boolean = true,
)

sealed interface SettingsEvent {
    data object ExportSucceeded : SettingsEvent
    data object ExportFailed : SettingsEvent
    data object ResetDone : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    progress: ProgressRepository,
    private val languageController: AppLanguageController,
    private val reminderScheduler: ReminderScheduler,
    private val dataManagement: DataManagementRepository,
) : ViewModel() {

    private val language = MutableStateFlow(languageController.current())
    private val events = Channel<SettingsEvent>(Channel.BUFFERED)

    /** One-off messages (export result, reset done) for a snackbar. */
    val eventFlow: Flow<SettingsEvent> = events.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferences.preferences,
        progress.observeProgress(),
        language,
    ) { prefs, progressValue, lang ->
        SettingsUiState(preferences = prefs, progress = progressValue, language = lang, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setLanguage(value: AppLanguage) {
        language.value = value
        languageController.apply(value)
    }

    fun setTheme(mode: ThemeMode) = launch { preferences.setThemeMode(mode) }

    fun setDynamicColor(enabled: Boolean) = launch { preferences.setDynamicColor(enabled) }

    fun setDateFormat(format: DateFormatPreference) = launch { preferences.setDateFormat(format) }

    /** Only the species changes; habits, XP, coins, inventory and achievements are untouched. */
    fun changePet(type: PetType) = launch { preferences.setPetType(type) }

    /**
     * Saves a new pet name. Blank clears it (the default name is shown). Returns false, and
     * saves nothing, when the name is longer than [UserPreferences.PET_NAME_MAX_LENGTH].
     */
    fun renamePet(name: String): Boolean {
        val clean = name.trim()
        if (clean.length > UserPreferences.PET_NAME_MAX_LENGTH || clean.contains('\n')) return false
        launch { preferences.setPetName(clean.ifEmpty { null }) }
        return true
    }

    fun setReducePetAnimations(reduce: Boolean) = launch { preferences.setReducePetAnimations(reduce) }

    /** Call with true only after the notification permission was granted (or is not needed). */
    fun setRemindersEnabled(enabled: Boolean) = launch {
        val time = preferences.preferences.first().reminderTime
        preferences.setReminders(enabled, time)
        if (enabled) reminderScheduler.schedule(time) else reminderScheduler.cancel()
    }

    fun setReminderTime(time: LocalTime) = launch {
        val enabled = preferences.preferences.first().remindersEnabled
        preferences.setReminders(enabled, time)
        if (enabled) reminderScheduler.schedule(time)
    }

    /** [write] stores the JSON at the place the user picked (done by the UI, off the main thread). */
    fun export(write: suspend (String) -> Unit) = launch {
        val result = runCatching { write(dataManagement.exportJson()) }
        events.send(if (result.isSuccess) SettingsEvent.ExportSucceeded else SettingsEvent.ExportFailed)
    }

    /** Deletes everything. The app then shows onboarding, because the preferences are cleared. */
    fun resetAllData() = launch {
        reminderScheduler.cancel()
        dataManagement.resetAllData()
        // A full reset also returns the in-app language to the device language.
        language.value = AppLanguage.SYSTEM
        languageController.apply(AppLanguage.SYSTEM)
        events.send(SettingsEvent.ResetDone)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
