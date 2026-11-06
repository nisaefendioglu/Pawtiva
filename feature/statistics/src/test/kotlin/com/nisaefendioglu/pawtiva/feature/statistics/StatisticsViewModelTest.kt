package com.nisaefendioglu.pawtiva.feature.statistics

import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.Progress
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 5)
    private val progress = FakeProgressRepository()
    private val habits = FakeHabitRepository()
    private val achievements = FakeAchievementRepository()
    private val preferences = FakeUserPreferencesRepository()

    private fun viewModel() = StatisticsViewModel(progress, habits, achievements, preferences, FixedClock(today))

    @Test
    fun noData_isAnEmptyStateWithAllAchievementsLocked() = runTest {
        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val state = vm.uiState.value
        assertFalse(state.loading)
        assertTrue(state.isEmpty)
        assertEquals(0, state.summary?.totalCompletions)
        assertEquals(7, state.achievements.size)
        assertEquals(0, state.unlockedCount)
        assertEquals(today, state.today)
    }

    @Test
    fun storedData_isSummarized() = runTest {
        habits.createHabit("Water", "", "💧", HabitColor.SKY)
        habits.createHabit("Read", "", "📚", HabitColor.MINT)
        progress.progress.value = Progress(totalXp = 30, level = 1, xpIntoLevel = 30, balance = 15, totalCoinsEarned = 15)
        progress.history.value = listOf(
            CompletionRecord(1, "Water", "💧", today, today.atTime(9, 0).toInstant(ZoneOffset.UTC)),
            CompletionRecord(2, "Read", "📚", today.minusDays(1), today.minusDays(1).atTime(9, 0).toInstant(ZoneOffset.UTC)),
            CompletionRecord(1, "Water", "💧", today.minusDays(1), today.minusDays(1).atTime(8, 0).toInstant(ZoneOffset.UTC)),
        )
        preferences.setDateFormat(DateFormatPreference.ISO)

        val vm = viewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }

        val state = vm.uiState.value
        val summary = requireNotNull(state.summary)
        assertFalse(state.isEmpty)
        assertEquals(3, summary.totalCompletions)
        assertEquals(1, summary.completedToday)
        assertEquals(0.5f, summary.todayRate)
        assertEquals(2, summary.currentStreak)
        assertEquals(15, state.progress.totalCoinsEarned)
        assertEquals(DateFormatPreference.ISO, state.dateFormat)
    }
}
