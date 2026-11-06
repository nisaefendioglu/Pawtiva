package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset

class StatisticsCalculatorTest {

    private val today = LocalDate.of(2026, 11, 5)

    private fun record(date: LocalDate, habitId: Long = 1, hour: Int = 9) = CompletionRecord(
        habitId = habitId,
        habitName = "Habit $habitId",
        habitIcon = "💧",
        date = date,
        completedAt = date.atTime(hour, 0).toInstant(ZoneOffset.UTC),
    )

    @Test
    fun emptyInput_givesZeroesAndNoHistory() {
        val summary = StatisticsCalculator.summarize(emptyList(), activeHabitCount = 0, today = today)
        assertTrue(summary.isEmpty)
        assertEquals(0f, summary.todayRate)
        assertEquals(7, summary.last7Days.size)
        assertTrue(summary.last7Days.all { it.count == 0 })
        assertEquals(0, summary.last30DaysTotal)
        assertTrue(summary.monthCounts.isEmpty())
        assertTrue(summary.recent.isEmpty())
        assertEquals(0, summary.currentStreak)
        assertEquals(0, summary.longestStreak)
    }

    @Test
    fun dailyRate_usesActiveHabitsAndIsClamped() {
        val records = listOf(record(today, 1), record(today, 2), record(today.minusDays(1), 1))
        val summary = StatisticsCalculator.summarize(records, activeHabitCount = 4, today = today)
        assertEquals(2, summary.completedToday)
        assertEquals(0.5f, summary.todayRate)

        // A habit completed today and archived afterwards cannot push the rate above 100%.
        val clamped = StatisticsCalculator.summarize(records, activeHabitCount = 1, today = today)
        assertEquals(1f, clamped.todayRate)
    }

    @Test
    fun last7Days_isOldestFirstEndingToday() {
        val records = listOf(record(today), record(today.minusDays(6)), record(today.minusDays(6), 2), record(today.minusDays(7)))
        val week = StatisticsCalculator.summarize(records, 2, today).last7Days
        assertEquals(today.minusDays(6), week.first().date)
        assertEquals(today, week.last().date)
        assertEquals(2, week.first().count)
        assertEquals(1, week.last().count)
        assertEquals(3, week.sumOf { it.count })
    }

    @Test
    fun last30Days_excludesOlderAndFutureRecords() {
        val records = listOf(record(today), record(today.minusDays(29)), record(today.minusDays(30)), record(today.plusDays(1)))
        assertEquals(2, StatisticsCalculator.summarize(records, 1, today).last30DaysTotal)
    }

    @Test
    fun monthCounts_respectMonthBoundaries() {
        val firstOfMonth = LocalDate.of(2026, 11, 1)
        val records = listOf(record(firstOfMonth), record(firstOfMonth, 2), record(LocalDate.of(2026, 10, 31)), record(today))
        val summary = StatisticsCalculator.summarize(records, 2, today)
        assertEquals(YearMonth.of(2026, 11), summary.month)
        assertEquals(mapOf(1 to 2, 5 to 1), summary.monthCounts)
    }

    @Test
    fun history_isNewestFirstAndLimited() {
        val records = (0L until 15L).map { record(today.minusDays(it)) }
        val recent = StatisticsCalculator.summarize(records, 1, today, recentLimit = 10).recent
        assertEquals(10, recent.size)
        assertEquals(today, recent.first().date)
        assertEquals(today.minusDays(9), recent.last().date)
        assertEquals(15, StatisticsCalculator.summarize(records, 1, today).totalCompletions)
    }

    @Test
    fun streaks_countConsecutiveDays_andTodayMayStillBeOpen() {
        val days = listOf(1L, 2L, 3L, 6L, 7L).map { record(today.minusDays(it)) }
        val summary = StatisticsCalculator.summarize(days, 1, today)
        assertEquals(3, summary.currentStreak) // yesterday back to 3 days ago
        assertEquals(3, summary.longestStreak)

        val withToday = StatisticsCalculator.summarize(days + record(today), 1, today)
        assertEquals(4, withToday.currentStreak)
        assertEquals(4, withToday.longestStreak)

        val broken = StatisticsCalculator.summarize(listOf(record(today.minusDays(2))), 1, today)
        assertEquals(0, broken.currentStreak)
        assertEquals(1, broken.longestStreak)
    }
}
