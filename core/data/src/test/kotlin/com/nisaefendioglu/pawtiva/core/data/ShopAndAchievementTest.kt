package com.nisaefendioglu.pawtiva.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ShopAndAchievementTest {

    private val repos = Repos(inMemoryDb())
    private val today = repos.clock.today

    @After
    fun close() = repos.db.close()

    /** Completes [count] fresh habits today: count * 10 XP and count * 5 coins. */
    private suspend fun earn(count: Int) {
        repeat(count) {
            val id = repos.habits.createHabit("H$it-${System.nanoTime()}", "", "🌿", HabitColor.MINT)
            repos.progress.complete(id, today)
        }
    }

    @Test
    fun purchase_withoutEnoughCoins_reportsMissingAmount() = runTest {
        earn(2) // 10 coins
        assertEquals(PurchaseOutcome.InsufficientBalance(missing = 30), repos.shop.purchase(AccessoryId.BASIC_CAP))
        assertEquals(10, repos.progress.observeProgress().first().balance)
    }

    @Test
    fun purchase_deductsCoins_andBlocksDuplicates() = runTest {
        earn(10) // 50 coins
        val result = repos.shop.purchase(AccessoryId.BASIC_CAP)
        assertTrue(result is PurchaseOutcome.Purchased)
        assertEquals(10, (result as PurchaseOutcome.Purchased).balanceAfter)
        assertEquals(PurchaseOutcome.AlreadyOwned, repos.shop.purchase(AccessoryId.BASIC_CAP))
        assertEquals(10, repos.progress.observeProgress().first().balance)

        val cap = repos.shop.observeShop(PetType.CAT).first().first { it.item.accessory == AccessoryId.BASIC_CAP }
        assertTrue(cap.owned)
        assertFalse(cap.equipped)
    }

    @Test
    fun levelLockedItem_cannotBeBoughtEarly() = runTest {
        earn(39) // 390 XP = level 4, 195 coins
        assertEquals(PurchaseOutcome.LevelTooLow(requiredLevel = 5), repos.shop.purchase(AccessoryId.ROYAL_CROWN))
        earn(1) // 400 XP = level 5, 200 coins
        assertTrue(repos.shop.purchase(AccessoryId.ROYAL_CROWN) is PurchaseOutcome.Purchased)
        assertEquals(0, repos.progress.observeProgress().first().balance)
    }

    @Test
    fun equip_keepsOnlyOneItemPerSlot_andSurvivesPetSwitch() = runTest {
        earn(70) // 350 coins, level 8
        assertTrue(repos.shop.purchase(AccessoryId.BASIC_CAP) is PurchaseOutcome.Purchased)
        assertTrue(repos.shop.purchase(AccessoryId.ROYAL_CROWN) is PurchaseOutcome.Purchased)
        assertTrue(repos.shop.purchase(AccessoryId.WINTER_SCARF) is PurchaseOutcome.Purchased)

        assertFalse(repos.shop.equip(AccessoryId.ROUND_GLASSES)) // not owned
        assertTrue(repos.shop.equip(AccessoryId.BASIC_CAP))
        assertTrue(repos.shop.equip(AccessoryId.WINTER_SCARF))
        assertTrue(repos.shop.equip(AccessoryId.ROYAL_CROWN))
        assertEquals(setOf(AccessoryId.ROYAL_CROWN, AccessoryId.WINTER_SCARF), repos.shop.observeEquipped().first())

        // Pet type only changes how the shop is shown; inventory is untouched.
        val asDog = repos.shop.observeShop(PetType.DOG).first().filter { it.owned }.map { it.item.accessory }.toSet()
        assertEquals(setOf(AccessoryId.BASIC_CAP, AccessoryId.ROYAL_CROWN, AccessoryId.WINTER_SCARF), asDog)

        repos.shop.unequip(AccessoryId.WINTER_SCARF)
        assertEquals(setOf(AccessoryId.ROYAL_CROWN), repos.shop.observeEquipped().first())
    }

    @Test
    fun achievements_unlockOnce_andSurviveUndo() = runTest {
        val id = repos.habits.createHabit("Only habit", "", "🌿", HabitColor.MINT)
        val first = repos.progress.complete(id, today) as CompleteOutcome.Completed
        assertEquals(listOf(AchievementId.FIRST_STEP, AchievementId.PERFECT_DAY), first.reward.unlockedAchievements)

        repos.progress.undo(id, today)
        val again = repos.progress.complete(id, today) as CompleteOutcome.Completed
        assertTrue(again.reward.unlockedAchievements.isEmpty())

        val statuses = repos.achievements.observeAchievements().first()
        assertTrue(statuses.first { it.id == AchievementId.FIRST_STEP }.unlocked)
        assertFalse(statuses.first { it.id == AchievementId.BEGINNER }.unlocked)
        assertEquals(0.1f, statuses.first { it.id == AchievementId.BEGINNER }.progress, 0.001f)
    }

    @Test
    fun firstPurchase_unlocksAchievement() = runTest {
        earn(8)
        val result = repos.shop.purchase(AccessoryId.BASIC_CAP) as PurchaseOutcome.Purchased
        assertTrue(AchievementId.FIRST_PURCHASE in result.unlockedAchievements)
    }

    @Test
    fun consistency_unlocksAfterSevenConsecutiveDays() = runTest {
        repos.clock.today = today.minusDays(6)
        val id = repos.habits.createHabit("Daily", "", "🌿", HabitColor.MINT)
        // Ticked off on each day itself.
        val unlocked = (6 downTo 0).flatMap { back ->
            repos.clock.today = today.minusDays(back.toLong())
            (repos.progress.complete(id, repos.clock.today) as CompleteOutcome.Completed).reward.unlockedAchievements
        }
        assertTrue(AchievementId.CONSISTENCY in unlocked)
        assertEquals(1, unlocked.count { it == AchievementId.CONSISTENCY })
    }

    @Test
    fun consistency_isNotUnlockedByFillingInPastDays() = runTest {
        val id = repos.habits.createHabit("Daily", "", "🌿", HabitColor.MINT)
        val unlocked = (6 downTo 0).flatMap { back ->
            (repos.progress.complete(id, today.minusDays(back.toLong())) as CompleteOutcome.Completed).reward.unlockedAchievements
        }
        assertTrue(AchievementId.CONSISTENCY !in unlocked)
        // The filled-in days still count as completions and XP.
        assertEquals(70, repos.progress.observeProgress().first().totalXp)
    }
}
