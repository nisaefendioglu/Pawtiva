package com.nisaefendioglu.pawtiva.feature.shop

import com.nisaefendioglu.pawtiva.core.domain.ShopCatalog
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.PurchaseOutcome
import com.nisaefendioglu.pawtiva.core.model.ShopCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val progress = FakeProgressRepository(Progress(totalXp = 150, level = 2, xpIntoLevel = 50, balance = 100))
    private val shop = FakeShopRepository(progress)
    private val preferences = FakeUserPreferencesRepository()

    private class Harness(val vm: ShopViewModel, val events: MutableList<ShopEvent>)

    /** Keeps uiState subscribed and records snackbar events for the length of the test. */
    private fun TestScope.harness(): Harness {
        val vm = ShopViewModel(shop, progress, preferences)
        val events = mutableListOf<ShopEvent>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(dispatcher) { vm.uiState.collect {} }
        backgroundScope.launch(dispatcher) { vm.eventFlow.collect { events += it } }
        return Harness(vm, events)
    }

    @Test
    fun loadsCatalogWithBalanceAndPet() = runTest {
        val h = harness()
        val state = h.vm.uiState.value
        assertFalse(state.loading)
        assertEquals(PetType.CAT, state.petType)
        assertEquals(100, state.progress.balance)
        assertEquals(ShopCatalog.items.size, state.items.size)
    }

    @Test
    fun buyConfirm_purchasesAndCelebrates() = runTest {
        val h = harness()
        h.vm.onBuyClicked(AccessoryId.BASIC_CAP)
        assertEquals(AccessoryId.BASIC_CAP, h.vm.uiState.value.pendingPurchase?.item?.accessory)

        h.vm.onConfirmPurchase()

        assertEquals(listOf(AccessoryId.BASIC_CAP), shop.purchaseCalls)
        assertNull(h.vm.uiState.value.pendingPurchase)
        assertEquals(1, h.vm.uiState.value.celebrationKey)
        assertEquals(60, h.vm.uiState.value.progress.balance)
        val event = h.events.single() as ShopEvent.Purchased
        assertEquals(AccessoryId.BASIC_CAP, event.id)
        assertEquals(60, event.balanceAfter)
        assertTrue(h.vm.uiState.value.items.first { it.item.accessory == AccessoryId.BASIC_CAP }.owned)
    }

    @Test
    fun dismissingTheDialog_doesNotPurchase() = runTest {
        val h = harness()
        h.vm.onBuyClicked(AccessoryId.BASIC_CAP)
        h.vm.onDismissPurchase()
        assertNull(h.vm.uiState.value.pendingPurchase)
        assertTrue(shop.purchaseCalls.isEmpty())
    }

    @Test
    fun insufficientBalance_isExplainedWithoutOpeningTheDialog() = runTest {
        progress.state.value = Progress(balance = 30, level = 2)
        val h = harness()
        h.vm.onBuyClicked(AccessoryId.WIRELESS_HEADPHONES) // costs 90

        assertNull(h.vm.uiState.value.pendingPurchase)
        assertEquals(ShopEvent.InsufficientBalance(60), h.events.single())
        assertTrue(shop.purchaseCalls.isEmpty())
    }

    @Test
    fun levelLockedItem_isExplained() = runTest {
        progress.state.value = Progress(balance = 500, level = 2)
        val h = harness()
        h.vm.onBuyClicked(AccessoryId.ROYAL_CROWN)
        assertEquals(ShopEvent.LevelTooLow(5), h.events.single())
    }

    @Test
    fun everyRepositoryOutcome_isMappedToAMessage() = runTest {
        val h = harness()
        val cases = listOf(
            PurchaseOutcome.AlreadyOwned to ShopEvent.AlreadyOwned,
            PurchaseOutcome.InsufficientBalance(12) to ShopEvent.InsufficientBalance(12),
            PurchaseOutcome.LevelTooLow(5) to ShopEvent.LevelTooLow(5),
            PurchaseOutcome.UnknownItem to ShopEvent.UnknownItem,
        )
        cases.forEach { (outcome, expected) ->
            shop.forcedOutcome = outcome
            h.vm.onBuyClicked(AccessoryId.BASIC_CAP)
            h.vm.onConfirmPurchase()
            assertEquals(expected, h.events.last())
        }
        assertEquals(0, h.vm.uiState.value.celebrationKey)
    }

    @Test
    fun preview_replacesTheSameSlotOnly_andTogglesOff() = runTest {
        shop.owned.value = setOf(AccessoryId.BASIC_CAP, AccessoryId.WINTER_SCARF)
        shop.equipped.value = setOf(AccessoryId.BASIC_CAP, AccessoryId.WINTER_SCARF)
        val h = harness()

        h.vm.onPreview(AccessoryId.ROYAL_CROWN)
        assertEquals(
            setOf(AccessoryId.ROYAL_CROWN, AccessoryId.WINTER_SCARF),
            h.vm.uiState.value.previewAccessories,
        )

        h.vm.onPreview(AccessoryId.ROYAL_CROWN)
        assertNull(h.vm.uiState.value.previewing)
        assertEquals(setOf(AccessoryId.BASIC_CAP, AccessoryId.WINTER_SCARF), h.vm.uiState.value.previewAccessories)
    }

    @Test
    fun equip_andUnequip_updateEquippedItems() = runTest {
        shop.owned.value = setOf(AccessoryId.BASIC_CAP, AccessoryId.ROYAL_CROWN)
        val h = harness()

        h.vm.onEquip(AccessoryId.BASIC_CAP)
        h.vm.onEquip(AccessoryId.ROYAL_CROWN)
        assertEquals(setOf(AccessoryId.ROYAL_CROWN), h.vm.uiState.value.equipped)

        h.vm.onUnequip(AccessoryId.ROYAL_CROWN)
        assertTrue(h.vm.uiState.value.equipped.isEmpty())
    }

    @Test
    fun equippingAnItemNotOwned_isExplained() = runTest {
        val h = harness()
        h.vm.onEquip(AccessoryId.FLOWER_CLIP)
        assertEquals(ShopEvent.NotOwned, h.events.single())
    }

    @Test
    fun ownedFilter_showsOnlyOwnedItems() = runTest {
        shop.owned.value = setOf(AccessoryId.FLOWER_CLIP)
        val h = harness()
        h.vm.onFilterSelected(ShopFilter.Owned)
        assertEquals(listOf(AccessoryId.FLOWER_CLIP), h.vm.uiState.value.visibleItems.map { it.item.accessory })

        h.vm.onFilterSelected(ShopFilter.Category(ShopCategory.HATS))
        assertTrue(h.vm.uiState.value.visibleItems.all { it.item.category == ShopCategory.HATS })
    }

    @Test
    fun switchingPet_keepsOwnershipAndEquippedItems() = runTest {
        shop.owned.value = setOf(AccessoryId.WINTER_SCARF)
        shop.equipped.value = setOf(AccessoryId.WINTER_SCARF)
        val h = harness()
        preferences.state.value = preferences.state.value.copy(petType = PetType.DOG)

        val state = h.vm.uiState.value
        assertEquals(PetType.DOG, state.petType)
        assertEquals(setOf(AccessoryId.WINTER_SCARF), state.equipped)
        assertTrue(state.items.first { it.item.accessory == AccessoryId.WINTER_SCARF }.owned)
    }
}
