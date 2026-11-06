package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.AchievementId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LevelCalculatorTest {
    @Test
    fun levelFormula_matchesSpecExamples() {
        assertEquals(1, LevelCalculator.level(0))
        assertEquals(1, LevelCalculator.level(90))
        assertEquals(1, LevelCalculator.level(99))
        assertEquals(2, LevelCalculator.level(100))
        assertEquals(3, LevelCalculator.level(250))
    }

    @Test
    fun progressIntoLevel_andRemaining() {
        assertEquals(50, LevelCalculator.xpIntoLevel(250))
        assertEquals(50, LevelCalculator.xpToNextLevel(250))
        assertEquals(100, LevelCalculator.xpToNextLevel(0))
    }

    @Test
    fun multipleLevelUps_inOneJump_andNegativeXpIsClamped() {
        assertEquals(2, LevelCalculator.level(250) - LevelCalculator.level(90))
        assertEquals(1, LevelCalculator.level(-50))
        assertEquals(0, LevelCalculator.xpIntoLevel(-50))
    }

    @Test
    fun rewardsAreCentral() {
        assertEquals(10, RewardPolicy.XP_PER_COMPLETION)
        assertTrue(RewardPolicy.COINS_PER_COMPLETION > 0)
    }
}

class EditableDaysTest {
    private val today = LocalDate.of(2026, 11, 5)

    @Test
    fun todayAndSixDaysBack_areEditable() {
        assertTrue(EditableDays.isEditable(today, today))
        assertTrue(EditableDays.isEditable(today.minusDays(6), today))
        assertFalse(EditableDays.isEditable(today.minusDays(7), today))
        assertFalse(EditableDays.isEditable(today.plusDays(1), today))
    }

    @Test
    fun window_isSevenDaysOldestFirst() {
        val window = EditableDays.window(today)
        assertEquals(7, window.size)
        assertEquals(today.minusDays(6), window.first())
        assertEquals(today, window.last())
    }
}

class HabitValidatorTest {
    @Test
    fun names_areValidated() {
        assertEquals(HabitValidator.NameError.BLANK, HabitValidator.validateName(""))
        assertEquals(HabitValidator.NameError.BLANK, HabitValidator.validateName("   "))
        assertEquals(HabitValidator.NameError.TOO_LONG, HabitValidator.validateName("x".repeat(41)))
        assertNull(HabitValidator.validateName("  Su iç  "))
        assertNull(HabitValidator.validateName("Çiçekleri sula ğüşıöç"))
    }
}

class AchievementEvaluatorTest {
    private val day = LocalDate.of(2026, 11, 1)

    private fun stats(
        completions: Int = 0,
        dates: Set<LocalDate> = emptySet(),
        level: Int = 1,
        purchases: Int = 0,
        perfect: Boolean = false,
    ) = AchievementStats(completions, dates, level, purchases, perfect)

    @Test
    fun nothingDone_unlocksNothing() {
        assertTrue(AchievementEvaluator.unlocked(stats()).isEmpty())
    }

    @Test
    fun countBasedAchievements() {
        assertEquals(setOf(AchievementId.FIRST_STEP), AchievementEvaluator.unlocked(stats(completions = 1)))
        assertTrue(AchievementId.BEGINNER in AchievementEvaluator.unlocked(stats(completions = 10)))
        assertFalse(AchievementId.DETERMINED in AchievementEvaluator.unlocked(stats(completions = 49)))
        assertTrue(AchievementId.DETERMINED in AchievementEvaluator.unlocked(stats(completions = 50)))
        assertEquals(0.5f, AchievementEvaluator.progress(AchievementId.BEGINNER, stats(completions = 5)), 0.0001f)
    }

    @Test
    fun levelPurchaseAndPerfectDay() {
        assertFalse(AchievementId.GROWING_TOGETHER in AchievementEvaluator.unlocked(stats(level = 4)))
        assertTrue(AchievementId.GROWING_TOGETHER in AchievementEvaluator.unlocked(stats(level = 5)))
        assertTrue(AchievementId.FIRST_PURCHASE in AchievementEvaluator.unlocked(stats(purchases = 1)))
        assertTrue(AchievementId.PERFECT_DAY in AchievementEvaluator.unlocked(stats(perfect = true)))
    }

    @Test
    fun consistency_needsSevenConsecutiveDays() {
        val six = (0L..5L).map { day.plusDays(it) }.toSet()
        assertEquals(6, AchievementEvaluator.longestStreak(six))
        assertFalse(AchievementId.CONSISTENCY in AchievementEvaluator.unlocked(stats(dates = six)))

        val gap = six + day.plusDays(7)
        assertEquals(6, AchievementEvaluator.longestStreak(gap))

        val seven = six + day.plusDays(6)
        assertEquals(7, AchievementEvaluator.longestStreak(seven))
        assertTrue(AchievementId.CONSISTENCY in AchievementEvaluator.unlocked(stats(dates = seven)))
        assertEquals(0, AchievementEvaluator.longestStreak(emptySet()))
    }
}
