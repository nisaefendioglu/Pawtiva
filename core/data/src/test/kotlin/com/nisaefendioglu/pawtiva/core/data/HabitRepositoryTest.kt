package com.nisaefendioglu.pawtiva.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HabitRepositoryTest {

    private val repos = Repos(inMemoryDb())

    @After
    fun close() = repos.db.close()

    @Test
    fun create_trimsAndStores() = runTest {
        val id = repos.habits.createHabit("  Drink water  ", "  8 glasses ", "💧", HabitColor.SKY)
        val habit = repos.habits.observeHabit(id).first()
        assertNotNull(habit)
        assertEquals("Drink water", habit!!.name)
        assertEquals("8 glasses", habit.description)
        assertEquals("💧", habit.icon)
        assertEquals(HabitColor.SKY, habit.color)
    }

    @Test(expected = IllegalArgumentException::class)
    fun create_blankName_isRejected() = runTest {
        repos.habits.createHabit("   ", "", "💧", HabitColor.MINT)
    }

    @Test(expected = IllegalArgumentException::class)
    fun update_blankName_isRejected() = runTest {
        val id = repos.habits.createHabit("Read", "", "📚", HabitColor.MINT)
        repos.habits.updateHabit(id, "", "", "📚", HabitColor.MINT)
    }

    @Test
    fun update_changesFields() = runTest {
        val id = repos.habits.createHabit("Read", "", "📚", HabitColor.MINT)
        repos.habits.updateHabit(id, "Read 10 pages", "before bed", "📚", HabitColor.PEACH)
        val habit = repos.habits.observeHabit(id).first()!!
        assertEquals("Read 10 pages", habit.name)
        assertEquals(HabitColor.PEACH, habit.color)
    }

    @Test
    fun archive_hidesHabit_butKeepsHistoryAndXp() = runTest {
        val id = repos.habits.createHabit("Walk", "", "🚶", HabitColor.MINT)
        repos.progress.complete(id, repos.clock.today)
        repos.habits.archiveHabit(id)

        assertTrue(repos.habits.observeActiveHabits().first().isEmpty())
        assertTrue(repos.habits.observeHabit(id).first()!!.archived)
        assertEquals(1, repos.progress.observeCompletionHistory().first().size)
        assertEquals(10, repos.progress.observeProgress().first().totalXp)
    }

    @Test
    fun dailyHabits_includeHabitsAddedLater_andCompletionState() = runTest {
        val clock = repos.clock
        val today = clock.today
        val old = repos.habits.createHabit("Old", "", "🌿", HabitColor.MINT)
        clock.today = today.plusDays(1)
        val fresh = repos.habits.createHabit("New", "", "🌿", HabitColor.MINT)

        // A habit added today is also listed on yesterday, so it can be filled in retroactively.
        val yesterdayList = repos.habits.observeDailyHabits(today).first()
        assertEquals(listOf(old, fresh), yesterdayList.map { it.habit.id })

        repos.progress.complete(fresh, clock.today)
        val todayList = repos.habits.observeDailyHabits(clock.today).first()
        assertEquals(listOf(old, fresh), todayList.map { it.habit.id })
        assertFalse(todayList.first { it.habit.id == old }.completed)
        assertTrue(todayList.first { it.habit.id == fresh }.completed)
    }
}
