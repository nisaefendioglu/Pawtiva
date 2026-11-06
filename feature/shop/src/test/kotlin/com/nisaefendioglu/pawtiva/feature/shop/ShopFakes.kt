package com.nisaefendioglu.pawtiva.feature.shop

import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ShopRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.domain.ShopCatalog
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.CompleteOutcome
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import com.nisaefendioglu.pawtiva.core.model.ThemeMode
import com.nisaefendioglu.pawtiva.core.model.UndoOutcome
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.LocalTime

/** In-memory shop that follows the same rules as the real one (catalog, balance, level, one item per slot). */
class FakeShopRepository(private val progress: FakeProgressRepository) : ShopRepository {
    val owned = MutableStateFlow<Set<AccessoryId>>(emptySet())
    val equipped = MutableStateFlow<Set<AccessoryId>>(emptySet())
    val purchaseCalls = mutableListOf<AccessoryId>()

    /** When set, [purchase] returns this instead of applying the rules. */
    var forcedOutcome: PurchaseOutcome? = null

    override fun observeShop(petType: PetType): Flow<List<ShopItemState>> =
        combine(owned, equipped, progress.state) { owned, equipped, p ->
            ShopCatalog.items.map { item ->
                ShopItemState(
                    item = item,
                    owned = item.accessory in owned,
                    equipped = item.accessory in equipped,
                    fitsCurrentPet = item.accessory.fits(petType),
                    affordable = p.balance >= item.price,
                    levelUnlocked = p.level >= item.requiredLevel,
                )
            }
        }

    override fun observeEquipped(): Flow<Set<AccessoryId>> = equipped

    override suspend fun purchase(id: AccessoryId): PurchaseOutcome {
        purchaseCalls += id
        forcedOutcome?.let { return it }
        val item = ShopCatalog.find(id) ?: return PurchaseOutcome.UnknownItem
        val p = progress.state.value
        return when {
            id in owned.value -> PurchaseOutcome.AlreadyOwned
            p.level < item.requiredLevel -> PurchaseOutcome.LevelTooLow(item.requiredLevel)
            p.balance < item.price -> PurchaseOutcome.InsufficientBalance(item.price - p.balance)
            else -> {
                progress.state.update { it.copy(balance = it.balance - item.price) }
                owned.update { it + id }
                PurchaseOutcome.Purchased(item, progress.state.value.balance, emptyList())
            }
        }
    }

    override suspend fun equip(id: AccessoryId): Boolean {
        if (id !in owned.value) return false
        equipped.update { current -> current.filterTo(mutableSetOf()) { it.slot != id.slot } + id }
        return true
    }

    override suspend fun unequip(id: AccessoryId) {
        equipped.update { it - id }
    }
}

class FakeProgressRepository(initial: Progress = Progress()) : ProgressRepository {
    val state = MutableStateFlow(initial)
    override fun observeProgress(): Flow<Progress> = state
    override suspend fun complete(habitId: Long, date: LocalDate): CompleteOutcome = CompleteOutcome.HabitNotFound
    override suspend fun undo(habitId: Long, date: LocalDate): UndoOutcome = UndoOutcome.NotCompleted
    override fun observeCompletionHistory(): Flow<List<CompletionRecord>> = flowOf(emptyList())
}

class FakeUserPreferencesRepository(initial: UserPreferences = UserPreferences(petType = PetType.CAT)) :
    UserPreferencesRepository {
    val state = MutableStateFlow(initial)
    override val preferences: Flow<UserPreferences> = state
    override suspend fun setPetType(type: PetType) = state.update { it.copy(petType = type) }
    override suspend fun setPetName(name: String?) = state.update { it.copy(petName = name) }
    override suspend fun setThemeMode(mode: ThemeMode) = state.update { it.copy(themeMode = mode) }
    override suspend fun setDynamicColor(enabled: Boolean) = state.update { it.copy(dynamicColor = enabled) }
    override suspend fun setOnboardingCompleted(completed: Boolean) =
        state.update { it.copy(onboardingCompleted = completed) }
    override suspend fun setReminders(enabled: Boolean, time: LocalTime) =
        state.update { it.copy(remindersEnabled = enabled, reminderTime = time) }
    override suspend fun setReducePetAnimations(reduce: Boolean) =
        state.update { it.copy(reducePetAnimations = reduce) }
    override suspend fun setDateFormat(format: DateFormatPreference) = state.update { it.copy(dateFormat = format) }
    override suspend fun clear() {
        state.value = UserPreferences()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
