package com.nisaefendioglu.pawtiva.core.data

import com.nisaefendioglu.pawtiva.core.datastore.PawtivaPreferencesDataSource
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.time.LocalTime
import javax.inject.Inject

/** Single entry point for user preferences. Changing the pet never touches progress data. */
interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setPetType(type: PetType)
    suspend fun setPetName(name: String?)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setReminders(enabled: Boolean, time: LocalTime)
    suspend fun setReducePetAnimations(reduce: Boolean)
    suspend fun setDateFormat(format: DateFormatPreference)

    /** Removes every preference (used by reset). */
    suspend fun clear()
}

internal class DataStoreUserPreferencesRepository @Inject constructor(
    private val source: PawtivaPreferencesDataSource,
) : UserPreferencesRepository {
    override val preferences: Flow<UserPreferences> = source.preferences
    override suspend fun setPetType(type: PetType) { source.setPetType(type) }
    override suspend fun setPetName(name: String?) { source.setPetName(name) }
    override suspend fun setThemeMode(mode: ThemeMode) { source.setThemeMode(mode) }
    override suspend fun setDynamicColor(enabled: Boolean) { source.setDynamicColor(enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) { source.setOnboardingCompleted(completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) { source.setReminders(enabled, time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) { source.setReducePetAnimations(reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) { source.setDateFormat(format) }
    override suspend fun clear() { source.clear() }
}
