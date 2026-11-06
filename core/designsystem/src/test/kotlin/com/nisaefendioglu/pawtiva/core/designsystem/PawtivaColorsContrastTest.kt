package com.nisaefendioglu.pawtiva.core.designsystem

import androidx.compose.ui.graphics.Color
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Contrast
import com.nisaefendioglu.pawtiva.core.designsystem.theme.DarkPawtivaColors
import com.nisaefendioglu.pawtiva.core.designsystem.theme.LightPawtivaColors
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PawtivaColorsContrastTest {

    private fun textPairs(c: PawtivaColors): Map<String, Pair<Color, Color>> = mapOf(
        "textPrimary/background" to (c.textPrimary to c.background),
        "textPrimary/surface" to (c.textPrimary to c.surface),
        "textPrimary/surfaceMuted" to (c.textPrimary to c.surfaceMuted),
        "textSecondary/background" to (c.textSecondary to c.background),
        "textSecondary/surface" to (c.textSecondary to c.surface),
        "onPrimary/primary" to (c.onPrimary to c.primary),
        "onPrimaryContainer/primaryContainer" to (c.onPrimaryContainer to c.primaryContainer),
        "onSecondary/secondary" to (c.onSecondary to c.secondary),
        "onSecondaryContainer/secondaryContainer" to (c.onSecondaryContainer to c.secondaryContainer),
        "onSuccessContainer/successContainer" to (c.onSuccessContainer to c.successContainer),
        "success/surface" to (c.success to c.surface),
        "onWarningContainer/warningContainer" to (c.onWarningContainer to c.warningContainer),
        "error/surface" to (c.error to c.surface),
        "onError/error" to (c.onError to c.error),
        "onErrorContainer/errorContainer" to (c.onErrorContainer to c.errorContainer),
        "primary/background (text buttons)" to (c.primary to c.background),
    )

    private fun assertAa(name: String, colors: PawtivaColors) {
        textPairs(colors).forEach { (pair, value) ->
            val ratio = Contrast.ratio(value.first, value.second)
            assertTrue(
                "$name $pair contrast ${"%.2f".format(ratio)} is below WCAG AA ${Contrast.AA_NORMAL_TEXT}",
                ratio >= Contrast.AA_NORMAL_TEXT,
            )
        }
    }

    @Test
    fun lightTheme_textPairsMeetAa() = assertAa("Light", LightPawtivaColors)

    @Test
    fun darkTheme_textPairsMeetAa() = assertAa("Dark", DarkPawtivaColors)

    @Test
    fun progressFill_isDistinguishableFromTrack() {
        listOf(LightPawtivaColors, DarkPawtivaColors).forEach { c ->
            assertTrue(Contrast.ratio(c.progressStart, c.progressTrack) >= Contrast.AA_LARGE_TEXT)
            assertTrue(Contrast.ratio(c.progressEnd, c.progressTrack) >= Contrast.AA_LARGE_TEXT)
        }
    }

    @Test
    fun contrast_matchesKnownReferenceValues() {
        assertEquals(21.0, Contrast.ratio(Color.Black, Color.White), 0.01)
        assertEquals(1.0, Contrast.ratio(Color.White, Color.White), 0.0001)
    }
}
