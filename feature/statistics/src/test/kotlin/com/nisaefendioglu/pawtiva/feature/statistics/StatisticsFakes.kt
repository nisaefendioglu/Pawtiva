package com.nisaefendioglu.pawtiva.feature.statistics

import com.nisaefendioglu.pawtiva.core.data.AchievementRepository
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
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
import java.time.ZoneId
import java.time.ZoneOffset

class FixedClock(private val today: LocalDate) : PawtivaClock {
    override fun now(): Instant = today.atTime(12, 0).toInstant(ZoneOffset.UTC)
    override fun zone(): ZoneId = ZoneOffset.UTC
}

class FakeHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    override fun observeActiveHabits(): Flow<List<Habit>> = habits.map { list -> list.filterNot { it.archived } }
    override fun observeHabit(id: Long): Flow<Habit?> = habits.map { list -> list.firstOrNull { it.id == id } }
    override fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>> =
        habits.map { list -> list.filterNot { it.archived }.map { DailyHabit(it, completed = false) } }
    override suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long {
        val id = (habits.value.maxOfOrNull { it.id } ?: 0L) + 1
        habits.update { it + Habit(id, name, description, icon, color, Instant.EPOCH) }
        return id
    }
    override suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor) =
        habits.update { list -> list.map { if (it.id == id) it.copy(name = name, description = description, icon = icon, color = color) else it } }
    override suspend fun archiveHabit(id: Long) =
        habits.update { list -> list.map { if (it.id == id) it.copy(archived = true) else it } }
}

class FakeProgressRepository : ProgressRepository {
    val progress = MutableStateFlow(Progress())
    val history = MutableStateFlow<List<CompletionRecord>>(emptyList())
    override fun observeProgress(): Flow<Progress> = progress
    override suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome = CompleteOutcome.HabitNotFound
    override suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome = UndoOutcome.NotCompleted
    override fun observeCompletionHistory(): Flow<List<CompletionRecord>> = history
}

class FakeAchievementRepository : AchievementRepository {
    val achievements = MutableStateFlow(AchievementId.entries.map { AchievementStatus(it, null, 0f) })
    override fun observeAchievements(): Flow<List<AchievementStatus>> = achievements
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    val state = MutableStateFlow(UserPreferences(petType = PetType.CAT))
    override val preferences: Flow<UserPreferences> = state
    override suspend fun setPetType(type: PetType) = state.update { it.copy(petType = type) }
    override suspend fun setPetName(name: String?) = state.update { it.copy(petName = name) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDynamicColor(enabled: Boolean) = state.update { it.copy(dynamicColor = enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) =
        state.update { it.copy(onboardingCompleted = completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(remindersEnabled = enabled, reminderTime = time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) =
        state.update { it.copy(reducePetAnimations = reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) = state.update { it.copy(dateFormat = format) }
    override suspend fun clear() {
        state.value = UserPreferences()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
