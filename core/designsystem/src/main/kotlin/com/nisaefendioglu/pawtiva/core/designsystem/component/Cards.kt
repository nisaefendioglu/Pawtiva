package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

/** Rounded card with a hairline border and a soft tinted shadow (shadow is dropped in dark theme). */
@Composable
fun PawtivaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = PawtivaTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(Spacing.md),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = PawtivaTheme.colors
    val shape = MaterialTheme.shapes.large
    val shadowed = modifier.shadow(
        elevation = if (colors.isDark) 0.dp else 10.dp,
        shape = shape,
        ambientColor = colors.shadow.copy(alpha = 0.08f),
        spotColor = colors.shadow.copy(alpha = 0.12f),
    )
    val border = BorderStroke(1.dp, colors.border)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = shadowed, shape = shape, color = containerColor, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Surface(modifier = shadowed, shape = shape, color = containerColor, border = border) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

/** Single statistic: an icon bubble, a value and its label. Read as one phrase by screen readers. */
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: Color = PawtivaTheme.colors.primaryContainer,
    onAccent: Color = PawtivaTheme.colors.onPrimaryContainer,
) {
    PawtivaCard(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$value, $label" },
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = onAccent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = PawtivaTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = PawtivaTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Section title with an optional trailing text action. */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = PawtivaTheme.colors.textPrimary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (actionLabel != null && onAction != null) {
            PawtivaButton(text = actionLabel, onClick = onAction, style = PawtivaButtonStyle.Text)
        }
    }
}
