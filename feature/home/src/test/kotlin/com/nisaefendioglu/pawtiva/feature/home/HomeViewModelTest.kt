package com.nisaefendioglu.pawtiva.feature.home

import androidx.lifecycle.SavedStateHandle
import com.nisaefendioglu.pawtiva.core.model.PetMood
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today: LocalDate = LocalDate.of(2026, 11, 5)
    private val clock = FakeClock(today.atTime(10, 0))
    private val habits = FakeHabitRepository()
    private val progress = FakeProgressRepository(habits)
    private val shop = FakeShopRepository()
    private val prefs = FakeUserPreferencesRepository()

    private fun TestScope.createViewModel(): Pair<HomeViewModel, MutableList<HomeEvent>> {
        val vm = HomeViewModel(prefs, habits, progress, shop, clock, SavedStateHandle())
        val events = mutableListOf<HomeEvent>()
        backgroundScope.launch { vm.uiState.collect {} }
        backgroundScope.launch { vm.events.toList(events) }
        runCurrent()
        return vm to events
    }

    @Test
    fun loadsToday_withSevenDayWindow() = runTest {
        habits.add(1, "Water")
        val (vm, _) = createViewModel()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(today, state.selectedDate)
        assertEquals(7, state.days.size)
        assertEquals(today, state.days.last())
        assertEquals(1, state.totalCount)
    }

    @Test
    fun complete_thenUndo_updatesStateAndEmitsEvents() = runTest {
        habits.add(1, "Water")
        habits.add(2, "Walk")
        val (vm, events) = createViewModel()

        vm.onToggleHabit(1)
        runCurrent()
        assertTrue(vm.uiState.value.habits.first { it.habit.id == 1L }.completed)
        assertEquals(10, vm.uiState.value.progress.totalXp)
        assertEquals(HomeEvent.Rewarded(10, 5), events.first())
        assertEquals(PetMood.EXCITED, vm.uiState.value.mood)

        vm.onToggleHabit(1)
        runCurrent()
        assertFalse(vm.uiState.value.habits.first { it.habit.id == 1L }.completed)
        assertEquals(0, vm.uiState.value.progress.totalXp)
        assertEquals(HomeEvent.Undone, events.last())
    }

    @Test
    fun doubleTap_whileSaving_isIgnored() = runTest {
        habits.add(1, "Water")
        val (vm, _) = createViewModel()
        val gate = CompletableDeferred<Unit>()
        progress.gate = gate

        vm.onToggleHabit(1)
        vm.onToggleHabit(1)
        runCurrent()
        assertTrue(1L in vm.uiState.value.inFlight)

        gate.complete(Unit)
        runCurrent()
        assertEquals(1, progress.completeCalls)
        assertTrue(vm.uiState.value.inFlight.isEmpty())
        assertEquals(10, vm.uiState.value.progress.totalXp)
    }

    @Test
    fun lastHabitOfTheDay_emitsDayCompleted_andLevelUp() = runTest {
        habits.add(1, "Water")
        progress.totalXp.value = 95
        val (vm, events) = createViewModel()

        vm.onToggleHabit(1)
        runCurrent()

        assertTrue(events.any { it is HomeEvent.DayCompleted && it.isToday })
        assertTrue(HomeEvent.LeveledUp(2) in events)
        assertEquals(2, vm.uiState.value.progress.level)
    }

    @Test
    fun pastDay_canBeSelectedAndCompleted_butPetFollowsToday() = runTest {
        habits.add(1, "Water")
        val (vm, _) = createViewModel()
        val yesterday = today.minusDays(1)

        vm.onSelectDate(yesterday)
        runCurrent()
        assertEquals(yesterday, vm.uiState.value.selectedDate)
        vm.onToggleHabit(1)
        runCurrent()

        assertTrue(vm.uiState.value.habits.single().completed)
        assertEquals(PetMood.NORMAL, vm.uiState.value.mood)
    }

    @Test
    fun datesOutsideTheWindow_fallBackToToday() = runTest {
        val (vm, _) = createViewModel()
        vm.onSelectDate(today.minusDays(10))
        runCurrent()
        assertEquals(today, vm.uiState.value.selectedDate)
    }

    @Test
    fun midnight_rollsOverToTheNewDay() = runTest {
        clock.dateTime = LocalDateTime.of(2026, 11, 5, 23, 59)
        val (vm, _) = createViewModel()
        assertEquals(today, vm.uiState.value.today)
        assertEquals(PetMood.SLEEPY, vm.uiState.value.mood)

        clock.dateTime = LocalDateTime.of(2026, 11, 6, 0, 0)
        advanceTimeBy(61_000)
        runCurrent()

        assertEquals(today.plusDays(1), vm.uiState.value.today)
        assertEquals(today.plusDays(1), vm.uiState.value.selectedDate)
    }

    @Test
    fun greeting_followsLocalHour() {
        assertEquals(Greeting.NIGHT, greetingFor(4))
        assertEquals(Greeting.MORNING, greetingFor(5))
        assertEquals(Greeting.AFTERNOON, greetingFor(12))
        assertEquals(Greeting.EVENING, greetingFor(21))
        assertEquals(Greeting.NIGHT, greetingFor(22))
    }
}
