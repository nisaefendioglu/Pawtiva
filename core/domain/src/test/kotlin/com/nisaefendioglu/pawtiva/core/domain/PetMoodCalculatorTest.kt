package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.PetMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetMoodCalculatorTest {

    private fun mood(done: Int, total: Int, hour: Int = 12) = PetMoodCalculator.restingMood(done, total, hour)

    @Test
    fun noHabitsOrNothingDone_isCalmDuringTheDay() {
        assertEquals(PetMood.NORMAL, mood(0, 0))
        assertEquals(PetMood.NORMAL, mood(0, 4))
    }

    @Test
    fun someProgress_isHappy_halfOrMore_isExcited() {
        assertEquals(PetMood.HAPPY, mood(1, 4))
        assertEquals(PetMood.EXCITED, mood(2, 4))
        assertEquals(PetMood.EXCITED, mood(3, 3))
        assertEquals(PetMood.EXCITED, mood(1, 1))
    }

    @Test
    fun lateNightWithoutProgress_isSleepy_butProgressWins() {
        assertEquals(PetMood.SLEEPY, mood(0, 3, hour = 23))
        assertEquals(PetMood.SLEEPY, mood(0, 3, hour = 5))
        assertEquals(PetMood.NORMAL, mood(0, 3, hour = 6))
        assertEquals(PetMood.HAPPY, mood(1, 3, hour = 23))
    }

    @Test
    fun negativeInputs_areTreatedAsZero() {
        assertEquals(PetMood.NORMAL, mood(-2, -1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidHour_isRejected() {
        mood(0, 0, hour = 24)
    }

    @Test
    fun completesTheDay_onlyOnTheLastHabit() {
        assertTrue(PetMoodCalculator.completesTheDay(completedBefore = 2, completedAfter = 3, activeHabitsToday = 3))
        assertFalse(PetMoodCalculator.completesTheDay(completedBefore = 1, completedAfter = 2, activeHabitsToday = 3))
        assertFalse(PetMoodCalculator.completesTheDay(completedBefore = 3, completedAfter = 3, activeHabitsToday = 3))
        assertFalse(PetMoodCalculator.completesTheDay(completedBefore = 0, completedAfter = 0, activeHabitsToday = 0))
    }
}
