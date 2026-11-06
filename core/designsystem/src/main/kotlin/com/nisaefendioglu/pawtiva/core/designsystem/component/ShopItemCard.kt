package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

/** Visual emphasis of the small status line under an item's description. */
enum class ShopItemStatusTone { Neutral, Positive, Warning }

/**
 * One shop item. Every text comes from the caller (already localized). Tapping the card
 * previews the item on the pet; the buttons buy, wear or take it off.
 */
@Composable
fun ShopItemCard(
    thumbnail: @Composable () -> Unit,
    name: String,
    description: String,
    priceText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    statusText: String? = null,
    statusTone: ShopItemStatusTone = ShopItemStatusTone.Neutral,
    previewing: Boolean = false,
    primaryActionLabel: String? = null,
    primaryActionEnabled: Boolean = true,
    onPrimaryAction: () -> Unit = {},
    primaryActionStyle: PawtivaButtonStyle = PawtivaButtonStyle.Primary,
) {
    val colors = PawtivaTheme.colors
    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            selected = previewing
            role = Role.Button
        },
        shape = MaterialTheme.shapes.large,
        color = if (previewing) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (previewing) 2.dp else 1.dp, if (previewing) colors.primary else colors.border),
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.surfaceMuted),
                    contentAlignment = Alignment.Center,
                ) { thumbnail() }
                Spacer(Modifier.width(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (badgeText != null) {
                            Spacer(Modifier.width(Spacing.xs))
                            Badge(badgeText)
                        }
                    }
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Spacer(Modifier.height(Spacing.xxs))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(PawtivaIcons.Coin, contentDescription = null, tint = colors.secondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(Spacing.xxs))
                        Text(text = priceText, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }
            }
            if (statusText != null) {
                Spacer(Modifier.height(Spacing.xs))
                StatusLine(statusText, statusTone)
            }
            if (primaryActionLabel != null) {
                Spacer(Modifier.height(Spacing.sm))
                PawtivaButton(
                    text = primaryActionLabel,
                    onClick = onPrimaryAction,
                    enabled = primaryActionEnabled,
                    style = primaryActionStyle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun Badge(text: String) {
    val colors = PawtivaTheme.colors
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(colors.successContainer)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = colors.onSuccessContainer)
    }
}

@Composable
private fun StatusLine(text: String, tone: ShopItemStatusTone) {
    val colors = PawtivaTheme.colors
    val (background: Color, content: Color) = when (tone) {
        ShopItemStatusTone.Neutral -> colors.surfaceMuted to colors.textSecondary
        ShopItemStatusTone.Positive -> colors.successContainer to colors.onSuccessContainer
        ShopItemStatusTone.Warning -> colors.warningContainer to colors.onWarningContainer
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(background)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = content)
    }
}
