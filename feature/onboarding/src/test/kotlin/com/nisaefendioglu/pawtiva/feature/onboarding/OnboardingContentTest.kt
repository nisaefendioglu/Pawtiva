package com.nisaefendioglu.pawtiva.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.PetType
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class OnboardingContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun petStep_continueEnabledOnlyAfterChoosing() {
        var state by mutableStateOf(OnboardingUiState(step = OnboardingStep.PET))
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                OnboardingContent(
                    state = state,
                    actions = OnboardingActions(onSelectPet = { state = state.copy(petType = it) }),
                )
            }
        }

        composeRule.onNode(hasText("Continue") and hasClickAction()).assertIsNotEnabled()
        composeRule.onNode(hasText("Puppy") and isSelectable(), useUnmergedTree = false).performClick()

        assertEquals(PetType.DOG, state.petType)
        composeRule.onNode(hasText("Puppy") and isSelectable()).assertIsSelected()
        composeRule.onNode(hasText("Continue") and hasClickAction()).assertIsEnabled()
    }

    @Test
    @Config(qualifiers = "tr")
    fun welcomeStep_showsTurkishText() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                OnboardingContent(state = OnboardingUiState(), actions = OnboardingActions())
            }
        }
        composeRule.onNodeWithText("Pawtiva'ya hoş geldin").assertExists()
        composeRule.onNodeWithText("Başlayalım").assertExists()
    }
}
