package com.nisaefendioglu.pawtiva.core.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max

/** All reward amounts live here, so balancing the game is a one-line change. */
object RewardPolicy {
    const val XP_PER_COMPLETION = 10
    const val COINS_PER_COMPLETION = 5
}

/** level = floor(totalXp / 100) + 1; progress into the level = totalXp % 100. */
object LevelCalculator {
    const val XP_PER_LEVEL = 100

    fun level(totalXp: Int): Int = max(totalXp, 0) / XP_PER_LEVEL + 1

    fun xpIntoLevel(totalXp: Int): Int = max(totalXp, 0) % XP_PER_LEVEL

    fun xpToNextLevel(totalXp: Int): Int = XP_PER_LEVEL - xpIntoLevel(totalXp)
}

/** Completions can be added or undone for today and the 6 days before it (7 days in total). */
object EditableDays {
    const val WINDOW_DAYS = 7L

    fun isEditable(date: LocalDate, today: LocalDate): Boolean =
        !date.isAfter(today) && !date.isBefore(today.minusDays(WINDOW_DAYS - 1))

    fun window(today: LocalDate): List<LocalDate> =
        (WINDOW_DAYS - 1 downTo 0).map { today.minusDays(it) }
}

object HabitValidator {
    const val NAME_MAX_LENGTH = 40
    const val DESCRIPTION_MAX_LENGTH = 120

    enum class NameError { BLANK, TOO_LONG }

    fun validateName(name: String): NameError? = when {
        name.isBlank() -> NameError.BLANK
        name.trim().length > NAME_MAX_LENGTH -> NameError.TOO_LONG
        else -> null
    }
}

/** Source of "now". Injected so tests can pin the date and time zone. */
interface PawtivaClock {
    fun now(): Instant
    fun zone(): ZoneId
    fun today(): LocalDate = now().atZone(zone()).toLocalDate()
}
