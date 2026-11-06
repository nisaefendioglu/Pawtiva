package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import java.time.LocalDate
import java.time.YearMonth

/** Number of completions on one calendar day. */
data class DayCount(val date: LocalDate, val count: Int)

/** Everything the statistics screen shows, computed only from stored completions. */
data class StatisticsSummary(
    val totalCompletions: Int,
    val completedToday: Int,
    val activeHabits: Int,
    /** completedToday / activeHabits, clamped to 0..1; 0 when there are no active habits. */
    val todayRate: Float,
    /** Seven entries, oldest first, ending with today. */
    val last7Days: List<DayCount>,
    val last30DaysTotal: Int,
    val month: YearMonth,
    /** Day of month -> completions, only for days of [month] that have at least one. */
    val monthCounts: Map<Int, Int>,
    /** Newest first. */
    val recent: List<CompletionRecord>,
    /** Consecutive days with at least one completion, ending today (or yesterday if today is still open). */
    val currentStreak: Int,
    val longestStreak: Int,
) {
    val isEmpty: Boolean get() = totalCompletions == 0
}

object StatisticsCalculator {

    const val DEFAULT_RECENT_LIMIT = 10

    fun summarize(
        records: List<CompletionRecord>,
        activeHabitCount: Int,
        today: LocalDate,
        recentLimit: Int = DEFAULT_RECENT_LIMIT,
        /** When given, today's rate counts only these (active) habits, matching the home screen. */
        activeHabitIds: Set<Long>? = null,
    ): StatisticsSummary {
        val countsByDate: Map<LocalDate, Int> = records.groupingBy { it.date }.eachCount()
        val active = activeHabitCount.coerceAtLeast(0)
        val completedToday = if (activeHabitIds != null) {
            records.count { it.date == today && it.habitId in activeHabitIds }
        } else {
            countsByDate[today] ?: 0
        }
        val todayRate = if (active == 0) 0f else (completedToday.coerceAtMost(active).toFloat() / active)

        val last7 = (6L downTo 0L).map { offset ->
            val date = today.minusDays(offset)
            DayCount(date, countsByDate[date] ?: 0)
        }
        val thirtyDaysStart = today.minusDays(29)
        val last30 = countsByDate
            .filterKeys { !it.isBefore(thirtyDaysStart) && !it.isAfter(today) }
            .values
            .sum()

        val month = YearMonth.from(today)
        val monthCounts = countsByDate
            .filterKeys { YearMonth.from(it) == month }
            .mapKeys { it.key.dayOfMonth }

        val recent = records
            .sortedWith(compareByDescending<CompletionRecord> { it.completedAt }.thenByDescending { it.date })
            .take(recentLimit.coerceAtLeast(0))

        val days = countsByDate.keys.filter { !it.isAfter(today) }.toSortedSet()
        return StatisticsSummary(
            totalCompletions = records.size,
            completedToday = completedToday,
            activeHabits = active,
            todayRate = todayRate,
            last7Days = last7,
            last30DaysTotal = last30,
            month = month,
            monthCounts = monthCounts,
            recent = recent,
            currentStreak = currentStreak(days, today),
            longestStreak = longestStreak(days),
        )
    }

    /** A day without completions only breaks the streak once it is over, so today may still be empty. */
    fun currentStreak(days: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in days) today else today.minusDays(1)
        var streak = 0
        while (cursor in days) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun longestStreak(days: Set<LocalDate>): Int {
        var longest = 0
        var run = 0
        var previous: LocalDate? = null
        days.sorted().forEach { day ->
            run = if (previous != null && previous!!.plusDays(1) == day) run + 1 else 1
            if (run > longest) longest = run
            previous = day
        }
        return longest
    }
}
