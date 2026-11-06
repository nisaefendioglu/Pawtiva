package com.nisaefendioglu.pawtiva.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.domain.EditableDays
import com.nisaefendioglu.pawtiva.core.model.DailyHabit
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import com.nisaefendioglu.pawtiva.core.model.PetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant
import java.time.LocalDate

/** A tall screen so every list item is composed without scrolling. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35], qualifiers = "w411dp-h1800dp")
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 5)

    private fun state(habits: List<DailyHabit>) = HomeUiState(
        isLoading = false,
        today = today,
        selectedDate = today,
        days = EditableDays.window(today),
        hourOfDay = 10,
        habits = habits,
        petType = PetType.CAT,
    )

    @Test
    fun emptyState_addButtonOpensEditor() {
        var addClicks = 0
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                HomeScreen(
                    state = state(emptyList()),
                    snackbarHostState = SnackbarHostState(),
                    onSelectDate = {},
                    onToggleHabit = {},
                    onOpenSettings = {},
                    onAddHabit = { addClicks++ },
                    onEditHabit = {},
                )
            }
        }
        composeRule.onNodeWithText("Your journey starts with a small step.").assertExists()
        composeRule.onNode(hasText("Add habit") and hasClickAction()).performClick()
        assertEquals(1, addClicks)
    }

    @Test
    fun habitCard_isACheckbox_andTogglesTheRightHabit() {
        val toggled = mutableListOf<Long>()
        val water = Habit(7, "Drink water", "", "💧", HabitColor.SKY, Instant.EPOCH)
        val walk = Habit(8, "Walk", "", "🚶", HabitColor.MINT, Instant.EPOCH)
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                HomeScreen(
                    state = state(listOf(DailyHabit(water, completed = false), DailyHabit(walk, completed = true))),
                    snackbarHostState = SnackbarHostState(),
                    onSelectDate = {},
                    onToggleHabit = { toggled += it },
                    onOpenSettings = {},
                    onAddHabit = {},
                    onEditHabit = {},
                )
            }
        }
        composeRule.onNode(hasText("Drink water") and isToggleable()).assertIsOff()
        composeRule.onNode(hasText("Walk") and isToggleable()).assertIsOn()

        composeRule.onNode(hasText("Drink water") and isToggleable()).performClick()
        assertEquals(listOf(7L), toggled)
        assertTrue(composeRule.onNodeWithText("Today's habits").fetchSemanticsNode() != null)
    }
}
