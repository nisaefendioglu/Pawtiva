package com.nisaefendioglu.pawtiva.feature.shop

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.domain.ShopCatalog
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.Progress
import com.nisaefendioglu.pawtiva.core.model.ShopItemState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class ShopContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun itemStates(balance: Int, level: Int, owned: Set<AccessoryId> = emptySet()) = ShopCatalog.items.map {
        ShopItemState(
            item = it,
            owned = it.accessory in owned,
            equipped = false,
            fitsCurrentPet = true,
            affordable = balance >= it.price,
            levelUnlocked = level >= it.requiredLevel,
        )
    }

    @Test
    fun confirmDialog_showsPriceAndBalanceAfter_andConfirms() {
        val items = itemStates(balance = 100, level = 2)
        var confirmed = 0
        var dismissed = 0
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                ShopContent(
                    state = ShopUiState(
                        loading = false,
                        petType = PetType.CAT,
                        progress = Progress(balance = 100, level = 2),
                        // No list behind the dialog, so "Buy" only matches the dialog button.
                        items = emptyList(),
                        pendingPurchase = items.first { it.item.accessory == AccessoryId.BASIC_CAP },
                    ),
                    actions = ShopActions(onConfirmPurchase = { confirmed++ }, onDismissPurchase = { dismissed++ }),
                )
            }
        }

        composeRule.onNodeWithText("Buy Basic Cap?").assertExists()
        composeRule.onNodeWithText("Price: 40 PawCoins").assertExists()
        composeRule.onNodeWithText("Balance after: 60 PawCoins").assertExists()

        composeRule.onNode(hasText("Cancel") and hasClickAction()).performClick()
        composeRule.onNode(hasText("Buy") and hasClickAction(), useUnmergedTree = false).performClick()
        assertEquals(1, dismissed)
        assertEquals(1, confirmed)
    }

    @Test
    fun lockedAndUnaffordableItems_explainWhyAndCannotBeBought() {
        val items = itemStates(balance = 30, level = 1)
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                ShopContent(
                    state = ShopUiState(loading = false, progress = Progress(balance = 30), items = items),
                    actions = ShopActions(),
                )
            }
        }
        // Items sit below the header and preview, so scroll each into composition first.
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("You need 10 more PawCoins."))
        composeRule.onNodeWithText("You need 10 more PawCoins.").assertExists()
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Unlocks at level 5."))
        composeRule.onNodeWithText("Unlocks at level 5.", useUnmergedTree = true).assertExists()
    }

    @Test
    @Config(qualifiers = "tr")
    fun turkishCopy_isShown() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                ShopContent(
                    state = ShopUiState(loading = false, items = itemStates(balance = 0, level = 1)),
                    actions = ShopActions(),
                )
            }
        }
        composeRule.onNodeWithText("Market").assertExists()
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Klasik Şapka"))
        composeRule.onNodeWithText("Klasik Şapka", useUnmergedTree = true).assertExists()
    }

    @Test
    fun buyButton_isDisabledWhenNotAffordable() {
        val items = itemStates(balance = 0, level = 1).filter { it.item.accessory == AccessoryId.BASIC_CAP }
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                ShopContent(
                    state = ShopUiState(loading = false, items = items),
                    actions = ShopActions(),
                )
            }
        }
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Basic Cap"))
        composeRule.onNode(hasText("Buy") and hasClickAction()).assertIsNotEnabled()
    }
}
