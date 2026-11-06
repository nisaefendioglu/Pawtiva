package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.database.HabitEntity
import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.HabitIcons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

internal class HabitRepositoryImpl @Inject constructor(
    private val db: PawtivaDatabase,
    private val clock: PawtivaClock,
) : HabitRepository {

    private val habitDao get() = db.habitDao()
    private val completionDao get() = db.completionDao()

    override fun observeActiveHabits(): Flow<List<Habit>> =
        habitDao.observeActive().map { list -> list.map { it.toModel() } }.distinctUntilChanged()

    override fun observeHabit(id: Long): Flow<Habit?> =
        habitDao.observeById(id).map { it?.toModel() }.distinctUntilChanged()

    override fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>> =
        combine(habitDao.observeActive(), completionDao.observeHabitIdsOn(date.toString())) { habits, doneIds ->
            // Every active habit is listed on every editable day, so a habit added today can
            // also be ticked off for the previous days of the 7-day window.
            val done = doneIds.toSet()
            habits.map { DailyHabit(habit = it.toModel(), completed = it.id in done) }
        }.distinctUntilChanged()

    override suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long {
        requireValidName(name)
        return habitDao.insert(
            HabitEntity(
                name = name.trim(),
                description = cleanDescription(description),
                iconKey = icon.ifBlank { HabitIcons.DEFAULT },
                colorKey = color.name,
                createdAtEpochMs = clock.now().toEpochMilli(),
            ),
        )
    }

    override suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor) {
        requireValidName(name)
        val existing = habitDao.getById(id) ?: throw IllegalArgumentException("Habit $id does not exist")
        habitDao.update(
            existing.copy(
                name = name.trim(),
                description = cleanDescription(description),
                iconKey = icon.ifBlank { HabitIcons.DEFAULT },
                colorKey = color.name,
            ),
        )
    }

    override suspend fun archiveHabit(id: Long) {
        habitDao.archive(id, clock.now().toEpochMilli())
    }

    private fun requireValidName(name: String) {
        val error = HabitValidator.validateName(name)
        require(error == null) { "Invalid habit name: $error" }
    }

    private fun cleanDescription(description: String): String =
        description.trim().take(HabitValidator.DESCRIPTION_MAX_LENGTH)
}
