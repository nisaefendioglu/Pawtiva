package com.nisaefendioglu.pawtiva.feature.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nisaefendioglu.pawtiva.core.data.ProgressRepository
import com.nisaefendioglu.pawtiva.core.data.ShopRepository
import com.nisaefendioglu.pawtiva.core.data.UserPreferencesRepository
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopCategory
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which items the list shows. */
sealed interface ShopFilter {
    fun matches(state: ShopItemState): Boolean

    data object All : ShopFilter {
        override fun matches(state: ShopItemState) = true
    }

    data object Owned : ShopFilter {
        override fun matches(state: ShopItemState) = state.owned
    }

    data class Category(val category: ShopCategory) : ShopFilter {
        override fun matches(state: ShopItemState) = state.item.category == category
    }
}

/** One-off messages for the snackbar. The UI turns them into localized text. */
sealed interface ShopEvent {
    data class Purchased(val id: AccessoryId, val balanceAfter: Int, val achievements: List<AchievementId>) : ShopEvent
    data object AlreadyOwned : ShopEvent
    data class InsufficientBalance(val missing: Int) : ShopEvent
    data class LevelTooLow(val requiredLevel: Int) : ShopEvent
    data object UnknownItem : ShopEvent
    data object NotOwned : ShopEvent
    data class DoesNotFitPet(val id: AccessoryId) : ShopEvent
}

data class ShopUiState(
    val loading: Boolean = true,
    val petType: PetType = PetType.CAT,
    val petName: String? = null,
    val progress: Progress = Progress(),
    val items: List<ShopItemState> = emptyList(),
    val equipped: Set<AccessoryId> = emptySet(),
    val filter: ShopFilter = ShopFilter.All,
    val previewing: AccessoryId? = null,
    val pendingPurchase: ShopItemState? = null,
    val purchasing: Boolean = false,
    /** Changes whenever the pet should celebrate (successful purchase). */
    val celebrationKey: Int = 0,
) {
    val visibleItems: List<ShopItemState> get() = items.filter(filter::matches)

    /** Equipped items, with the previewed item replacing whatever sits in its slot. */
    val previewAccessories: Set<AccessoryId>
        get() {
            val preview = previewing ?: return equipped
            return equipped.filterTo(mutableSetOf()) { it.slot != preview.slot } + preview
        }

    val ownedCount: Int get() = items.count { it.owned }
}

private data class LocalState(
    val filter: ShopFilter = ShopFilter.All,
    val previewing: AccessoryId? = null,
    val pendingPurchase: AccessoryId? = null,
    val purchasing: Boolean = false,
    val celebrationKey: Int = 0,
)

private data class ShopData(
    val petType: PetType,
    val petName: String?,
    val progress: Progress,
    val items: List<ShopItemState>,
    val equipped: Set<AccessoryId>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shop: ShopRepository,
    progressRepository: ProgressRepository,
    preferences: UserPreferencesRepository,
) : ViewModel() {

    private val local = MutableStateFlow(LocalState())
    private val events = Channel<ShopEvent>(Channel.BUFFERED)

    /** Snackbar messages; each is delivered once. */
    val eventFlow: Flow<ShopEvent> = events.receiveAsFlow()

    private val data: Flow<ShopData> = preferences.preferences.flatMapLatest { prefs ->
        val pet = prefs.petType ?: PetType.CAT
        combine(
            shop.observeShop(pet),
            shop.observeEquipped(),
            progressRepository.observeProgress(),
        ) { items, equipped, progress ->
            ShopData(pet, prefs.petName, progress, items, equipped)
        }
    }

    val uiState: StateFlow<ShopUiState> = combine(data, local) { d, l ->
        ShopUiState(
            loading = false,
            petType = d.petType,
            petName = d.petName,
            progress = d.progress,
            items = d.items,
            equipped = d.equipped,
            filter = l.filter,
            previewing = l.previewing,
            pendingPurchase = l.pendingPurchase?.let { id -> d.items.firstOrNull { it.item.accessory == id } },
            purchasing = l.purchasing,
            celebrationKey = l.celebrationKey,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShopUiState())

    fun onFilterSelected(filter: ShopFilter) = local.update { it.copy(filter = filter) }

    /** Tapping the previewed item again clears the preview. */
    fun onPreview(id: AccessoryId) = local.update {
        it.copy(previewing = if (it.previewing == id) null else id)
    }

    fun onClearPreview() = local.update { it.copy(previewing = null) }

    fun onBuyClicked(id: AccessoryId) {
        val state = uiState.value.items.firstOrNull { it.item.accessory == id }
        when {
            state == null -> sendEvent(ShopEvent.UnknownItem)
            state.owned -> sendEvent(ShopEvent.AlreadyOwned)
            !state.levelUnlocked -> sendEvent(ShopEvent.LevelTooLow(state.item.requiredLevel))
            !state.affordable -> sendEvent(
                ShopEvent.InsufficientBalance((state.item.price - uiState.value.progress.balance).coerceAtLeast(1)),
            )
            else -> local.update { it.copy(pendingPurchase = id, previewing = id) }
        }
    }

    fun onDismissPurchase() = local.update { it.copy(pendingPurchase = null) }

    fun onConfirmPurchase() {
        val id = local.value.pendingPurchase ?: return
        if (local.value.purchasing) return
        local.update { it.copy(purchasing = true) }
        viewModelScope.launch {
            val outcome = try {
                shop.purchase(id)
            } finally {
                local.update { it.copy(purchasing = false, pendingPurchase = null) }
            }
            when (outcome) {
                is PurchaseOutcome.Purchased -> {
                    local.update { it.copy(previewing = null, celebrationKey = it.celebrationKey + 1) }
                    sendEvent(ShopEvent.Purchased(outcome.item.accessory, outcome.balanceAfter, outcome.unlockedAchievements))
                }
                PurchaseOutcome.AlreadyOwned -> sendEvent(ShopEvent.AlreadyOwned)
                is PurchaseOutcome.InsufficientBalance -> sendEvent(ShopEvent.InsufficientBalance(outcome.missing))
                is PurchaseOutcome.LevelTooLow -> sendEvent(ShopEvent.LevelTooLow(outcome.requiredLevel))
                PurchaseOutcome.UnknownItem -> sendEvent(ShopEvent.UnknownItem)
            }
        }
    }

    fun onEquip(id: AccessoryId) {
        val state = uiState.value.items.firstOrNull { it.item.accessory == id }
        if (state != null && !state.fitsCurrentPet) {
            sendEvent(ShopEvent.DoesNotFitPet(id))
            return
        }
        viewModelScope.launch {
            if (!shop.equip(id)) sendEvent(ShopEvent.NotOwned)
            local.update { if (it.previewing == id) it.copy(previewing = null) else it }
        }
    }

    fun onUnequip(id: AccessoryId) {
        viewModelScope.launch { shop.unequip(id) }
    }

    private fun sendEvent(event: ShopEvent) {
        events.trySend(event)
    }
}
