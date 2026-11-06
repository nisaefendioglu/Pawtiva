package com.nisaefendioglu.pawtiva.feature.home

import com.nisaefendioglu.pawtiva.core.data.HabitRepository
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ShopRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.LevelCalculator
import com.nisaefendioglu.pawtiva.core.domain.PawtivaClock
import com.nisaefendioglu.pawtiva.core.domain.RewardPolicy
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.CompletionReward
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeClock(var dateTime: LocalDateTime) : PawtivaClock {
    override fun now(): Instant = dateTime.toInstant(ZoneOffset.UTC)
    override fun zone(): ZoneId = ZoneOffset.UTC
}

class FakeHabitRepository : HabitRepository {
    val habits = MutableStateFlow<List<Habit>>(emptyList())
    val done = MutableStateFlow<Set<Pair<Long, LocalDate>>>(emptySet())

    fun add(id: Long, name: String) {
        habits.update { it + Habit(id, name, "", "🌿", HabitColor.MINT, Instant.EPOCH) }
    }

    override fun observeActiveHabits(): Flow<List<Habit>> = habits.map { list -> list.filter { !it.archived } }
    override fun observeHabit(id: Long): Flow<Habit?> = habits.map { list -> list.firstOrNull { it.id == id } }
    override fun observeDailyHabits(date: LocalDate): Flow<List<DailyHabit>> =
        combine(observeActiveHabits(), done) { list, completed -> list.map { DailyHabit(it, (it.id to date) in completed) } }

    override suspend fun createHabit(name: String, description: String, icon: String, color: HabitColor): Long =
        error("not used")
    override suspend fun updateHabit(id: Long, name: String, description: String, icon: String, color: HabitColor) =
        error("not used")
    override suspend fun archiveHabit(id: Long) = error("not used")
}

/** Rewards like the real repository; [gate] lets a test hold a save "in flight". */
class FakeProgressRepository(private val habits: FakeHabitRepository) : ProgressRepository {
    val totalXp = MutableStateFlow(0)
    var completeCalls = 0
    var gate: CompletableDeferred<Unit>? = null

    override fun observeProgress(): Flow<Progress> = totalXp.map { xp ->
        Progress(
            totalXp = xp,
            level = LevelCalculator.level(xp),
            xpIntoLevel = LevelCalculator.xpIntoLevel(xp),
            balance = xp / 2,
            totalCoinsEarned = xp / 2,
        )
    }

    override suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome {
        completeCalls++
        gate?.await()
        if ((habitId to date) in habits.done.value) return CompleteOutcome.AlreadyCompleted
        val active = habits.habits.value.count { !it.archived }
        val before = habits.done.value.count { it.second == date }
        val levelBefore = LevelCalculator.level(totalXp.value)
        habits.done.update { it + (habitId to date) }
        totalXp.update { it + RewardPolicy.XP_PER_COMPLETION }
        return CompleteOutcome.Completed(
            CompletionReward(
                xpGained = RewardPolicy.XP_PER_COMPLETION,
                coinsGained = RewardPolicy.COINS_PER_COMPLETION,
                levelBefore = levelBefore,
                levelAfter = LevelCalculator.level(totalXp.value),
                completedTheDay = before + 1 == active,
                unlockedAchievements = emptyList(),
            ),
        )
    }

    override suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome {
        if ((habitId to date) !in habits.done.value) return UndoOutcome.NotCompleted
        habits.done.update { it - (habitId to date) }
        totalXp.update { it - RewardPolicy.XP_PER_COMPLETION }
        return UndoOutcome.Undone(RewardPolicy.XP_PER_COMPLETION, RewardPolicy.COINS_PER_COMPLETION, LevelCalculator.level(totalXp.value))
    }

    override fun observeCompletionHistory(): Flow<List<CompletionRecord>> = flowOf(emptyList())
}

class FakeShopRepository : ShopRepository {
    val equipped = MutableStateFlow<Set<AccessoryId>>(emptySet())
    override fun observeShop(petType: PetType): Flow<List<ShopItemState>> = flowOf(emptyList())
    override fun observeEquipped(): Flow<Set<AccessoryId>> = equipped
    override suspend fun purchase(id: AccessoryId): PurchaseOutcome = PurchaseOutcome.UnknownItem
    override suspend fun equip(id: AccessoryId): Boolean = false
    override suspend fun unequip(id: AccessoryId) = Unit
}

class FakeUserPreferencesRepository(initial: UserPreferences = UserPreferences(petType = PetType.DOG, onboardingCompleted = true)) :
    UserPreferencesRepository {
    val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state
    override suspend fun setPetType(type: PetType) = state.update { it.copy(petType = type) }
    override suspend fun setPetName(name: String?) = state.update { it.copy(petName = name) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDynamicColor(enabled: Boolean) = state.update { it.copy(dynamicColor = enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) = state.update { it.copy(onboardingCompleted = completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(remindersEnabled = enabled, reminderTime = time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) = state.update { it.copy(reducePetAnimations = reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) = state.update { it.copy(dateFormat = format) }
    override suspend fun clear() {
        state.value = UserPreferences()
    }
}
