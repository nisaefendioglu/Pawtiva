package com.nisaefendioglu.pawtiva.core.model

import java.time.LocalTime

/** In-app language choice. [SYSTEM] follows the device language. */
enum class AppLanguage { SYSTEM, TURKISH, ENGLISH }

/**
 * Simple user preferences stored in DataStore. Progress data (habits, XP, coins, inventory)
 * lives in Room and is never touched when these change.
 */
data class UserPreferences(
    val petType: PetType? = null,
    val petName: String? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val remindersEnabled: Boolean = false,
    val reminderTime: LocalTime = DEFAULT_REMINDER_TIME,
    val reducePetAnimations: Boolean = false,
    val dateFormat: DateFormatPreference = DateFormatPreference.SYSTEM,
) {
    companion object {
        val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(20, 0)
        const val PET_NAME_MAX_LENGTH = 20
    }
}
