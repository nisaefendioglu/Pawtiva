package com.nisaefendioglu.pawtiva.feature.settings

import com.nisaefendioglu.pawtiva.core.model.AppLanguage
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val prefs = FakeUserPreferencesRepository(
        UserPreferences(
            petType = PetType.CAT,
            petName = "Mochi",
            onboardingCompleted = true,
            themeMode = ThemeMode.LIGHT,
            reminderTime = LocalTime.of(20, 0),
        ),
    )
    private val progress = FakeProgressRepository(Progress(totalXp = 250, level = 3, xpIntoLevel = 50, balance = 40))
    private val language = FakeAppLanguageController()
    private val scheduler = FakeReminderScheduler()
    private val data = FakeDataManagementRepository(prefs)

    private fun viewModel() = SettingsViewModel(prefs, progress, language, scheduler, data)

    @Test
    fun uiState_combinesPreferencesProgressAndLanguage() = runTest {
        val vm = viewModel()
        val state = vm.uiState.first { !it.isLoading }
        assertEquals("Mochi", state.preferences.petName)
        assertEquals(3, state.progress.level)
        assertEquals(AppLanguage.SYSTEM, state.language)
    }

    @Test
    fun language_isAppliedImmediately() {
        val vm = viewModel()
        vm.setLanguage(AppLanguage.ENGLISH)
        assertEquals(AppLanguage.ENGLISH, language.language)
    }

    @Test
    fun themeAndDateFormat_areSaved() {
        val vm = viewModel()
        vm.setTheme(ThemeMode.DARK)
        vm.setDateFormat(DateFormatPreference.ISO)
        vm.setDynamicColor(true)
        assertEquals(ThemeMode.DARK, prefs.state.value.themeMode)
        assertEquals(DateFormatPreference.ISO, prefs.state.value.dateFormat)
        assertTrue(prefs.state.value.dynamicColor)
    }

    @Test
    fun changingPet_changesOnlyTheSpecies() {
        val before = prefs.state.value
        val progressBefore = progress.state.value
        viewModel().changePet(PetType.DOG)
        assertEquals(before.copy(petType = PetType.DOG), prefs.state.value)
        assertEquals(progressBefore, progress.state.value)
        assertEquals(0, data.resetCount)
    }

    @Test
    fun rename_trimsClearsAndRejectsTooLong() {
        val vm = viewModel()
        assertTrue(vm.renamePet("  Pamuk  "))
        assertEquals("Pamuk", prefs.state.value.petName)

        assertTrue(vm.renamePet("   "))
        assertNull(prefs.state.value.petName)

        assertFalse(vm.renamePet("x".repeat(UserPreferences.PET_NAME_MAX_LENGTH + 1)))
        assertNull(prefs.state.value.petName)
    }

    @Test
    fun reminderToggle_schedulesAndCancels() {
        val vm = viewModel()
        vm.setRemindersEnabled(true)
        assertTrue(prefs.state.value.remindersEnabled)
        assertEquals(LocalTime.of(20, 0), scheduler.scheduledAt)

        vm.setRemindersEnabled(false)
        assertFalse(prefs.state.value.remindersEnabled)
        assertNull(scheduler.scheduledAt)
        assertEquals(1, scheduler.cancelCount)
    }

    @Test
    fun reminderTime_reschedulesOnlyWhenEnabled() {
        val vm = viewModel()
        vm.setReminderTime(LocalTime.of(8, 15))
        assertEquals(LocalTime.of(8, 15), prefs.state.value.reminderTime)
        assertNull(scheduler.scheduledAt)

        vm.setRemindersEnabled(true)
        vm.setReminderTime(LocalTime.of(9, 0))
        assertEquals(LocalTime.of(9, 0), scheduler.scheduledAt)
    }

    @Test
    fun reset_cancelsReminderAndDeletesData() = runTest {
        val vm = viewModel()
        vm.setRemindersEnabled(true)
        vm.resetAllData()
        assertEquals(1, data.resetCount)
        assertEquals(AppLanguage.SYSTEM, language.language)
        assertNull(scheduler.scheduledAt)
        assertFalse(prefs.state.value.onboardingCompleted)
        assertEquals(SettingsEvent.ResetDone, vm.eventFlow.first())
    }

    @Test
    fun export_reportsSuccessAndFailure() = runTest {
        val vm = viewModel()
        var written = ""
        vm.export { written = it }
        assertTrue(written.contains("habits"))
        assertEquals(SettingsEvent.ExportSucceeded, vm.eventFlow.first())

        data.failExport = true
        vm.export { written = it }
        assertEquals(SettingsEvent.ExportFailed, vm.eventFlow.first())
    }
}
