package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.AchievementId
import java.time.LocalDate

/** Inputs the achievement rules look at. All values come from stored data only. */
data class AchievementStats(
    val totalCompletions: Int,
    val completionDates: Set<LocalDate>,
    val level: Int,
    val purchaseCount: Int,
    /** True when the current action finished every active habit of a day. */
    val hadPerfectDay: Boolean,
)

object AchievementEvaluator {
    const val BEGINNER_TARGET = 10
    const val DETERMINED_TARGET = 50
    const val CONSISTENCY_DAYS = 7
    const val GROWING_TOGETHER_LEVEL = 5

    fun qualifies(id: AchievementId, stats: AchievementStats): Boolean = progress(id, stats) >= 1f

    /** 0..1 progress towards [id]. */
    fun progress(id: AchievementId, stats: AchievementStats): Float {
        fun ratio(value: Int, target: Int) = (value.toFloat() / target).coerceIn(0f, 1f)
        return when (id) {
            AchievementId.FIRST_STEP -> ratio(stats.totalCompletions, 1)
            AchievementId.BEGINNER -> ratio(stats.totalCompletions, BEGINNER_TARGET)
            AchievementId.CONSISTENCY -> ratio(longestStreak(stats.completionDates), CONSISTENCY_DAYS)
            AchievementId.DETERMINED -> ratio(stats.totalCompletions, DETERMINED_TARGET)
            AchievementId.GROWING_TOGETHER -> ratio(stats.level - 1, GROWING_TOGETHER_LEVEL - 1)
            AchievementId.FIRST_PURCHASE -> ratio(stats.purchaseCount, 1)
            AchievementId.PERFECT_DAY -> if (stats.hadPerfectDay) 1f else 0f
        }
    }

    fun unlocked(stats: AchievementStats): Set<AchievementId> =
        AchievementId.entries.filter { qualifies(it, stats) }.toSet()

    /** Longest run of consecutive calendar days in [dates]. */
    fun longestStreak(dates: Set<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        var best = 1
        var current = 1
        val sorted = dates.sorted()
        for (i in 1 until sorted.size) {
            current = if (sorted[i - 1].plusDays(1) == sorted[i]) current + 1 else 1
            if (current > best) best = current
        }
        return best
    }
}
