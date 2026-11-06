package com.nisaefendioglu.pawtiva.core.data

import androidx.room.withTransaction
import com.nisaefendioglu.pawtiva.core.database.CurrencyTransactionEntity
import com.nisaefendioglu.pawtiva.core.database.CurrencyTransactionType
import com.nisaefendioglu.pawtiva.core.database.HabitCompletionEntity
import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.EditableDays
import com.nisaefendioglu.pawtiva.core.domain.LevelCalculator
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.domain.PetMoodCalculator
import com.nisaefendioglu.pawtiva.core.domain.RewardPolicy
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.CompletionReward
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

internal class ProgressRepositoryImpl @Inject constructor(
    private val db: PawtivaDatabase,
    private val clock: PawtivaClock,
    private val achievements: AchievementUnlocker,
) : ProgressRepository {

    private val habitDao get() = db.habitDao()
    private val completionDao get() = db.completionDao()
    private val currencyDao get() = db.currencyDao()

    override fun observeProgress(): Flow<Progress> = combine(
        completionDao.observeTotalXp(),
        currencyDao.observeBalance(),
        currencyDao.observeTotalEarned(),
    ) { xp, balance, earned ->
        Progress(
            totalXp = xp,
            level = LevelCalculator.level(xp),
            xpIntoLevel = LevelCalculator.xpIntoLevel(xp),
            xpPerLevel = LevelCalculator.XP_PER_LEVEL,
            balance = balance.coerceAtLeast(0),
            totalCoinsEarned = earned.coerceAtLeast(0),
        )
    }.distinctUntilChanged()

    override suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome {
        if (!EditableDays.isEditable(date, clock.today())) return CompleteOutcome.DateNotEditable
        return db.withTransaction {
            val habit = habitDao.getById(habitId)
            if (habit == null || habit.archivedAtEpochMs != null) return@withTransaction CompleteOutcome.HabitNotFound
            val zone = clock.zone()

            val dateKey = date.toString()
            val eligibleIds = eligibleHabitIds(date)
            val doneBefore = completionDao.habitIdsOn(dateKey).count { it in eligibleIds }
            val xpBefore = completionDao.totalXp()
            val now = clock.now().toEpochMilli()

            // A habit-day pays at most COINS_PER_COMPLETION in total. If an earlier undo could not
            // take its coins back (they were already spent), re-completing pays only the difference,
            // so undo -> spend -> redo can never mint free PawCoins.
            val alreadyCredited = currencyDao.netAmountFor(rewardKey(habitId, dateKey), reversalKey(habitId, dateKey))
            val coins = (RewardPolicy.COINS_PER_COMPLETION - alreadyCredited).coerceIn(0, RewardPolicy.COINS_PER_COMPLETION)

            val rowId = completionDao.insert(
                HabitCompletionEntity(
                    habitId = habitId,
                    localDate = dateKey,
                    completedAtEpochMs = now,
                    zoneId = zone.id,
                    xpAwarded = RewardPolicy.XP_PER_COMPLETION,
                    currencyAwarded = coins,
                ),
            )
            if (rowId == -1L) return@withTransaction CompleteOutcome.AlreadyCompleted

            if (coins > 0) {
                currencyDao.insert(
                    CurrencyTransactionEntity(
                        amount = coins,
                        type = CurrencyTransactionType.REWARD.name,
                        refKey = rewardKey(habitId, dateKey),
                        createdAtEpochMs = now,
                    ),
                )
            }

            val doneAfter = doneBefore + if (habitId in eligibleIds) 1 else 0
            val activeCount = eligibleIds.size
            val completedTheDay = PetMoodCalculator.completesTheDay(doneBefore, doneAfter, activeCount)
            val perfectDay = activeCount > 0 && doneAfter >= activeCount
            val unlocked = achievements.unlockNew(hadPerfectDay = perfectDay)

            CompleteOutcome.Completed(
                CompletionReward(
                    xpGained = RewardPolicy.XP_PER_COMPLETION,
                    coinsGained = coins,
                    levelBefore = LevelCalculator.level(xpBefore),
                    levelAfter = LevelCalculator.level(xpBefore + RewardPolicy.XP_PER_COMPLETION),
                    completedTheDay = completedTheDay,
                    unlockedAchievements = unlocked,
                ),
            )
        }
    }

    override suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome {
        if (!EditableDays.isEditable(date, clock.today())) return UndoOutcome.DateNotEditable
        return db.withTransaction {
            val dateKey = date.toString()
            val completion = completionDao.get(habitId, dateKey) ?: return@withTransaction UndoOutcome.NotCompleted
            completionDao.deleteById(completion.id)

            // Coins may already be spent; take back only what is there, never below zero.
            val balance = currencyDao.balance().coerceAtLeast(0)
            val coinsToRemove = minOf(completion.currencyAwarded, balance)
            if (coinsToRemove > 0) {
                currencyDao.insert(
                    CurrencyTransactionEntity(
                        amount = -coinsToRemove,
                        type = CurrencyTransactionType.REWARD_REVERSAL.name,
                        refKey = reversalKey(habitId, dateKey),
                        createdAtEpochMs = clock.now().toEpochMilli(),
                    ),
                )
            }
            UndoOutcome.Undone(
                xpRemoved = completion.xpAwarded,
                coinsRemoved = coinsToRemove,
                levelAfter = LevelCalculator.level(completionDao.totalXp()),
            )
        }
    }

    override fun observeCompletionHistory(): Flow<List<CompletionRecord>> =
        completionDao.observeHistory().map { rows -> rows.map { it.toModel() } }.distinctUntilChanged()

    private fun rewardKey(habitId: Long, dateKey: String) = "completion:$habitId:$dateKey"

    private fun reversalKey(habitId: Long, dateKey: String) = "undo:$habitId:$dateKey"

    /** Habits that count for [date]: every active habit (any day in the 7-day window can be filled in). */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun eligibleHabitIds(date: LocalDate): Set<Long> =
        habitDao.getActive().map { it.id }.toSet()
}
