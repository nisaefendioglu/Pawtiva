package com.nisaefendioglu.pawtiva.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

// Android Studio previews only: the numbers below are sample inputs for checking
// the look of each component in light and dark theme, not app data.
@Preview(name = "Light", showBackground = true, widthDp = 380)
@Preview(name = "Dark", showBackground = true, widthDp = 380, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DesignSystemPreview() {
    PawtivaTheme {
        Column(
            modifier = Modifier
                .background(PawtivaTheme.colors.background)
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SectionHeader(title = "Section header", actionLabel = "See all", onAction = {})
            DailyProgressCard(completed = 3, total = 5)
            XPProgressBar(progress = 0.65f, contentDescription = "65 / 100 XP")
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                StatCard(label = "Total XP", value = "1,250", icon = PawtivaIcons.Stats, modifier = Modifier.weight(1f))
                StatCard(
                    label = "PawCoins",
                    value = "320",
                    icon = PawtivaIcons.Coin,
                    modifier = Modifier.weight(1f),
                    accent = PawtivaTheme.colors.secondaryContainer,
                    onAccent = PawtivaTheme.colors.onSecondaryContainer,
                )
            }
            PawtivaButton(text = "Primary", onClick = {}, leadingIcon = PawtivaIcons.Add)
            PawtivaButton(text = "Secondary", onClick = {}, style = PawtivaButtonStyle.Secondary)
            PawtivaButton(text = "Disabled", onClick = {}, enabled = false)
        }
    }
}

@Preview(name = "States", showBackground = true, widthDp = 380)
@Composable
private fun StatesPreview() {
    PawtivaTheme {
        Column(Modifier.background(PawtivaTheme.colors.background)) {
            EmptyState(
                title = "Your journey starts with a small step",
                message = "Create your first habit.",
                actionLabel = "Add habit",
                onAction = {},
            )
            ErrorState(message = "Couldn't load your habits.", onRetry = {})
            LoadingState()
        }
    }
}
