package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing

enum class PawtivaButtonStyle { Primary, Secondary, Text }

/** Pill-shaped button with a soft press-scale; the scale is skipped when motion is reduced. */
@Composable
fun PawtivaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PawtivaButtonStyle = PawtivaButtonStyle.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = PawtivaTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduceMotion = PawtivaTheme.reduceMotion
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduceMotion) 0.96f else 1f,
        label = "pawtivaButtonScale",
    )
    val (container, content) = when (style) {
        PawtivaButtonStyle.Primary -> colors.primary to colors.onPrimary
        PawtivaButtonStyle.Secondary -> colors.primaryContainer to colors.onPrimaryContainer
        PawtivaButtonStyle.Text -> Color.Transparent to colors.primary
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (style == PawtivaButtonStyle.Text) Color.Transparent else colors.disabled,
            disabledContentColor = colors.onDisabled,
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = 14.dp),
        interactionSource = interaction,
    ) {
        if (leadingIcon != null) {
            Icon(imageVector = leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(Spacing.xs))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}
