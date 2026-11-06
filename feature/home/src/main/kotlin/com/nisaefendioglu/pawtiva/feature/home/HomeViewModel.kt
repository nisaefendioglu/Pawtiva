package com.nisaefendioglu.pawtiva.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ShopRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.EditableDays
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.domain.PetMoodCalculator
import com.nisaefendioglu.pawtiva.core.domain.RewardPolicy
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** One-shot UI events. Sent through a Channel so they are never replayed after rotation. */
sealed interface HomeEvent {
    data class Rewarded(val xp: Int, val coins: Int) : HomeEvent
    data class LeveledUp(val level: Int) : HomeEvent
    /** Every habit of [date] is done; [isToday] decides the wording and whether the pet celebrates. */
    data class DayCompleted(val date: LocalDate, val isToday: Boolean) : HomeEvent
    data object Undone : HomeEvent
    data object UpdateFailed : HomeEvent
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val today: LocalDate = LocalDate.EPOCH,
    val selectedDate: LocalDate = LocalDate.EPOCH,
    val days: List<LocalDate> = emptyList(),
    val hourOfDay: Int = 12,
    val habits: List<DailyHabit> = emptyList(),
    val progress: Progress = Progress(),
    val petType: PetType = PetType.CAT,
    val petName: String? = null,
    val mood: PetMood = PetMood.NORMAL,
    val equipped: Set<AccessoryId> = emptySet(),
    val dateFormat: DateFormatPreference = DateFormatPreference.SYSTEM,
    val reducePetAnimations: Boolean = false,
    /** Habits whose toggle is being saved; their cards are disabled until it finishes. */
    val inFlight: Set<Long> = emptySet(),
) {
    val completedCount: Int get() = habits.count { it.completed }
    val totalCount: Int get() = habits.size
    val isToday: Boolean get() = selectedDate == today
    val xpPerCompletion: Int get() = RewardPolicy.XP_PER_COMPLETION
}

/** Local hour bucket; re-emitted when the hour or the calendar day changes. */
private data class Tick(val today: LocalDate, val hour: Int)

private data class DayKey(val today: LocalDate, val selected: LocalDate, val hour: Int)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val habitRepository: HabitRepository,
    private val progressRepository: ProgressRepository,
    private val shopRepository: ShopRepository,
    private val clock: PawtivaClock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /**
     * null = follow "today", so the screen rolls over at midnight on its own. Kept in
     * SavedStateHandle so a chosen past day survives process death.
     */
    private val selectedDate: Flow<LocalDate?> = savedStateHandle.getStateFlow<String?>(KEY_SELECTED_DATE, null)
        .map { stored -> stored?.let { runCatching { LocalDate.parse(it) }.getOrNull() } }
    private val inFlight = MutableStateFlow<Set<Pair<Long, LocalDate>>>(emptySet())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    private val ticks: Flow<Tick> = flow {
        while (true) {
            val now = clock.now().atZone(clock.zone())
            emit(Tick(now.toLocalDate(), now.hour))
            delay(TICK_MILLIS)
        }
    }.distinctUntilChanged()

    val uiState: StateFlow<HomeUiState> = combine(ticks, selectedDate) { tick, selected ->
        val date = selected?.takeIf { EditableDays.isEditable(it, tick.today) } ?: tick.today
        DayKey(tick.today, date, tick.hour)
    }
        .distinctUntilChanged()
        .flatMapLatest { key ->
            val dayHabits = habitRepository.observeDailyHabits(key.selected)
            val todayHabits = if (key.selected == key.today) dayHabits else habitRepository.observeDailyHabits(key.today)
            combine(
                dayHabits,
                todayHabits,
                progressRepository.observeProgress(),
                preferences.preferences,
                shopRepository.observeEquipped(),
            ) { day, todayList, progress, prefs, equipped ->
                buildState(key, day, todayList, progress, prefs, equipped)
            }
        }
        .combine(inFlight) { state, flying ->
            state.copy(inFlight = flying.filter { it.second == state.selectedDate }.map { it.first }.toSet())
        }
        .catch { emit(HomeUiState(isLoading = false, loadFailed = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onSelectDate(date: LocalDate) {
        // Read the clock directly: uiState.today is a placeholder while the screen is loading.
        val today = clock.today()
        savedStateHandle[KEY_SELECTED_DATE] =
            if (date == today || !EditableDays.isEditable(date, today)) null else date.toString()
    }

    fun onToggleHabit(habitId: Long) {
        val state = uiState.value
        if (state.isLoading) return
        val date = state.selectedDate
        val habit = state.habits.firstOrNull { it.habit.id == habitId } ?: return
        val key = habitId to date
        // Double-tap guard: a second tap while the first is still saving is ignored.
        if (key in inFlight.value) return
        inFlight.update { it + key }
        viewModelScope.launch {
            try {
                if (habit.completed) undo(habitId, date) else complete(habitId, date)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (error: Exception) {
                _events.send(HomeEvent.UpdateFailed)
            } finally {
                inFlight.update { it - key }
            }
        }
    }

    private suspend fun complete(habitId: Long, date: LocalDate) {
        when (val outcome = progressRepository.complete(habitId, date)) {
            is CompleteOutcome.Completed -> {
                val reward = outcome.reward
                _events.send(HomeEvent.Rewarded(reward.xpGained, reward.coinsGained))
                if (reward.completedTheDay) _events.send(HomeEvent.DayCompleted(date, isToday = date == clock.today()))
                if (reward.leveledUp) _events.send(HomeEvent.LeveledUp(reward.levelAfter))
            }
            CompleteOutcome.AlreadyCompleted -> Unit
            CompleteOutcome.DateNotEditable, CompleteOutcome.HabitNotFound -> _events.send(HomeEvent.UpdateFailed)
        }
    }

    private suspend fun undo(habitId: Long, date: LocalDate) {
        when (progressRepository.undo(habitId, date)) {
            is UndoOutcome.Undone -> _events.send(HomeEvent.Undone)
            UndoOutcome.NotCompleted -> Unit
            UndoOutcome.DateNotEditable -> _events.send(HomeEvent.UpdateFailed)
        }
    }

    private fun buildState(
        key: DayKey,
        day: List<DailyHabit>,
        todayList: List<DailyHabit>,
        progress: Progress,
        prefs: UserPreferences,
        equipped: Set<AccessoryId>,
    ): HomeUiState {
        // The pet always reflects TODAY, even while the user looks at a past day.
        val mood = PetMoodCalculator.restingMood(
            completedToday = todayList.count { it.completed },
            activeHabitsToday = todayList.size,
            hourOfDay = key.hour,
        )
        return HomeUiState(
            isLoading = false,
            today = key.today,
            selectedDate = key.selected,
            days = EditableDays.window(key.today),
            hourOfDay = key.hour,
            habits = day,
            progress = progress,
            petType = prefs.petType ?: PetType.CAT,
            petName = prefs.petName,
            mood = mood,
            equipped = equipped,
            dateFormat = prefs.dateFormat,
            reducePetAnimations = prefs.reducePetAnimations,
        )
    }

    private companion object {
        const val TICK_MILLIS = 60_000L
        const val KEY_SELECTED_DATE = "home_selected_date"
    }
}

/** Exposed for tests: the greeting bucket for a local hour. */
internal enum class Greeting { MORNING, AFTERNOON, EVENING, NIGHT }

internal fun greetingFor(hour: Int): Greeting = when (hour) {
    in 5..11 -> Greeting.MORNING
    in 12..17 -> Greeting.AFTERNOON
    in 18..21 -> Greeting.EVENING
    else -> Greeting.NIGHT
}
