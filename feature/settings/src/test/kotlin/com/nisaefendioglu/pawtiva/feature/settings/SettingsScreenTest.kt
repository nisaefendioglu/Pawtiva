package com.nisaefendioglu.pawtiva.feature.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.PetType
import com.nisaefendioglu.pawtiva.core.model.UserPreferences
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var resets = 0

    private fun launch() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                SettingsScreen(
                    state = SettingsUiState(
                        preferences = UserPreferences(petType = PetType.DOG, onboardingCompleted = true),
                        isLoading = false,
                    ),
                    actions = SettingsActions(onReset = { resets++ }),
                    onBack = {},
                    versionName = "1.0.0",
                    dynamicColorSupported = false,
                )
            }
        }
    }

    @Test
    fun reset_requiresTwoConfirmations() {
        launch()
        composeRule.onNodeWithText("Reset all data").performScrollTo().performClick()
        composeRule.onNodeWithText("Reset all data?").assertExists()

        composeRule.onNodeWithText("Continue").performClick()
        composeRule.onNodeWithText("Are you sure?").assertExists()
        assertEquals(0, resets)

        composeRule.onNodeWithText("Delete everything").performClick()
        assertEquals(1, resets)
        composeRule.onNodeWithText("Are you sure?").assertDoesNotExist()
    }

    @Test
    fun reset_canBeCancelled() {
        launch()
        composeRule.onNodeWithText("Reset all data").performScrollTo().performClick()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Reset all data?").assertDoesNotExist()
        assertEquals(0, resets)
    }

    @Test
    fun aboutSection_showsVersionAndDeveloper() {
        launch()
        composeRule.onNodeWithContentDescription("Version, 1.0.0").performScrollTo().assertExists()
        composeRule.onNodeWithContentDescription("Developer, Nisa Efendioğlu").assertExists()
    }
}
