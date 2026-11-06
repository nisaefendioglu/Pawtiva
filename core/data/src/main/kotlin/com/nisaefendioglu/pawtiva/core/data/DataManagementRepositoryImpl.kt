package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.database.PawtivaDatabase
import com.nisaefendioglu.pawtiva.core.datastore.PawtivaPreferencesDataSource
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class DataManagementRepositoryImpl @Inject constructor(
    private val db: PawtivaDatabase,
    private val preferences: PawtivaPreferencesDataSource,
    private val clock: PawtivaClock,
) : DataManagementRepository {

    override suspend fun exportJson(): String {
        val prefs = preferences.preferences.first()
        val export = PawtivaExport(
            schemaVersion = EXPORT_SCHEMA_VERSION,
            exportedAt = clock.now().toString(),
            habits = db.habitDao().getAll().map {
                ExportHabit(it.id, it.name, it.description, it.iconKey, it.colorKey, it.createdAtEpochMs, it.archivedAtEpochMs)
            },
            completions = db.completionDao().getAll().map {
                ExportCompletion(it.habitId, it.localDate, it.completedAtEpochMs, it.zoneId, it.xpAwarded, it.currencyAwarded)
            },
            transactions = db.currencyDao().getAll().map {
                ExportTransaction(it.amount, it.type, it.refKey, it.createdAtEpochMs)
            },
            inventory = db.inventoryDao().getAll().map { ExportInventoryItem(it.itemId, it.purchasedAtEpochMs, it.equipped) },
            achievements = db.achievementDao().getAll().map { ExportAchievement(it.achievementId, it.unlockedAtEpochMs) },
            preferences = ExportPreferences(
                petType = prefs.petType?.name,
                petName = prefs.petName,
                themeMode = prefs.themeMode.name,
                dynamicColor = prefs.dynamicColor,
                remindersEnabled = prefs.remindersEnabled,
                reminderTime = prefs.reminderTime.toString(),
                reducePetAnimations = prefs.reducePetAnimations,
                dateFormat = prefs.dateFormat.name,
            ),
        )
        return ExportJson.encodeToString(PawtivaExport.serializer(), export)
    }

    override suspend fun resetAllData() {
        withContext(Dispatchers.IO) { db.clearAllTables() }
        preferences.clear()
    }

    companion object {
        const val EXPORT_SCHEMA_VERSION = 1
        internal val ExportJson = Json { prettyPrint = true; encodeDefaults = true }
    }
}

@Serializable
internal data class PawtivaExport(
    val schemaVersion: Int,
    val exportedAt: String,
    val habits: List<ExportHabit>,
    val completions: List<ExportCompletion>,
    val transactions: List<ExportTransaction>,
    val inventory: List<ExportInventoryItem>,
    val achievements: List<ExportAchievement>,
    val preferences: ExportPreferences,
)

@Serializable
internal data class ExportHabit(
    val id: Long,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val createdAtEpochMs: Long,
    val archivedAtEpochMs: Long?,
)

@Serializable
internal data class ExportCompletion(
    val habitId: Long,
    val localDate: String,
    val completedAtEpochMs: Long,
    val zoneId: String,
    val xpAwarded: Int,
    val coinsAwarded: Int,
)

@Serializable
internal data class ExportTransaction(val amount: Int, val type: String, val reference: String, val createdAtEpochMs: Long)

@Serializable
internal data class ExportInventoryItem(val itemId: String, val purchasedAtEpochMs: Long, val equipped: Boolean)

@Serializable
internal data class ExportAchievement(val achievementId: String, val unlockedAtEpochMs: Long)

@Serializable
internal data class ExportPreferences(
    val petType: String?,
    val petName: String?,
    val themeMode: String,
    val dynamicColor: Boolean,
    val remindersEnabled: Boolean,
    val reminderTime: String,
    val reducePetAnimations: Boolean,
    val dateFormat: String,
)
