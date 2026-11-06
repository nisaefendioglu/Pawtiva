package com.nisaefendioglu.pawtiva.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nisaefendioglu.pawtiva.core.database.HabitCompletionEntity
import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** Mutable test clock pinned to noon of [today] in [zone]. */
class FixedClock(
    var today: LocalDate = LocalDate.of(2026, 11, 5),
    var zoneId: ZoneId = ZoneOffset.UTC,
) : PawtivaClock {
    override fun now(): Instant = today.atTime(12, 0).atZone(zoneId).toInstant()
    override fun zone(): ZoneId = zoneId
}

fun inMemoryDb(): PawtivaDatabase =
    Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), PawtivaDatabase::class.java)
        .allowMainThreadQueries()
        .build()

fun fileDb(file: File): PawtivaDatabase =
    Room.databaseBuilder(ApplicationProvider.getApplicationContext<Context>(), PawtivaDatabase::class.java, file.absolutePath)
        .allowMainThreadQueries()
        .build()

/** Bundles real repository implementations over one database. */
class Repos(val db: PawtivaDatabase, val clock: FixedClock = FixedClock()) {
    private val unlocker = AchievementUnlocker(db, clock)
    val habits: HabitRepository = HabitRepositoryImpl(db, clock)
    val progress: ProgressRepository = ProgressRepositoryImpl(db, clock, unlocker)
    val shop: ShopRepository = ShopRepositoryImpl(db, clock, unlocker)
    val achievements: AchievementRepository = AchievementRepositoryImpl(db)

    /** Inserts raw XP for test setup (bypasses the 7-day window), one row per fake habit. */
    suspend fun grantXp(xp: Int) {
        db.completionDao().insert(
            HabitCompletionEntity(
                habitId = -1_000L - xp,
                localDate = "2020-01-01",
                completedAtEpochMs = 0,
                zoneId = "UTC",
                xpAwarded = xp,
                currencyAwarded = 0,
            ),
        )
    }
}
