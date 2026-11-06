package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.AchievementEvaluator
import com.nisaefendioglu.pawtiva.core.domain.AchievementStats
import com.nisaefendioglu.pawtiva.core.domain.LevelCalculator
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDate
import javax.inject.Inject

internal class AchievementRepositoryImpl @Inject constructor(
    private val db: PawtivaDatabase,
) : AchievementRepository {

    override fun observeAchievements(): Flow<List<AchievementStatus>> = combine(
        db.achievementDao().observeAll(),
        db.completionDao().observeCount(),
        db.completionDao().observeTimings(),
        db.completionDao().observeTotalXp(),
        db.currencyDao().observePurchaseCount(),
    ) { unlockedRows, completions, timings, xp, purchases ->
        val unlockedAt = unlockedRows.associate { it.achievementId to it.unlockedAtEpochMs }
        val stats = AchievementStats(
            totalCompletions = completions,
            completionDates = timings.filter { it.recordedSameDay() }.map { LocalDate.parse(it.localDate) }.toSet(),
            level = LevelCalculator.level(xp),
            purchaseCount = purchases,
            hadPerfectDay = false,
        )
        AchievementId.entries.map { id ->
            val at = unlockedAt[id.storageId]
            AchievementStatus(
                id = id,
                unlockedAtEpochMillis = at,
                progress = if (at != null) 1f else AchievementEvaluator.progress(id, stats),
            )
        }
    }.distinctUntilChanged()
}
