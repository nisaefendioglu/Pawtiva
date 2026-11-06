package com.nisaefendioglu.pawtiva.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Raw brand palette. Screens never use these directly; they read [PawtivaColors]
 * through [PawtivaTheme.colors] so light, dark and dynamic themes stay consistent.
 * Pastel tones are used for surfaces and accents only, never for body text.
 */
internal object PawtivaPalette {
    // Pastel lavender
    val Lavender100 = Color(0xFFE9E1FF)
    val Lavender200 = Color(0xFFD6C8FF)
    val Lavender300 = Color(0xFFC9B8FF)
    val Lavender600 = Color(0xFF6E4FC7)
    val Lavender800 = Color(0xFF4A3399)
    val Lavender900 = Color(0xFF2E1D6B)
    val Lavender950 = Color(0xFF2A1A5E)

    // Warm cream
    val Cream50 = Color(0xFFFFFBF5)
    val Cream100 = Color(0xFFFFF4E6)
    val Cream200 = Color(0xFFF2E6D6)

    // Mint
    val Mint100 = Color(0xFFDDF6EA)
    val Mint300 = Color(0xFF8FE0BB)
    val Mint700 = Color(0xFF1E7A52)
    val Mint800 = Color(0xFF1A5A3F)
    val Mint900 = Color(0xFF125238)

    // Peach
    val Peach100 = Color(0xFFFFE6D9)
    val Peach300 = Color(0xFFFFB892)
    val Peach700 = Color(0xFFA4481E)
    val Peach800 = Color(0xFF7A3A1A)
    val Peach900 = Color(0xFF6B2E10)
    val Peach950 = Color(0xFF4A1F08)

    // Light pink and light sky blue (accents)
    val Pink100 = Color(0xFFFFE3EC)
    val Pink300 = Color(0xFFF9B4CB)
    val Pink600 = Color(0xFFC2547F)
    val Sky100 = Color(0xFFE0F1FF)
    val Sky300 = Color(0xFFA9D6FA)

    // Warning (amber) and error (coral)
    val Amber100 = Color(0xFFFFF0C7)
    val Amber400 = Color(0xFFF5C24C)
    val Amber800 = Color(0xFF6B4A00)
    val Amber900 = Color(0xFF5C3D00)
    val Coral100 = Color(0xFFFFE0DC)
    val Coral200 = Color(0xFFFFDAD6)
    val Coral300 = Color(0xFFFFB4AB)
    val Coral700 = Color(0xFFB3261E)
    val Coral800 = Color(0xFF8C1D18)
    val Coral900 = Color(0xFF7A1712)

    // Neutrals
    val Ink900 = Color(0xFF2B2438)
    val Ink600 = Color(0xFF5C5470)
    val Ink400 = Color(0xFF9A93A8)
    val Ink200 = Color(0xFFE6E0EE)
    val White = Color(0xFFFFFFFF)

    val Night950 = Color(0xFF17131F)
    val Night900 = Color(0xFF211B2C)
    val Night800 = Color(0xFF2C2539)
    val Night700 = Color(0xFF3A3248)
    val Night500 = Color(0xFF6E6680)
    val NightText = Color(0xFFF2EEF8)
    val NightTextSecondary = Color(0xFFC7BFD6)
}

/** Semantic color tokens. Every on-X / X pair meets WCAG AA (see PawtivaColorsContrastTest). */
@Immutable
data class PawtivaColors(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val border: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val disabled: Color,
    val onDisabled: Color,
    val progressTrack: Color,
    val progressStart: Color,
    val progressEnd: Color,
    val coin: Color,
    val accentPink: Color,
    val accentSky: Color,
    val shadow: Color,
    val isDark: Boolean,
)

internal val LightPawtivaColors = PawtivaColors(
    background = PawtivaPalette.Cream50,
    surface = PawtivaPalette.White,
    surfaceMuted = PawtivaPalette.Cream100,
    border = PawtivaPalette.Cream200,
    primary = PawtivaPalette.Lavender600,
    onPrimary = PawtivaPalette.White,
    primaryContainer = PawtivaPalette.Lavender100,
    onPrimaryContainer = PawtivaPalette.Lavender900,
    secondary = PawtivaPalette.Peach700,
    onSecondary = PawtivaPalette.White,
    secondaryContainer = PawtivaPalette.Peach100,
    onSecondaryContainer = PawtivaPalette.Peach900,
    success = PawtivaPalette.Mint700,
    successContainer = PawtivaPalette.Mint100,
    onSuccessContainer = PawtivaPalette.Mint900,
    warning = PawtivaPalette.Amber400,
    warningContainer = PawtivaPalette.Amber100,
    onWarningContainer = PawtivaPalette.Amber900,
    error = PawtivaPalette.Coral700,
    onError = PawtivaPalette.White,
    errorContainer = PawtivaPalette.Coral100,
    onErrorContainer = PawtivaPalette.Coral900,
    textPrimary = PawtivaPalette.Ink900,
    textSecondary = PawtivaPalette.Ink600,
    disabled = PawtivaPalette.Ink200,
    onDisabled = PawtivaPalette.Ink400,
    progressTrack = PawtivaPalette.Lavender100,
    progressStart = PawtivaPalette.Lavender600,
    progressEnd = PawtivaPalette.Pink600,
    coin = PawtivaPalette.Amber400,
    accentPink = PawtivaPalette.Pink100,
    accentSky = PawtivaPalette.Sky100,
    shadow = PawtivaPalette.Lavender600,
    isDark = false,
)

internal val DarkPawtivaColors = PawtivaColors(
    background = PawtivaPalette.Night950,
    surface = PawtivaPalette.Night900,
    surfaceMuted = PawtivaPalette.Night800,
    border = PawtivaPalette.Night700,
    primary = PawtivaPalette.Lavender300,
    onPrimary = PawtivaPalette.Lavender950,
    primaryContainer = PawtivaPalette.Lavender800,
    onPrimaryContainer = PawtivaPalette.Lavender100,
    secondary = PawtivaPalette.Peach300,
    onSecondary = PawtivaPalette.Peach950,
    secondaryContainer = PawtivaPalette.Peach800,
    onSecondaryContainer = PawtivaPalette.Peach100,
    success = PawtivaPalette.Mint300,
    successContainer = PawtivaPalette.Mint800,
    onSuccessContainer = PawtivaPalette.Mint100,
    warning = PawtivaPalette.Amber400,
    warningContainer = PawtivaPalette.Amber800,
    onWarningContainer = PawtivaPalette.Amber100,
    error = PawtivaPalette.Coral300,
    onError = PawtivaPalette.Coral900,
    errorContainer = PawtivaPalette.Coral800,
    onErrorContainer = PawtivaPalette.Coral200,
    textPrimary = PawtivaPalette.NightText,
    textSecondary = PawtivaPalette.NightTextSecondary,
    disabled = PawtivaPalette.Night700,
    onDisabled = PawtivaPalette.Night500,
    progressTrack = PawtivaPalette.Night700,
    progressStart = PawtivaPalette.Lavender300,
    progressEnd = PawtivaPalette.Pink300,
    coin = PawtivaPalette.Amber400,
    accentPink = PawtivaPalette.Night800,
    accentSky = PawtivaPalette.Night800,
    shadow = Color.Black,
    isDark = true,
)

val LocalPawtivaColors = staticCompositionLocalOf { LightPawtivaColors }
