package com.nisaefendioglu.pawtiva.core.domain

import com.nisaefendioglu.pawtiva.core.model.PetMood

/**
 * The pet's resting mood, derived only from today. It never punishes: a missed day simply
 * starts fresh at [PetMood.NORMAL]. [PetMood.CELEBRATING] is a short reaction (level-up,
 * all habits done) shown by the UI on top of this resting mood, so it is not returned here.
 */
object PetMoodCalculator {

    const val SLEEP_START_HOUR = 22
    const val SLEEP_END_HOUR = 6

    fun restingMood(completedToday: Int, activeHabitsToday: Int, hourOfDay: Int): PetMood {
        require(hourOfDay in 0..23) { "hourOfDay must be 0..23, was $hourOfDay" }
        val completed = completedToday.coerceAtLeast(0)
        val total = activeHabitsToday.coerceAtLeast(0)
        return when {
            total > 0 && completed * 2 >= total -> PetMood.EXCITED
            completed > 0 -> PetMood.HAPPY
            isSleepTime(hourOfDay) -> PetMood.SLEEPY
            else -> PetMood.NORMAL
        }
    }

    fun isSleepTime(hourOfDay: Int): Boolean = hourOfDay >= SLEEP_START_HOUR || hourOfDay < SLEEP_END_HOUR

    /** True when today's last remaining habit has just been completed. */
    fun completesTheDay(completedBefore: Int, completedAfter: Int, activeHabitsToday: Int): Boolean =
        activeHabitsToday > 0 && completedBefore < activeHabitsToday && completedAfter >= activeHabitsToday
}
