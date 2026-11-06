package com.nisaefendioglu.pawtiva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.navigation.PawtivaBottomBar
import com.nisaefendioglu.pawtiva.navigation.TopLevelDestination
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Bottom navigation on the JVM with Robolectric (feature screens need Hilt, so they are tested in their modules). */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class PawtivaAppNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun launch() {
        var selected by mutableStateOf(TopLevelDestination.HOME)
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                PawtivaBottomBar(selected = selected, onSelect = { selected = it })
            }
        }
    }

    @Test
    fun startsOnHome_andSwitchesTabs_english() {
        launch()
        composeRule.onNode(hasText("Home") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("Shop") and isSelectable()).performClick()
        composeRule.onNode(hasText("Shop") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("Home") and isSelectable()).assertIsNotSelected()
        composeRule.onNode(hasText("Stats") and isSelectable()).performClick()
        composeRule.onNode(hasText("Stats") and isSelectable()).assertIsSelected()
    }

    @Test
    @Config(qualifiers = "tr")
    fun showsTurkishLabels_whenLocaleIsTurkish() {
        launch()
        composeRule.onNode(hasText("Ana sayfa") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("Alışkanlıklar") and isSelectable()).performClick()
        composeRule.onNode(hasText("Alışkanlıklar") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("İstatistik") and isSelectable()).assertExists()
    }
}
