package com.nisaefendioglu.pawtiva.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nisaefendioglu.pawtiva.core.designsystem.R
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.designsystem.theme.Spacing
import java.text.NumberFormat

/** Animated XP bar with a lavender-to-pink fill. [progress] is clamped to 0..1. */
@Composable
fun XPProgressBar(
    progress: Float,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
) {
    val colors = PawtivaTheme.colors
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = if (PawtivaTheme.reduceMotion) snap() else tween(600, easing = FastOutSlowInEasing),
        label = "xpProgress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(colors.progressTrack)
            .semantics {
                this.contentDescription = contentDescription
                progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f)
            },
    ) {
        if (animated > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animated)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(colors.progressStart, colors.progressEnd))),
            )
        }
    }
}

/** Circular ring showing [fraction] of today's habits. */
@Composable
fun ProgressRing(
    fraction: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 10.dp,
    centerText: String? = null,
) {
    val colors = PawtivaTheme.colors
    val animated by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = if (PawtivaTheme.reduceMotion) snap() else tween(700, easing = FastOutSlowInEasing),
        label = "progressRing",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = colors.progressTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke),
            )
            if (animated > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(colors.progressStart, colors.progressEnd, colors.progressStart)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        if (centerText != null) {
            Text(text = centerText, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
        }
    }
}

/** Today's completion summary: ring, localized percentage and a pluralized sentence. */
@Composable
fun DailyProgressCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    val fraction = if (total > 0) completed.toFloat() / total else 0f
    val percentText = remember(fraction, locale) { NumberFormat.getPercentInstance(locale).format(fraction) }
    val summary = if (total == 0) {
        stringResource(R.string.pawtiva_daily_progress_empty)
    } else {
        pluralStringResource(R.plurals.pawtiva_daily_progress_summary, total, completed, total)
    }
    PawtivaCard(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            ProgressRing(fraction = fraction, centerText = percentText)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.pawtiva_daily_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = PawtivaTheme.colors.textPrimary,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PawtivaTheme.colors.textSecondary,
                )
            }
        }
    }
}
