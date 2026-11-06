package com.nisaefendioglu.pawtiva.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.AchievementRepository
import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.domain.StatisticsCalculator
import com.nisaefendioglu.pawtiva.core.domain.StatisticsSummary
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.Progress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class StatisticsUiState(
    val loading: Boolean = true,
    val summary: StatisticsSummary? = null,
    val progress: Progress = Progress(),
    val achievements: List<AchievementStatus> = emptyList(),
    val dateFormat: DateFormatPreference = DateFormatPreference.SYSTEM,
    val today: LocalDate? = null,
) {
    /** True when nothing has ever been completed: charts are replaced by an explanation. */
    val isEmpty: Boolean get() = summary?.isEmpty ?: true
    val unlockedCount: Int get() = achievements.count { it.unlocked }
}

/** Everything on this screen is computed from stored data; nothing is estimated or invented. */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    progressRepository: ProgressRepository,
    habitRepository: HabitRepository,
    achievementRepository: AchievementRepository,
    preferences: UserPreferencesRepository,
    private val clock: PawtivaClock,
) : ViewModel() {

    /** Re-emits the date every minute so "today" rolls over at midnight while the screen is open. */
    private val todayTicks = flow {
        while (true) {
            emit(clock.today())
            delay(60_000)
        }
    }.distinctUntilChanged()

    private val data = combine(
        progressRepository.observeCompletionHistory(),
        habitRepository.observeActiveHabits(),
        progressRepository.observeProgress(),
        achievementRepository.observeAchievements(),
        preferences.preferences,
    ) { history, habits, progress, achievements, prefs -> Data(history, habits, progress, achievements, prefs.dateFormat) }

    val uiState: StateFlow<StatisticsUiState> = combine(data, todayTicks) { d, today ->
        StatisticsUiState(
            loading = false,
            summary = StatisticsCalculator.summarize(
                records = d.history,
                activeHabitCount = d.habits.size,
                today = today,
                activeHabitIds = d.habits.map { it.id }.toSet(),
            ),
            progress = d.progress,
            achievements = d.achievements,
            dateFormat = d.dateFormat,
            today = today,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    private data class Data(
        val history: List<CompletionRecord>,
        val habits: List<Habit>,
        val progress: Progress,
        val achievements: List<AchievementStatus>,
        val dateFormat: DateFormatPreference,
    )
}
