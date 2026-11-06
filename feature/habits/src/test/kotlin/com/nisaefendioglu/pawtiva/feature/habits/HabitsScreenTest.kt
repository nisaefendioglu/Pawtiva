package com.nisaefendioglu.pawtiva.feature.habits

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.Habit
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class HabitsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyList_showsCallToAction() {
        var adds = 0
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                HabitsScreen(HabitsUiState.Loaded(emptyList()), onAddHabit = { adds++ }, onEditHabit = {}, onDelete = {})
            }
        }
        composeRule.onNode(hasText("Add habit") and hasClickAction()).performClick()
        assertEquals(1, adds)
    }

    @Test
    fun delete_asksForConfirmationFirst() {
        val deleted = mutableListOf<Long>()
        val habit = Habit(3, "Read", "", "📚", HabitColor.LAVENDER, Instant.EPOCH)
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                HabitsScreen(HabitsUiState.Loaded(listOf(habit)), onAddHabit = {}, onEditHabit = {}, onDelete = { deleted += it })
            }
        }
        composeRule.onNodeWithContentDescription("Delete Read").performClick()
        assertEquals(emptyList<Long>(), deleted)
        composeRule.onNodeWithText("Delete “Read”?").assertExists()

        composeRule.onNode(hasText("Delete") and hasClickAction() and hasAnyAncestor(isDialog())).performClick()
        assertEquals(listOf(3L), deleted)
        composeRule.onAllNodesWithText("Read").onFirst().assertExists()
    }

    @Test
    fun editor_saveDisabledUntilNameEntered() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                HabitEditorScreen(
                    state = HabitEditorUiState(),
                    onClose = {},
                    onNameChange = {},
                    onDescriptionChange = {},
                    onIconSelected = {},
                    onColorSelected = {},
                    onSave = {},
                    onDelete = {},
                )
            }
        }
        composeRule.onNode(hasText("Save") and hasClickAction()).assertIsNotEnabled()
    }
}
