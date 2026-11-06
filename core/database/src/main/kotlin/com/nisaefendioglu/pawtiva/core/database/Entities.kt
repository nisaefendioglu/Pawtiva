package com.nisaefendioglu.pawtiva.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * Room schema v1. Dates are stored as ISO yyyy-MM-dd text (the user's local calendar day at the
 * moment of completion) and instants as UTC epoch milliseconds, so no TypeConverters are needed.
 */

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    @ColumnInfo(name = "icon_key") val iconKey: String,
    @ColumnInfo(name = "color_key") val colorKey: String,
    @ColumnInfo(name = "created_at") val createdAtEpochMs: Long,
    @ColumnInfo(name = "archived_at") val archivedAtEpochMs: Long? = null,
)

@Entity(
    tableName = "habit_completions",
    indices = [
        Index(value = ["habit_id", "local_date"], unique = true),
        Index(value = ["local_date"]),
    ],
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "completed_at") val completedAtEpochMs: Long,
    @ColumnInfo(name = "zone_id") val zoneId: String,
    @ColumnInfo(name = "xp_awarded") val xpAwarded: Int,
    @ColumnInfo(name = "currency_awarded") val currencyAwarded: Int,
)

@Entity(tableName = "currency_transactions")
data class CurrencyTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Signed: positive for rewards, negative for purchases and reversals. */
    val amount: Int,
    /** One of [CurrencyTransactionType] names. */
    val type: String,
    @ColumnInfo(name = "ref_key") val refKey: String,
    @ColumnInfo(name = "created_at") val createdAtEpochMs: Long,
)

enum class CurrencyTransactionType { REWARD, REWARD_REVERSAL, PURCHASE }

@Entity(tableName = "inventory")
data class InventoryEntity(
    /** AccessoryId.storageId */
    @PrimaryKey @ColumnInfo(name = "item_id") val itemId: String,
    @ColumnInfo(name = "purchased_at") val purchasedAtEpochMs: Long,
    val equipped: Boolean = false,
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    /** AchievementId.storageId */
    @PrimaryKey @ColumnInfo(name = "achievement_id") val achievementId: String,
    @ColumnInfo(name = "unlocked_at") val unlockedAtEpochMs: Long,
)

/** Result row of the completion-history join. */
data class CompletionWithHabit(
    @ColumnInfo(name = "habit_id") val habitId: Long,
    @ColumnInfo(name = "habit_name") val habitName: String,
    @ColumnInfo(name = "habit_icon") val habitIcon: String,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "completed_at") val completedAtEpochMs: Long,
)

/** When a completion was recorded, relative to the day it counts for. */
data class CompletionTimingRow(
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "completed_at") val completedAtEpochMs: Long,
    @ColumnInfo(name = "zone_id") val zoneId: String,
) {
    /** True when it was ticked off on its own calendar day rather than filled in afterwards. */
    fun recordedSameDay(): Boolean = runCatching {
        java.time.Instant.ofEpochMilli(completedAtEpochMs)
            .atZone(java.time.ZoneId.of(zoneId))
            .toLocalDate()
            .toString() == localDate
    }.getOrDefault(false)
}
