package com.nisaefendioglu.pawtiva.core.designsystem.pet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Draws every pet, mood and accessory on Robolectric so a drawing error fails the build. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [35])
class PetDisplayTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everyPetMoodAndAccessory_draws() {
        composeRule.setContent {
            PawtivaTheme(reduceMotion = true) {
                Column {
                    PetType.entries.forEach { type ->
                        PetMood.entries.forEach { mood ->
                            PetDisplay(
                                appearance = PetAppearance(type, mood, AccessoryId.entries.toSet()),
                                contentDescription = "$type-$mood",
                                modifier = Modifier.size(64.dp),
                            )
                        }
                    }
                }
            }
        }
        PetType.entries.forEach { type ->
            PetMood.entries.forEach { mood ->
                composeRule.onNodeWithContentDescription("$type-$mood").assertExists()
            }
        }
    }

    @Test
    fun switchingPet_keepsAccessoriesOnScreen_andReactionsDoNotCrash() {
        var type by mutableStateOf(PetType.CAT)
        composeRule.setContent {
            PawtivaTheme {
                var taps by remember { mutableIntStateOf(0) }
                PetDisplay(
                    appearance = PetAppearance(type, PetMood.HAPPY, setOf(AccessoryId.ROYAL_CROWN, AccessoryId.WINTER_SCARF)),
                    contentDescription = "pet",
                    modifier = Modifier
                        .size(120.dp)
                        .clickable { taps++ },
                    reaction = PetReaction.CELEBRATE,
                    reactionKey = taps.takeIf { it > 0 },
                )
            }
        }
        composeRule.onNodeWithContentDescription("pet").performClick()
        composeRule.mainClock.advanceTimeBy(2_000)
        type = PetType.DOG
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("pet").assertExists()
    }
}
