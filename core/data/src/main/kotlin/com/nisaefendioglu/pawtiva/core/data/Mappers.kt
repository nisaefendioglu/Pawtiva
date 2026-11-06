package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.database.CompletionWithHabit
import com.nisaefendioglu.pawtiva.core.database.HabitEntity
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.HabitIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun HabitEntity.toModel(): Habit = Habit(
    id = id,
    name = name,
    description = description,
    icon = iconKey.ifBlank { HabitIcons.DEFAULT },
    color = HabitColor.entries.firstOrNull { it.name == colorKey } ?: HabitColor.LAVENDER,
    createdAt = Instant.ofEpochMilli(createdAtEpochMs),
    archived = archivedAtEpochMs != null,
)

/** Calendar day the habit was created on, in [zone]. */
internal fun HabitEntity.createdOn(zone: ZoneId): LocalDate =
    Instant.ofEpochMilli(createdAtEpochMs).atZone(zone).toLocalDate()

internal fun CompletionWithHabit.toModel(): CompletionRecord = CompletionRecord(
    habitId = habitId,
    habitName = habitName,
    habitIcon = habitIcon.ifBlank { HabitIcons.DEFAULT },
    date = LocalDate.parse(localDate),
    completedAt = Instant.ofEpochMilli(completedAtEpochMs),
)
