package com.nisaefendioglu.pawtiva.feature.habits

import androidx.lifecycle.SavedStateHandle
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HabitEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeHabitRepository()

    private fun editor(id: Long = 0L, handle: SavedStateHandle = SavedStateHandle(mapOf("habitId" to id))) =
        HabitEditorViewModel(repository, handle)

    @Test
    fun newHabit_blankName_cannotBeSaved_andShowsError() = runTest {
        val vm = editor()
        assertFalse(vm.uiState.value.canSave)
        assertNull(vm.uiState.value.visibleNameError)

        vm.onSave()
        assertEquals(HabitValidator.NameError.BLANK, vm.uiState.value.visibleNameError)
        assertTrue(repository.habits.value.isEmpty())

        vm.onNameChange("   ")
        assertEquals(HabitValidator.NameError.BLANK, vm.uiState.value.visibleNameError)
    }

    @Test
    fun tooLongName_isReported() {
        val vm = editor()
        vm.onNameChange("x".repeat(HabitValidator.NAME_MAX_LENGTH + 1))
        assertEquals(HabitValidator.NameError.TOO_LONG, vm.uiState.value.visibleNameError)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun create_savesTrimmedValues_andEmitsSaved() = runTest {
        val vm = editor()
        val events = mutableListOf<HabitEditorEvent>()
        backgroundScope.launch { vm.events.toList(events) }

        vm.onNameChange("  Read 10 pages ")
        vm.onDescriptionChange("Before bed")
        vm.onIconSelected("📚")
        vm.onColorSelected(HabitColor.SKY)
        vm.onSave()
        runCurrent()

        val saved = repository.habits.value.single()
        assertEquals("Read 10 pages", saved.name)
        assertEquals("Before bed", saved.description)
        assertEquals("📚", saved.icon)
        assertEquals(HabitColor.SKY, saved.color)
        assertEquals(listOf<HabitEditorEvent>(HabitEditorEvent.Saved), events)
    }

    @Test
    fun edit_prefillsFields_andUpdatesInPlace() = runTest {
        repository.habits.value = listOf(Habit(5, "Walk", "Evening", "🚶", HabitColor.MINT, Instant.EPOCH))
        val vm = editor(id = 5)
        runCurrent()

        val state = vm.uiState.value
        assertTrue(state.isEditing)
        assertFalse(state.isLoading)
        assertEquals("Walk", state.name)
        assertEquals("Evening", state.description)
        assertEquals(HabitColor.MINT, state.color)

        vm.onNameChange("Long walk")
        vm.onSave()
        runCurrent()
        assertEquals("Long walk", repository.habits.value.single().name)
        assertEquals(1, repository.habits.value.size)
    }

    @Test
    fun delete_archivesHabit_andEmitsDeleted() = runTest {
        repository.habits.value = listOf(Habit(5, "Walk", "", "🚶", HabitColor.MINT, Instant.EPOCH))
        val vm = editor(id = 5)
        val events = mutableListOf<HabitEditorEvent>()
        backgroundScope.launch { vm.events.toList(events) }
        runCurrent()

        vm.onDelete()
        runCurrent()

        assertTrue(repository.habits.value.single().archived)
        assertEquals(listOf<HabitEditorEvent>(HabitEditorEvent.Deleted), events)
    }

    @Test
    fun missingHabit_showsNotFound() = runTest {
        val vm = editor(id = 99)
        runCurrent()
        assertTrue(vm.uiState.value.notFound)
        assertFalse(vm.uiState.value.canSave)
    }

    @Test
    fun formSurvivesProcessDeath() = runTest {
        val handle = SavedStateHandle(mapOf("habitId" to 0L))
        editor(handle = handle).apply {
            onNameChange("Stretch")
            onColorSelected(HabitColor.PEACH)
        }
        val restored = editor(handle = handle)
        assertEquals("Stretch", restored.uiState.value.name)
        assertEquals(HabitColor.PEACH, restored.uiState.value.color)
    }

    @Test
    fun habitsList_deleteArchivesButKeepsData() = runTest {
        repository.habits.value = listOf(Habit(1, "Water", "", "💧", HabitColor.SKY, Instant.EPOCH))
        val vm = HabitsViewModel(repository)
        backgroundScope.launch { vm.uiState.collect {} }
        runCurrent()
        assertEquals(1, (vm.uiState.value as HabitsUiState.Loaded).habits.size)

        vm.onDelete(1)
        runCurrent()
        assertTrue((vm.uiState.value as HabitsUiState.Loaded).habits.isEmpty())
        assertEquals(1, repository.habits.value.size)
    }
}
