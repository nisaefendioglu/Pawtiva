package com.nisaefendioglu.pawtiva.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime
import javax.inject.Inject

/**
 * Reads and writes [UserPreferences] in Preferences DataStore. Unknown or corrupt values fall
 * back to defaults instead of crashing. Enum values are stored by name, so reordering an enum
 * never changes what a user picked.
 */
class PawtivaPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toUserPreferences() }
        .distinctUntilChanged()

    suspend fun setPetType(type: PetType) = dataStore.edit { it[Keys.PET_TYPE] = type.name }

    suspend fun setPetName(name: String?) = dataStore.edit {
        val clean = name?.trim()?.take(UserPreferences.PET_NAME_MAX_LENGTH)
        if (clean.isNullOrEmpty()) it.remove(Keys.PET_NAME) else it[Keys.PET_NAME] = clean
    }

    suspend fun setThemeMode(mode: ThemeMode) = dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }

    suspend fun setOnboardingCompleted(completed: Boolean) =
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }

    suspend fun setReminders(enabled: Boolean, time: LocalTime) = dataStore.edit {
        it[Keys.REMINDERS_ENABLED] = enabled
        it[Keys.REMINDER_MINUTE_OF_DAY] = time.hour * 60 + time.minute
    }

    suspend fun setReducePetAnimations(reduce: Boolean) =
        dataStore.edit { it[Keys.REDUCE_PET_ANIMATIONS] = reduce }

    suspend fun setDateFormat(format: DateFormatPreference) = dataStore.edit { it[Keys.DATE_FORMAT] = format.name }

    /** Clears every preference (used by "reset app data" in settings). */
    suspend fun clear() = dataStore.edit { it.clear() }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val minute = this[Keys.REMINDER_MINUTE_OF_DAY]
        return UserPreferences(
            petType = this[Keys.PET_TYPE]?.let { name -> PetType.entries.firstOrNull { it.name == name } },
            petName = this[Keys.PET_NAME],
            themeMode = this[Keys.THEME_MODE]
                ?.let { name -> ThemeMode.entries.firstOrNull { it.name == name } }
                ?: ThemeMode.SYSTEM,
            dynamicColor = this[Keys.DYNAMIC_COLOR] ?: false,
            onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: false,
            remindersEnabled = this[Keys.REMINDERS_ENABLED] ?: false,
            reminderTime = if (minute != null && minute in 0 until 24 * 60) {
                LocalTime.of(minute / 60, minute % 60)
            } else {
                UserPreferences.DEFAULT_REMINDER_TIME
            },
            reducePetAnimations = this[Keys.REDUCE_PET_ANIMATIONS] ?: false,
            dateFormat = this[Keys.DATE_FORMAT]
                ?.let { name -> DateFormatPreference.entries.firstOrNull { it.name == name } }
                ?: DateFormatPreference.SYSTEM,
        )
    }

    private object Keys {
        val PET_TYPE = stringPreferencesKey("pet_type")
        val PET_NAME = stringPreferencesKey("pet_name")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_MINUTE_OF_DAY = intPreferencesKey("reminder_minute_of_day")
        val REDUCE_PET_ANIMATIONS = booleanPreferencesKey("reduce_pet_animations")
        val DATE_FORMAT = stringPreferencesKey("date_format")
    }
}
