package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.icon.PawtivaIcons
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

/**
 * One habit on the daily checklist. The whole card is a checkbox (role Checkbox), so screen
 * readers announce it as one control with [stateDescription]. Completion is shown with a check
 * mark and strike-through text, never by color alone.
 */
@Composable
fun HabitCard(
    icon: String,
    name: String,
    description: String?,
    completed: Boolean,
    rewardLabel: String,
    stateDescription: String,
    containerColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = PawtivaTheme.colors
    val reduceMotion = PawtivaTheme.reduceMotion
    val checkScale by animateFloatAsState(
        targetValue = if (completed) 1f else 0f,
        animationSpec = if (reduceMotion) snap() else spring(dampingRatio = 0.5f),
        label = "habitCheck",
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .clip(MaterialTheme.shapes.large)
            .toggleable(
                value = completed,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .semantics { this.stateDescription = stateDescription },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, colors.border),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, style = MaterialTheme.typography.titleLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    textDecoration = if (completed) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = rewardLabel,
                    style = MaterialTheme.typography.labelMedium,
                    // Earned rewards switch to the success tone; the label text changes too, so
                    // the state never depends on color alone.
                    color = if (completed) colors.onSuccessContainer else colors.onPrimaryContainer,
                    modifier = Modifier
                        .padding(top = Spacing.xxs)
                        .clip(CircleShape)
                        .background(if (completed) colors.successContainer else colors.primaryContainer)
                        .padding(horizontal = Spacing.xs, vertical = 2.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (completed) colors.success else colors.surface)
                    .border(2.dp, if (completed) colors.success else colors.textSecondary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = PawtivaIcons.Check,
                    contentDescription = null,
                    tint = if (colors.isDark) colors.background else Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = checkScale
                            scaleY = checkScale
                            alpha = checkScale
                        },
                )
            }
        }
    }
}
