package com.nisaefendioglu.pawtiva.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.nisaefendioglu.pawtiva.core.model.DateFormatPreference
import com.nisaefendioglu.pawtiva.core.model.HabitColor
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Formats a calendar date following the user's date-format preference and app locale. */
fun formatDate(date: LocalDate, preference: DateFormatPreference, locale: Locale): String {
    val formatter = when (preference) {
        DateFormatPreference.SYSTEM -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        DateFormatPreference.DAY_MONTH_YEAR -> DateTimeFormatter.ofPattern("dd.MM.yyyy", locale)
        DateFormatPreference.MONTH_DAY_YEAR -> DateTimeFormatter.ofPattern("MM/dd/yyyy", locale)
        DateFormatPreference.ISO -> DateTimeFormatter.ISO_LOCAL_DATE
    }
    return date.format(formatter)
}

/** Localized integer, e.g. 1.250 in Turkish and 1,250 in English. */
fun formatNumber(value: Int, locale: Locale): String = NumberFormat.getIntegerInstance(locale).format(value)

/** The app's current locale (follows the in-app language choice). */
val currentLocale: Locale
    @Composable @ReadOnlyComposable get() = LocalConfiguration.current.locales[0]

/** Card tint for a habit; pastel in light theme, deep tone in dark theme. Text uses textPrimary. */
@Composable
@ReadOnlyComposable
fun habitContainerColor(color: HabitColor): Color {
    val dark = PawtivaTheme.colors.isDark
    return when (color) {
        HabitColor.LAVENDER -> if (dark) Color(0xFF3B2F66) else Color(0xFFE9E1FF)
        HabitColor.MINT -> if (dark) Color(0xFF1F4A38) else Color(0xFFDDF6EA)
        HabitColor.PEACH -> if (dark) Color(0xFF5A3322) else Color(0xFFFFE6D9)
        HabitColor.PINK -> if (dark) Color(0xFF5A2A3D) else Color(0xFFFFE3EC)
        HabitColor.SKY -> if (dark) Color(0xFF1F3F5C) else Color(0xFFE0F1FF)
        HabitColor.SUN -> if (dark) Color(0xFF574416) else Color(0xFFFFF0C7)
    }
}
