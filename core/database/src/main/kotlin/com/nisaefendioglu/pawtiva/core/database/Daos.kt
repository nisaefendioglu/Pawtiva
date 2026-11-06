package com.nisaefendioglu.pawtiva.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archived_at IS NULL ORDER BY created_at ASC, id ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE archived_at IS NULL ORDER BY created_at ASC, id ASC")
    suspend fun getActive(): List<HabitEntity>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getAll(): List<HabitEntity>

    @Insert
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("UPDATE habits SET archived_at = :archivedAt WHERE id = :id AND archived_at IS NULL")
    suspend fun archive(id: Long, archivedAt: Long)
}

@Dao
interface CompletionDao {
    /** Returns -1 when the (habit, day) pair already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(completion: HabitCompletionEntity): Long

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND local_date = :localDate LIMIT 1")
    suspend fun get(habitId: Long, localDate: String): HabitCompletionEntity?

    @Query("DELETE FROM habit_completions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COALESCE(SUM(xp_awarded), 0) FROM habit_completions")
    suspend fun totalXp(): Int

    @Query("SELECT COALESCE(SUM(xp_awarded), 0) FROM habit_completions")
    fun observeTotalXp(): Flow<Int>

    @Query("SELECT COUNT(*) FROM habit_completions")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM habit_completions")
    fun observeCount(): Flow<Int>

    @Query("SELECT local_date, completed_at, zone_id FROM habit_completions")
    suspend fun timings(): List<CompletionTimingRow>

    @Query("SELECT local_date, completed_at, zone_id FROM habit_completions")
    fun observeTimings(): Flow<List<CompletionTimingRow>>

    @Query("SELECT habit_id FROM habit_completions WHERE local_date = :localDate")
    suspend fun habitIdsOn(localDate: String): List<Long>

    @Query("SELECT habit_id FROM habit_completions WHERE local_date = :localDate")
    fun observeHabitIdsOn(localDate: String): Flow<List<Long>>

    @Query(
        """
        SELECT c.habit_id AS habit_id, h.name AS habit_name, h.icon_key AS habit_icon,
               c.local_date AS local_date, c.completed_at AS completed_at
        FROM habit_completions c
        INNER JOIN habits h ON h.id = c.habit_id
        ORDER BY c.local_date DESC, c.completed_at DESC
        """,
    )
    fun observeHistory(): Flow<List<CompletionWithHabit>>

    @Query("SELECT * FROM habit_completions ORDER BY id ASC")
    suspend fun getAll(): List<HabitCompletionEntity>
}

@Dao
interface CurrencyDao {
    @Insert
    suspend fun insert(transaction: CurrencyTransactionEntity): Long

    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_transactions")
    suspend fun balance(): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_transactions")
    fun observeBalance(): Flow<Int>

    /** Net coins earned from habits: rewards minus reversals (purchases are not subtracted). */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_transactions WHERE type IN ('REWARD', 'REWARD_REVERSAL')")
    fun observeTotalEarned(): Flow<Int>

    /** Net coins already credited for one habit-day (rewards minus reversals). */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM currency_transactions WHERE ref_key IN (:rewardKey, :reversalKey)")
    suspend fun netAmountFor(rewardKey: String, reversalKey: String): Int

    @Query("SELECT COUNT(*) FROM currency_transactions WHERE type = 'PURCHASE'")
    suspend fun purchaseCount(): Int

    @Query("SELECT COUNT(*) FROM currency_transactions WHERE type = 'PURCHASE'")
    fun observePurchaseCount(): Flow<Int>

    @Query("SELECT * FROM currency_transactions ORDER BY id ASC")
    suspend fun getAll(): List<CurrencyTransactionEntity>
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory ORDER BY purchased_at ASC")
    fun observeAll(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory ORDER BY purchased_at ASC")
    suspend fun getAll(): List<InventoryEntity>

    @Query("SELECT * FROM inventory WHERE item_id = :itemId")
    suspend fun get(itemId: String): InventoryEntity?

    /** Returns -1 when the item is already owned. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: InventoryEntity): Long

    @Query("UPDATE inventory SET equipped = :equipped WHERE item_id = :itemId")
    suspend fun setEquipped(itemId: String, equipped: Boolean)
}

@Dao
interface AchievementDao {
    /** Returns -1 when already unlocked; unlocks are never overwritten. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(achievement: AchievementEntity): Long

    @Query("SELECT * FROM achievements")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY unlocked_at ASC")
    suspend fun getAll(): List<AchievementEntity>
}
