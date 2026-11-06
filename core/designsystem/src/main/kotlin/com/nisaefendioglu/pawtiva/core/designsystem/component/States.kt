package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.R
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

/** Friendly empty state with a gently floating icon and an optional call to action. */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = PawtivaIcons.Paw,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = PawtivaTheme.colors
    val offset = if (PawtivaTheme.reduceMotion) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "emptyFloat")
        val value by transition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
            label = "emptyFloatOffset",
        )
        value
    }
    StateLayout(
        modifier = modifier,
        icon = icon,
        iconBackground = colors.primaryContainer,
        iconTint = colors.onPrimaryContainer,
        iconOffsetDp = offset,
        title = title,
        message = message,
    ) {
        if (actionLabel != null && onAction != null) {
            PawtivaButton(text = actionLabel, onClick = onAction, leadingIcon = PawtivaIcons.Add)
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.pawtiva_loading),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.xl)
            .semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = PawtivaTheme.colors.primary, trackColor = PawtivaTheme.colors.progressTrack)
        Spacer(Modifier.height(Spacing.md))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = PawtivaTheme.colors.textSecondary)
    }
}

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    val colors = PawtivaTheme.colors
    StateLayout(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Assertive },
        icon = PawtivaIcons.Close,
        iconBackground = colors.errorContainer,
        iconTint = colors.onErrorContainer,
        iconOffsetDp = 0f,
        title = stringResource(R.string.pawtiva_error_title),
        message = message,
    ) {
        if (onRetry != null) {
            PawtivaButton(
                text = stringResource(R.string.pawtiva_retry),
                onClick = onRetry,
                style = PawtivaButtonStyle.Secondary,
            )
        }
    }
}

@Composable
private fun StateLayout(
    modifier: Modifier,
    icon: ImageVector,
    iconBackground: Color,
    iconTint: Color,
    iconOffsetDp: Float,
    title: String,
    message: String,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = iconOffsetDp * density }
                .size(96.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = PawtivaTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = PawtivaTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.lg))
        action()
    }
}
