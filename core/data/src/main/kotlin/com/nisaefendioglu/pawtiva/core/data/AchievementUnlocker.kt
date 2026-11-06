package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.database.AchievementEntity
import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.AchievementEvaluator
import com.nisaefendioglu.pawtiva.core.domain.AchievementStats
import com.nisaefendioglu.pawtiva.core.domain.LevelCalculator
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import java.time.LocalDate
import javax.inject.Inject

/**
 * Checks the achievement rules against stored data and records new unlocks. Must be called
 * inside the caller's database transaction so the unlock and its trigger commit together.
 * Unlocks are insert-once: undoing a completion never takes an achievement away.
 */
internal class AchievementUnlocker @Inject constructor(
    private val db: PawtivaDatabase,
    private val clock: PawtivaClock,
) {
    suspend fun currentStats(hadPerfectDay: Boolean): AchievementStats = AchievementStats(
        totalCompletions = db.completionDao().count(),
        // Streaks count only days ticked off on the day itself, so filling in the 7-day window
        // retroactively cannot unlock "Consistency" on the first day.
        completionDates = db.completionDao().timings().filter { it.recordedSameDay() }
            .map { LocalDate.parse(it.localDate) }.toSet(),
        level = LevelCalculator.level(db.completionDao().totalXp()),
        purchaseCount = db.currencyDao().purchaseCount(),
        hadPerfectDay = hadPerfectDay,
    )

    /** Returns achievements unlocked by this call, in definition order. */
    suspend fun unlockNew(hadPerfectDay: Boolean): List<AchievementId> {
        val stats = currentStats(hadPerfectDay)
        val now = clock.now().toEpochMilli()
        return AchievementEvaluator.unlocked(stats)
            .sortedBy { it.ordinal }
            .filter { id -> db.achievementDao().insert(AchievementEntity(id.storageId, now)) != -1L }
    }
}
