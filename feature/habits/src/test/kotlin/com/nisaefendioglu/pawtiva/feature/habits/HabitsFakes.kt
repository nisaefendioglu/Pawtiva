package com.nisaefendioglu.pawtiva.feature.habits

import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

/** Mirrors the real repository's contract, including name validation and soft delete. */
class FakeHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    private var nextId = 1L

    override fun observeActiveHabits(): Flow<List<Habit>> = habits.map { list -> list.filter { !it.archived } }
    override fun observeHabit(id: Long): Flow<Habit?> = habits.map { list -> list.firstOrNull { it.id == id } }
    override fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>> =
        observeActiveHabits().map { list -> list.map { DailyHabit(it, completed = false) } }

    override suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long {
        require(HabitValidator.validateName(name) == null) { "invalid name" }
        val id = nextId++
        habits.update { it + Habit(id, name.trim(), description, icon, color, Instant.EPOCH) }
        return id
    }

    override suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor) {
        require(HabitValidator.validateName(name) == null) { "invalid name" }
        habits.update { list ->
            list.map { if (it.id == id) it.copy(name = name.trim(), description = description, icon = icon, color = color) else it }
        }
    }

    override suspend fun archiveHabit(id: Long) {
        habits.update { list -> list.map { if (it.id == id) it.copy(archived = true) else it } }
    }
}
