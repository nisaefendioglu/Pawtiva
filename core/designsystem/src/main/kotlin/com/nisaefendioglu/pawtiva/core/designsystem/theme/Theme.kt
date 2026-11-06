package com.nisaefendioglu.pawtiva.core.designsystem.theme

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.nisaefendioglu.pawtiva.core.model.ThemeMode

/** True when animations should be skipped (system animator scale 0 or the user's in-app choice). */
val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * Pawtiva theme. Material 3 components receive a color scheme derived from Pawtiva's
 * semantic tokens, so stock components already look on-brand.
 *
 * With [dynamicColor] on Android 12+, only primary/secondary come from the wallpaper;
 * success, warning and error stay fixed so their meaning never changes.
 */
@Composable
fun PawtivaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val base = if (darkTheme) DarkPawtivaColors else LightPawtivaColors
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        base.copy(
            primary = dynamic.primary,
            onPrimary = dynamic.onPrimary,
            primaryContainer = dynamic.primaryContainer,
            onPrimaryContainer = dynamic.onPrimaryContainer,
            secondary = dynamic.secondary,
            onSecondary = dynamic.onSecondary,
            secondaryContainer = dynamic.secondaryContainer,
            onSecondaryContainer = dynamic.onSecondaryContainer,
        )
    } else {
        base
    }

    val systemAnimationsOff = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }

    CompositionLocalProvider(
        LocalPawtivaColors provides colors,
        LocalReduceMotion provides (reduceMotion || systemAnimationsOff),
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = PawtivaTypography,
            shapes = PawtivaShapes,
            content = content,
        )
    }
}

/** Accessors for Pawtiva-specific tokens, mirroring MaterialTheme. */
object PawtivaTheme {
    val colors: PawtivaColors
        @Composable @ReadOnlyComposable get() = LocalPawtivaColors.current

    val reduceMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReduceMotion.current
}

private fun PawtivaColors.toMaterialScheme(): ColorScheme {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = success,
        tertiaryContainer = successContainer,
        onTertiaryContainer = onSuccessContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        background = background,
        onBackground = textPrimary,
        surface = surface,
        onSurface = textPrimary,
        surfaceVariant = surfaceMuted,
        onSurfaceVariant = textSecondary,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surfaceMuted,
        surfaceContainerHigh = surfaceMuted,
        surfaceContainerHighest = border,
        outline = border,
        outlineVariant = border,
    )
}
