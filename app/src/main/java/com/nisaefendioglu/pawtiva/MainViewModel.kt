package com.nisaefendioglu.pawtiva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(val preferences: UserPreferences) : MainUiState
}

/** Decides between onboarding and the main shell, and feeds theme preferences to the root. */
@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = preferences.preferences
        .map<UserPreferences, MainUiState> { MainUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState.Loading)
}
