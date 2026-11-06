package com.nisaefendioglu.pawtiva.core.model

import java.text.BreakIterator
import java.time.Instant
import java.time.LocalDate

/** Card tint for a habit. Stored by name; new colors are only ever appended. */
enum class HabitColor { LAVENDER, MINT, PEACH, PINK, SKY, SUN }

data class Habit(
    val id: Long,
    val name: String,
    val description: String,
    val icon: String,
    val color: HabitColor,
    val createdAt: Instant,
    val archived: Boolean = false,
)

/** One habit marked done on one calendar day. At most one per habit per [date]. */
data class HabitCompletion(
    val habitId: Long,
    val date: LocalDate,
    val completedAt: Instant,
    val xpAwarded: Int,
    val coinsAwarded: Int,
)

/** A completion joined with the habit's name, for history lists. */
data class CompletionRecord(
    val habitId: Long,
    val habitName: String,
    val habitIcon: String,
    val date: LocalDate,
    val completedAt: Instant,
)

/** A habit with its done-state for one day, as shown on the home checklist. */
data class DailyHabit(val habit: Habit, val completed: Boolean)

/** Icons offered in the habit editor. Emoji render with the system font; no licensing needed. */
object HabitIcons {
    val all: List<String> = listOf(
        "💧", "📚", "🏃", "🧘", "🥗", "😴", "✍️", "🎵", "🧹", "💊", "🌿", "☀️", "🦷", "🚶", "🍎", "💤",
    )
    const val DEFAULT = "🌿"

    /**
     * Accepts exactly one emoji typed from the device keyboard (skin tones and joined emoji
     * included) and returns it trimmed, or null for text, numbers, several emoji or blanks.
     */
    fun normalizeCustom(input: String): String? {
        val text = input.trim()
        if (text.isEmpty()) return null
        val graphemes = BreakIterator.getCharacterInstance()
        graphemes.setText(text)
        val end = graphemes.next()
        if (end == BreakIterator.DONE || end != text.length) return null
        val codePoints = text.codePoints().toArray()
        if (codePoints.any { Character.isLetterOrDigit(it) || Character.isWhitespace(it) }) return null
        val looksLikeEmoji = codePoints.any { cp ->
            Character.getType(cp) == Character.OTHER_SYMBOL.toInt() || cp >= 0x1F000
        }
        return if (looksLikeEmoji) text else null
    }
}
