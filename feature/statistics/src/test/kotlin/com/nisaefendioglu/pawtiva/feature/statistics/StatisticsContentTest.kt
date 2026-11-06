package com.nisaefendioglu.pawtiva.feature.statistics

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.domain.StatisticsCalculator
import com.nisaefendioglu.pawtiva.core.model.AchievementId
import com.nisaefendioglu.pawtiva.core.model.AchievementStatus
import com.nisaefendioglu.pawtiva.core.model.CompletionRecord
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneOffset

@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class StatisticsContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val today = LocalDate.of(2026, 11, 5)
    private val lockedAchievements = AchievementId.entries.map { AchievementStatus(it, null, 0f) }

    @Test
    fun emptyData_showsExplanationNotCharts() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                StatisticsContent(
                    StatisticsUiState(
                        loading = false,
                        summary = StatisticsCalculator.summarize(emptyList(), 0, today),
                        achievements = lockedAchievements,
                        today = today,
                    ),
                )
            }
        }
        composeRule.onNodeWithText("No statistics yet", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("Last 7 days").assertDoesNotExist()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Achievements · 0/7"))
        composeRule.onNodeWithText("Achievements · 0/7").assertExists()
    }

    @Test
    fun withData_showsWeekAndHistory() {
        val records = listOf(
            CompletionRecord(1, "Drink water", "💧", today, today.atTime(9, 0).toInstant(ZoneOffset.UTC)),
        )
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                StatisticsContent(
                    StatisticsUiState(
                        loading = false,
                        summary = StatisticsCalculator.summarize(records, 1, today),
                        achievements = lockedAchievements,
                        today = today,
                    ),
                )
            }
        }
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Last 7 days"))
        composeRule.onNodeWithText("Last 7 days").assertExists()
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Drink water"))
        composeRule.onNodeWithText("Drink water", useUnmergedTree = true).assertExists()
    }

    @Test
    @Config(qualifiers = "tr")
    fun turkishAchievementTitles() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                StatisticsContent(
                    StatisticsUiState(
                        loading = false,
                        summary = StatisticsCalculator.summarize(emptyList(), 0, today),
                        achievements = lockedAchievements,
                        today = today,
                    ),
                )
            }
        }
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("İlk Adım", substring = true))
        composeRule.onNodeWithText("İlk Adım", useUnmergedTree = true).assertExists()
    }
}
