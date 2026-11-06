package com.nisaefendioglu.pawtiva.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.datastore.PawtivaPreferencesDataSource
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class DataManagementRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val repos = Repos(inMemoryDb())
    private val storeScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var preferences: PawtivaPreferencesDataSource

    private fun management(): DataManagementRepository {
        preferences = PawtivaPreferencesDataSource(
            PreferenceDataStoreFactory.create(scope = storeScope, produceFile = { File(tmp.root, "prefs.preferences_pb") }),
        )
        return DataManagementRepositoryImpl(repos.db, preferences, repos.clock)
    }

    @After
    fun close() {
        storeScope.cancel()
        repos.db.close()
    }

    @Test
    fun export_containsEverything_andIsValidJson() = runTest {
        val manager = management()
        preferences.setPetType(PetType.DOG)
        val id = repos.habits.createHabit("Water", "", "💧", HabitColor.SKY)
        repeat(8) { repos.habits.createHabit("Extra $it", "", "🌿", HabitColor.MINT) }
        repos.habits.observeActiveHabits().first().forEach { repos.progress.complete(it.id, repos.clock.today) }
        repos.shop.purchase(AccessoryId.BASIC_CAP)

        val json = kotlinx.serialization.json.Json.parseToJsonElement(manager.exportJson()).jsonObject

        assertEquals(1, json.getValue("schemaVersion").jsonPrimitive.int)
        assertEquals(9, json.getValue("habits").jsonArray.size)
        assertEquals(9, json.getValue("completions").jsonArray.size)
        assertEquals(10, json.getValue("transactions").jsonArray.size)
        assertEquals(1, json.getValue("inventory").jsonArray.size)
        assertTrue(json.getValue("achievements").jsonArray.isNotEmpty())
        assertEquals("DOG", json.getValue("preferences").jsonObject.getValue("petType").jsonPrimitive.content)
        assertTrue(id > 0)
    }

    @Test
    fun reset_emptiesTablesAndPreferences() = runTest {
        val manager = management()
        preferences.setPetType(PetType.CAT)
        preferences.setOnboardingCompleted(true)
        val id = repos.habits.createHabit("Water", "", "💧", HabitColor.SKY)
        repos.progress.complete(id, repos.clock.today)

        manager.resetAllData()

        assertTrue(repos.habits.observeActiveHabits().first().isEmpty())
        assertEquals(0, repos.progress.observeProgress().first().totalXp)
        assertEquals(0, repos.progress.observeProgress().first().balance)
        val prefs = preferences.preferences.first()
        assertNull(prefs.petType)
        assertEquals(false, prefs.onboardingCompleted)
    }
}
