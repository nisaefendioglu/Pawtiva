package com.nisaefendioglu.pawtiva.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Version history:
 * 1 — initial schema (habits, completions, currency ledger, inventory, achievements).
 *
 * Future versions must add an explicit Migration and a MigrationTestHelper test; destructive
 * fallback is deliberately not enabled, so a missing migration fails loudly instead of
 * silently wiping user progress.
 */
@Database(
    entities = [
        HabitEntity::class,
        HabitCompletionEntity::class,
        CurrencyTransactionEntity::class,
        InventoryEntity::class,
        AchievementEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PawtivaDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun completionDao(): CompletionDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        const val NAME = "pawtiva.db"
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PawtivaDatabase =
        Room.databaseBuilder(context, PawtivaDatabase::class.java, PawtivaDatabase.NAME).build()

    @Provides fun provideHabitDao(db: PawtivaDatabase): HabitDao = db.habitDao()
    @Provides fun provideCompletionDao(db: PawtivaDatabase): CompletionDao = db.completionDao()
    @Provides fun provideCurrencyDao(db: PawtivaDatabase): CurrencyDao = db.currencyDao()
    @Provides fun provideInventoryDao(db: PawtivaDatabase): InventoryDao = db.inventoryDao()
    @Provides fun provideAchievementDao(db: PawtivaDatabase): AchievementDao = db.achievementDao()
}
