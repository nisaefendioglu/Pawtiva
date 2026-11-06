package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

/*
 * Contracts shared by every feature module. Implementations live in this module and are bound
 * with Hilt; features depend only on these interfaces (and use fakes in their tests).
 * All suspend functions are main-safe: implementations move work off the main thread.
 */

interface HabitRepository {
    /** Active (not archived) habits, oldest first. */
    fun observeActiveHabits(): Flow<List<Habit>>

    fun observeHabit(id: Long): Flow<Habit?>

    /**
     * Every active habit with its done-state for [date], oldest first. Habits are listed on
     * every day of the 7-day window, including days before they were created.
     */
    fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>>

    /** Returns the new id. Throws [IllegalArgumentException] when the name is invalid. */
    suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long

    /** Throws [IllegalArgumentException] when the name is invalid. */
    suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor)

    /**
     * "Delete" from the user's point of view: the habit disappears from lists, but its past
     * completions, XP and coins stay, so statistics and level never change.
     */
    suspend fun archiveHabit(id: Long)
}

interface ProgressRepository {
    fun observeProgress(): Flow<Progress>

    /** Awards XP and PawCoins once per habit per day, in one database transaction. */
    suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome

    /** Removes the completion and reverses its reward; the coin balance never goes below zero. */
    suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome

    /** Every completion ever recorded (archived habits included), newest first. */
    fun observeCompletionHistory(): Flow<List<CompletionRecord>>
}

interface ShopRepository {
    /** Full catalog with ownership, equipped state, affordability and pet fit. */
    fun observeShop(petType: PetType): Flow<List<ShopItemState>>

    /** Equipped items, whether or not they fit the current pet (renderers filter). */
    fun observeEquipped(): Flow<Set<AccessoryId>>

    suspend fun purchase(id: AccessoryId): PurchaseOutcome

    /** Equips an owned item, taking off whatever was worn in the same slot. False if not owned. */
    suspend fun equip(id: AccessoryId): Boolean

    suspend fun unequip(id: AccessoryId)
}

interface AchievementRepository {
    /** Every achievement in definition order, unlocked or not, with progress 0..1. */
    fun observeAchievements(): Flow<List<AchievementStatus>>
}

interface DataManagementRepository {
    /** All habits, completions, transactions, inventory, achievements and preferences as JSON. */
    suspend fun exportJson(): String

    /** Deletes every table and preference; the app returns to onboarding. Cannot be undone. */
    suspend fun resetAllData()
}

/** Daily reminder at a local time. Implemented in the app module (AlarmManager). */
interface ReminderScheduler {
    fun schedule(time: LocalTime)
    fun cancel()
}
