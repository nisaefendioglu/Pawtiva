package com.nisaefendioglu.pawtiva.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.time.LocalTime

/** Runs against a real DataStore file on the JVM; "restart" = a new DataStore on the same file. */
@OptIn(ExperimentalCoroutinesApi::class)
class PawtivaPreferencesDataSourceTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun newStore(file: File, scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    private fun newSource(file: File, scope: CoroutineScope) =
        PawtivaPreferencesDataSource(newStore(file, scope))

    @Test
    fun defaults_whenNothingStored() = runTest {
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val source = newSource(File(tmp.root, "a.preferences_pb"), scope)

        val prefs = source.preferences.first()

        assertEquals(UserPreferences(), prefs)
        assertNull(prefs.petType)
        assertFalse(prefs.onboardingCompleted)
        scope.cancel()
    }

    @Test
    fun choices_surviveRestart() = runTest {
        val file = File(tmp.root, "b.preferences_pb")
        val firstScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val first = newSource(file, firstScope)
        first.setPetType(PetType.DOG)
        first.setPetName("  Waffle  ")
        first.setThemeMode(ThemeMode.DARK)
        first.setReminders(enabled = true, time = LocalTime.of(9, 30))
        first.setOnboardingCompleted(true)
        firstScope.cancel()
        advanceUntilIdle() // lets DataStore release the file before it is reopened

        val secondScope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val prefs = newSource(file, secondScope).preferences.first()

        assertEquals(PetType.DOG, prefs.petType)
        assertEquals("Waffle", prefs.petName)
        assertEquals(ThemeMode.DARK, prefs.themeMode)
        assertTrue(prefs.remindersEnabled)
        assertEquals(LocalTime.of(9, 30), prefs.reminderTime)
        assertTrue(prefs.onboardingCompleted)
        secondScope.cancel()
    }

    @Test
    fun changingPet_keepsEverythingElse() = runTest {
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val source = newSource(File(tmp.root, "c.preferences_pb"), scope)
        source.setPetType(PetType.CAT)
        source.setPetName("Mochi")
        source.setOnboardingCompleted(true)

        source.setPetType(PetType.DOG)

        val prefs = source.preferences.first()
        assertEquals(PetType.DOG, prefs.petType)
        assertEquals("Mochi", prefs.petName)
        assertTrue(prefs.onboardingCompleted)
        scope.cancel()
    }

    @Test
    fun petName_isTrimmedLimitedAndBlankClears() = runTest {
        val scope = TestScope(UnconfinedTestDispatcher(testScheduler))
        val source = newSource(File(tmp.root, "d.preferences_pb"), scope)

        source.setPetName("x".repeat(40))
        assertEquals(UserPreferences.PET_NAME_MAX_LENGTH, source.preferences.first().petName?.length)

        source.setPetName("   ")
        assertNull(source.preferences.first().petName)
        scope.cancel()
    }
}
