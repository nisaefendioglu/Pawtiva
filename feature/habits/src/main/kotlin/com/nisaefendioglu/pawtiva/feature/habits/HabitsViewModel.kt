package com.nisaefendioglu.pawtiva.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.model.Habit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HabitsUiState {
    data object Loading : HabitsUiState
    data object Error : HabitsUiState
    data class Loaded(val habits: List<Habit>) : HabitsUiState
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
) : ViewModel() {

    val uiState: StateFlow<HabitsUiState> = habitRepository.observeActiveHabits()
        .map<List<Habit>, HabitsUiState> { HabitsUiState.Loaded(it) }
        .catch { emit(HabitsUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitsUiState.Loading)

    /** Archives the habit; its history, XP and coins stay. */
    fun onDelete(habitId: Long) {
        viewModelScope.launch { habitRepository.archiveHabit(habitId) }
    }
}
