package com.nisaefendioglu.pawtiva.core.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ProgressRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val repos = Repos(inMemoryDb())
    /** Fixed reference day; tests move [FixedClock.today] around it. */
    private val today = repos.clock.today

    @After
    fun close() = repos.db.close()

    private suspend fun newHabit(name: String = "Habit") = repos.habits.createHabit(name, "", "🌿", HabitColor.MINT)

    @Test
    fun complete_awards10Xp_and5Coins() = runTest {
        val id = newHabit()
        val outcome = repos.progress.complete(id, today)

        assertTrue(outcome is CompleteOutcome.Completed)
        val reward = (outcome as CompleteOutcome.Completed).reward
        assertEquals(10, reward.xpGained)
        assertEquals(5, reward.coinsGained)
        val progress = repos.progress.observeProgress().first()
        assertEquals(10, progress.totalXp)
        assertEquals(1, progress.level)
        assertEquals(5, progress.balance)
        assertEquals(5, progress.totalCoinsEarned)
    }

    @Test
    fun duplicateCompletion_isBlocked_evenWhenConcurrent() = runTest {
        val id = newHabit()
        val results = (1..5).map { async { repos.progress.complete(id, today) } }.awaitAll()

        assertEquals(1, results.count { it is CompleteOutcome.Completed })
        assertEquals(4, results.count { it == CompleteOutcome.AlreadyCompleted })
        assertEquals(10, repos.progress.observeProgress().first().totalXp)
        assertEquals(5, repos.progress.observeProgress().first().balance)
    }

    @Test
    fun undo_reversesXpAndCoins() = runTest {
        val id = newHabit()
        repos.progress.complete(id, today)
        val undo = repos.progress.undo(id, today)

        assertEquals(UndoOutcome.Undone(xpRemoved = 10, coinsRemoved = 5, levelAfter = 1), undo)
        val progress = repos.progress.observeProgress().first()
        assertEquals(0, progress.totalXp)
        assertEquals(0, progress.balance)
        assertEquals(UndoOutcome.NotCompleted, repos.progress.undo(id, today))
        // Can be completed again after undo.
        assertTrue(repos.progress.complete(id, today) is CompleteOutcome.Completed)
    }

    @Test
    fun undo_afterSpendingCoins_neverGoesNegative() = runTest {
        // Earn exactly 40 coins (8 habits), buy the 40-coin cap, then undo one completion.
        val ids = (1..8).map { newHabit("H$it") }
        ids.forEach { repos.progress.complete(it, today) }
        assertTrue(repos.shop.purchase(AccessoryId.BASIC_CAP) is PurchaseOutcome.Purchased)
        assertEquals(0, repos.progress.observeProgress().first().balance)

        val undo = repos.progress.undo(ids.first(), today) as UndoOutcome.Undone
        assertEquals(0, undo.coinsRemoved)
        assertEquals(0, repos.progress.observeProgress().first().balance)
        assertEquals(70, repos.progress.observeProgress().first().totalXp)
    }

    @Test
    fun undoSpendRedo_cannotMintFreeCoins() = runTest {
        val ids = (1..8).map { newHabit("H$it") }
        ids.forEach { repos.progress.complete(it, today) }
        assertTrue(repos.shop.purchase(AccessoryId.BASIC_CAP) is PurchaseOutcome.Purchased)

        // Undo everything (no coins can be taken back), then complete everything again.
        ids.forEach { repos.progress.undo(it, today) }
        val redo = ids.map { repos.progress.complete(it, today) as CompleteOutcome.Completed }

        assertTrue(redo.all { it.reward.coinsGained == 0 && it.reward.xpGained == 10 })
        assertEquals(0, repos.progress.observeProgress().first().balance)
        assertEquals(80, repos.progress.observeProgress().first().totalXp)

        // A normal undo -> redo (coins were taken back) still pays again.
        val fresh = newHabit("Fresh")
        repos.progress.complete(fresh, today)
        repos.progress.undo(fresh, today)
        val again = repos.progress.complete(fresh, today) as CompleteOutcome.Completed
        assertEquals(5, again.reward.coinsGained)
        assertEquals(5, repos.progress.observeProgress().first().balance)
    }

    @Test
    fun levelBoundaries_andMultipleLevelUps() = runTest {
        repos.grantXp(80)
        val id = newHabit()
        val first = repos.progress.complete(id, today) as CompleteOutcome.Completed
        assertEquals(1, first.reward.levelBefore)
        assertEquals(1, first.reward.levelAfter) // 90 XP

        val second = repos.progress.complete(newHabit("B"), today) as CompleteOutcome.Completed
        assertTrue(second.reward.leveledUp) // 100 XP -> level 2
        assertEquals(2, second.reward.levelAfter)

        repos.grantXp(150) // 250 XP total
        assertEquals(3, repos.progress.observeProgress().first().level)
        assertEquals(50, repos.progress.observeProgress().first().xpIntoLevel)
    }

    @Test
    fun datesOutsideTheSevenDayWindow_areRejected() = runTest {
        repos.clock.today = today.minusDays(10)
        val id = newHabit()
        repos.clock.today = today
        assertEquals(CompleteOutcome.DateNotEditable, repos.progress.complete(id, today.plusDays(1)))
        assertEquals(CompleteOutcome.DateNotEditable, repos.progress.complete(id, today.minusDays(7)))
        assertTrue(repos.progress.complete(id, today.minusDays(6)) is CompleteOutcome.Completed)
        assertEquals(UndoOutcome.DateNotEditable, repos.progress.undo(id, today.minusDays(7)))
    }

    @Test
    fun newHabit_canBeFilledInForEarlierDays_butNotWhenArchivedOrMissing() = runTest {
        val id = newHabit()
        assertTrue(repos.progress.complete(id, today.minusDays(1)) is CompleteOutcome.Completed)
        assertTrue(repos.progress.complete(id, today.minusDays(6)) is CompleteOutcome.Completed)
        assertEquals(CompleteOutcome.HabitNotFound, repos.progress.complete(999, today))
        repos.habits.archiveHabit(id)
        assertEquals(CompleteOutcome.HabitNotFound, repos.progress.complete(id, today))
    }

    @Test
    fun lastHabitOfTheDay_completesTheDay() = runTest {
        val a = newHabit("A")
        val b = newHabit("B")
        val first = repos.progress.complete(a, today) as CompleteOutcome.Completed
        assertFalse(first.reward.completedTheDay)
        val second = repos.progress.complete(b, today) as CompleteOutcome.Completed
        assertTrue(second.reward.completedTheDay)
    }

    @Test
    fun history_isNewestFirst_andIncludesArchivedHabits() = runTest {
        val id = newHabit("Stretch")
        repos.progress.complete(id, today)
        repos.clock.today = today.plusDays(1)
        repos.progress.complete(id, repos.clock.today)
        repos.habits.archiveHabit(id)

        val history = repos.progress.observeCompletionHistory().first()
        assertEquals(listOf(repos.clock.today, today), history.map { it.date })
        assertEquals("Stretch", history.first().habitName)
    }

    @Test
    fun progress_survivesReopeningTheDatabase() = runTest {
        val file = tmp.newFile("pawtiva-test.db")
        file.delete()
        val clock = FixedClock()
        Repos(fileDb(file), clock).run {
            val id = habits.createHabit("Persist", "", "🌿", HabitColor.MINT)
            progress.complete(id, clock.today)
            db.close()
        }
        Repos(fileDb(file), clock).run {
            val p = progress.observeProgress().first()
            assertEquals(10, p.totalXp)
            assertEquals(5, p.balance)
            assertEquals(1, habits.observeActiveHabits().first().size)
            db.close()
        }
    }
}
