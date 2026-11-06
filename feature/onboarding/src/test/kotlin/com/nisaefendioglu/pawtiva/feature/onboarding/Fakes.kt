package com.nisaefendioglu.pawtiva.feature.onboarding

import com.nisaefendioglu.pawtiva.core.data.AppLanguageController
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ReminderScheduler
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class FakeUserPreferencesRepository(initial: UserPreferences = UserPreferences()) : UserPreferencesRepository {
    val state = MutableStateFlow(initial)
    override val preferences = state
    override suspend fun setPetType(type: PetType) = state.update { it.copy(petType = type) }
    override suspend fun setPetName(name: String?) =
        state.update { it.copy(petName = name?.trim()?.takeIf { n -> n.isNotEmpty() }) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDynamicColor(enabled: Boolean) = state.update { it.copy(dynamicColor = enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) =
        state.update { it.copy(onboardingCompleted = completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(remindersEnabled = enabled, reminderTime = time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) =
        state.update { it.copy(reducePetAnimations = reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) = state.update { it.copy(dateFormat = format) }
    override suspend fun clear() { state.value = UserPreferences() }
}

class FakeAppLanguageController(var language: AppLanguage = AppLanguage.SYSTEM) : AppLanguageController {
    override fun current() = language
    override fun apply(language: AppLanguage) {
        this.language = language
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    var createCalls = 0
    var failNext = false
    private var nextId = 1L

    override fun observeActiveHabits(): Flow<List<Habit>> = habits.map { list -> list.filter { !it.archived } }
    override fun observeHabit(id: Long): Flow<Habit?> = habits.map { list -> list.firstOrNull { it.id == id } }
    override fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>> =
        observeActiveHabits().map { list -> list.map { DailyHabit(it, completed = false) } }

    override suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long {
        if (failNext) {
            failNext = false
            throw IllegalStateException("disk full")
        }
        require(name.isNotBlank())
        createCalls++
        val id = nextId++
        habits.update { it + Habit(id, name, description, icon, color, Instant.EPOCH) }
        return id
    }

    override suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor) {
        habits.update { list -> list.map { if (it.id == id) it.copy(name = name, description = description, icon = icon, color = color) else it } }
    }

    override suspend fun archiveHabit(id: Long) {
        habits.update { list -> list.map { if (it.id == id) it.copy(archived = true) else it } }
    }
}

class FakeReminderScheduler : ReminderScheduler {
    var scheduled: LocalTime? = null
    var cancelCalls = 0
    override fun schedule(time: LocalTime) {
        scheduled = time
    }
    override fun cancel() {
        scheduled = null
        cancelCalls++
    }
}
