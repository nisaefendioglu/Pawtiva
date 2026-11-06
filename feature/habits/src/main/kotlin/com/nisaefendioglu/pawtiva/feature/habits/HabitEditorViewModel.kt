package com.nisaefendioglu.pawtiva.feature.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.domain.HabitValidator
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.HabitIcons
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HabitEditorEvent {
    data object Saved : HabitEditorEvent
    data object Deleted : HabitEditorEvent
    data object SaveFailed : HabitEditorEvent
}

data class HabitEditorUiState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val name: String = "",
    val description: String = "",
    val icon: String = HabitIcons.DEFAULT,
    val color: HabitColor = HabitColor.LAVENDER,
    /** The error is shown only after the user has typed in the field or tried to save. */
    val showNameError: Boolean = false,
    val isSaving: Boolean = false,
) {
    val nameError: HabitValidator.NameError? get() = HabitValidator.validateName(name)
    val visibleNameError: HabitValidator.NameError? get() = if (showNameError) nameError else null
    val canSave: Boolean get() = nameError == null && !isSaving && !isLoading && !notFound
}

/**
 * Create or edit one habit. The route argument "habitId" (0 = new) comes through
 * [SavedStateHandle]; form fields are mirrored there so they survive process death.
 */
@HiltViewModel
class HabitEditorViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val habitId: Long = savedStateHandle.get<Long>(KEY_HABIT_ID) ?: 0L

    private val _uiState = MutableStateFlow(
        HabitEditorUiState(
            isEditing = habitId != 0L,
            isLoading = habitId != 0L && savedStateHandle.get<Boolean>(KEY_PREFILLED) != true,
            name = savedStateHandle[KEY_NAME] ?: "",
            description = savedStateHandle[KEY_DESCRIPTION] ?: "",
            icon = savedStateHandle[KEY_ICON] ?: HabitIcons.DEFAULT,
            color = savedStateHandle.get<String>(KEY_COLOR)
                ?.let { name -> HabitColor.entries.firstOrNull { it.name == name } }
                ?: HabitColor.LAVENDER,
        ),
    )
    val uiState: StateFlow<HabitEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<HabitEditorEvent>(Channel.BUFFERED)
    val events: Flow<HabitEditorEvent> = _events.receiveAsFlow()

    init {
        if (_uiState.value.isLoading) {
            viewModelScope.launch {
                val habit = habitRepository.observeHabit(habitId).first()
                if (habit == null || habit.archived) {
                    _uiState.update { it.copy(isLoading = false, notFound = true) }
                } else {
                    savedStateHandle[KEY_PREFILLED] = true
                    update {
                        it.copy(
                            isLoading = false,
                            name = habit.name,
                            description = habit.description,
                            icon = habit.icon,
                            color = habit.color,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = update {
        it.copy(name = value.replace("\n", "").take(HabitValidator.NAME_MAX_LENGTH + 10), showNameError = true)
    }

    fun onDescriptionChange(value: String) = update {
        it.copy(description = value.take(HabitValidator.DESCRIPTION_MAX_LENGTH))
    }

    fun onIconSelected(icon: String) = update { it.copy(icon = icon) }

    fun onColorSelected(color: HabitColor) = update { it.copy(color = color) }

    fun onSave() {
        val state = _uiState.value
        if (state.nameError != null) {
            update { it.copy(showNameError = true) }
            return
        }
        if (!state.canSave) return
        update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val name = state.name.trim()
                val description = state.description.trim()
                if (state.isEditing) {
                    habitRepository.updateHabit(habitId, name, description, state.icon, state.color)
                } else {
                    habitRepository.createHabit(name, description, state.icon, state.color)
                }
                _events.send(HabitEditorEvent.Saved)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (failure: Exception) {
                update { it.copy(isSaving = false, showNameError = true) }
                _events.send(HabitEditorEvent.SaveFailed)
            }
        }
    }

    fun onDelete() {
        if (!_uiState.value.isEditing) return
        viewModelScope.launch {
            habitRepository.archiveHabit(habitId)
            _events.send(HabitEditorEvent.Deleted)
        }
    }

    private fun update(transform: (HabitEditorUiState) -> HabitEditorUiState) {
        _uiState.update(transform)
        val state = _uiState.value
        savedStateHandle[KEY_NAME] = state.name
        savedStateHandle[KEY_DESCRIPTION] = state.description
        savedStateHandle[KEY_ICON] = state.icon
        savedStateHandle[KEY_COLOR] = state.color.name
    }

    companion object {
        /** Navigation argument name; must match the app's HabitEditorRoute property. */
        const val KEY_HABIT_ID = "habitId"
        private const val KEY_PREFILLED = "editor_prefilled"
        private const val KEY_NAME = "editor_name"
        private const val KEY_DESCRIPTION = "editor_description"
        private const val KEY_ICON = "editor_icon"
        private const val KEY_COLOR = "editor_color"
    }
}
