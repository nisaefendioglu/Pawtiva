package com.nisaefendioglu.pawtiva.feature.settings

import com.nisaefendioglu.pawtiva.core.data.AppLanguageController
import com.nisaefendioglu.pawtiva.core.data.DataManagementRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.LocalTime

class FakeUserPreferencesRepository(initial: UserPreferences = UserPreferences()) : UserPreferencesRepository {
    val state = MutableStateFlow(initial)
    override val preferences = state
    override suspend fun setPetType(type: PetType) = state.update { it.copy(petType = type) }
    override suspend fun setPetName(name: String?) = state.update { it.copy(petName = name) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDynamicColor(enabled: Boolean) = state.update { it.copy(dynamicColor = enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) = state.update { it.copy(onboardingCompleted = completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(remindersEnabled = enabled, reminderTime = time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) = state.update { it.copy(reducePetAnimations = reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) = state.update { it.copy(dateFormat = format) }
    override suspend fun clear() {
        state.value = UserPreferences()
    }
}

class FakeProgressRepository(progress: Progress = Progress()) : ProgressRepository {
    val state = MutableStateFlow(progress)
    override fun observeProgress(): Flow<Progress> = state
    override suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome = CompleteOutcome.HabitNotFound
    override suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome = UndoOutcome.NotCompleted
    override fun observeCompletionHistory(): Flow<List<CompletionRecord>> = flowOf(emptyList())
}

class FakeAppLanguageController(var language: AppLanguage = AppLanguage.SYSTEM) : AppLanguageController {
    override fun current() = language
    override fun apply(language: AppLanguage) {
        this.language = language
    }
}

class FakeReminderScheduler : ReminderScheduler {
    var scheduledAt: LocalTime? = null
    var cancelCount = 0
    override fun schedule(time: LocalTime) {
        scheduledAt = time
    }
    override fun cancel() {
        scheduledAt = null
        cancelCount++
    }
}

class FakeDataManagementRepository(private val preferences: FakeUserPreferencesRepository) : DataManagementRepository {
    var resetCount = 0
    var failExport = false
    override suspend fun exportJson(): String {
        check(!failExport) { "export failed" }
        return """{"habits":[]}"""
    }
    override suspend fun resetAllData() {
        resetCount++
        preferences.clear()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
