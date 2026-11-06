package com.nisaefendioglu.pawtiva.core.designsystem.pet

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import com.nisaefendioglu.pawtiva.core.model.AccessorySlot
import com.nisaefendioglu.pawtiva.core.model.PetMood
import com.nisaefendioglu.pawtiva.core.model.PetType

/**
 * Original, code-drawn mascot (flat 2D vector illustration, not a 3D model).
 * Drawn on a 100 x 100 grid scaled to the composable's size, with a transparent background.
 * Accessories are drawn on top in slot order (see [drawAccessory]); callers pass only the
 * items that fit the pet, normally `PetAppearance.visibleAccessories`.
 */
@Composable
fun PetIllustration(
    type: PetType,
    mood: PetMood,
    contentDescription: String,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    accessories: List<AccessoryId> = emptyList(),
) {
    val breathing = if (animate && !PawtivaTheme.reduceMotion) {
        val transition = rememberInfiniteTransition(label = "petIdle")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.025f,
            animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
            label = "petBreathing",
        )
        value
    } else {
        1f
    }
    Canvas(modifier = modifier.semantics { this.contentDescription = contentDescription }) {
        val u = size.minDimension / 100f
        val palette = if (type == PetType.CAT) CatPalette else DogPalette
        drawOval(Color.Black.copy(alpha = 0.08f), topLeft = Offset(22 * u, 88 * u), size = Size(56 * u, 9 * u))
        scale(scaleX = 1f, scaleY = breathing, pivot = Offset(50 * u, 95 * u)) {
            when (type) {
                PetType.CAT -> drawCat(u, palette, mood)
                PetType.DOG -> drawDog(u, palette, mood)
            }
            accessories.sortedBy { AccessoryDrawOrder.indexOf(it.slot) }.forEach { drawAccessory(it, u) }
        }
    }
}

private data class PetPalette(val fur: Color, val furDark: Color, val accent: Color, val muzzle: Color)

/** Back to front: scarf under headphones, headphones under hats, glasses and flower on top. */
private val AccessoryDrawOrder = listOf(
    AccessorySlot.NECK,
    AccessorySlot.EARS,
    AccessorySlot.HEAD,
    AccessorySlot.EYES,
    AccessorySlot.SPECIAL,
)

internal val Outline = Color(0xFF2B2438)
private val Blush = Color(0xFFF9B4CB)
private val Tongue = Color(0xFFF08AA6)
private val CatPalette = PetPalette(Color(0xFFF6C9A2), Color(0xFFE2A273), Color(0xFFF9B4CB), Color(0xFFFFF1E4))
private val DogPalette = PetPalette(Color(0xFFF4E4CF), Color(0xFFB98A62), Color(0xFF2B2438), Color(0xFFFFF8EE))

internal fun DrawScope.outline(u: Float) = Stroke(width = 1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)

internal fun DrawScope.filledOval(color: Color, cx: Float, cy: Float, w: Float, h: Float, u: Float, stroke: Boolean = true) {
    val topLeft = Offset((cx - w / 2) * u, (cy - h / 2) * u)
    val ovalSize = Size(w * u, h * u)
    drawOval(color, topLeft, ovalSize)
    if (stroke) drawOval(Outline, topLeft, ovalSize, style = outline(u))
}

internal fun DrawScope.polygon(color: Color, u: Float, vararg points: Pair<Float, Float>, stroke: Boolean = true) {
    val path = Path().apply {
        moveTo(points[0].first * u, points[0].second * u)
        points.drop(1).forEach { lineTo(it.first * u, it.second * u) }
        close()
    }
    drawPath(path, color)
    if (stroke) drawPath(path, Outline, style = outline(u))
}

private fun DrawScope.drawCat(u: Float, p: PetPalette, mood: PetMood) {
    // Tail behind the body
    val tail = Path().apply {
        moveTo(72 * u, 82 * u)
        cubicTo(92 * u, 80 * u, 94 * u, 58 * u, 82 * u, 54 * u)
    }
    drawPath(tail, Outline, style = Stroke(9.6f * u, cap = StrokeCap.Round))
    drawPath(tail, p.fur, style = Stroke(6.4f * u, cap = StrokeCap.Round))
    filledOval(p.fur, 50f, 74f, 52f, 34f, u)
    // Ears
    polygon(p.fur, u, 23f to 34f, 28f to 9f, 45f to 21f)
    polygon(p.fur, u, 77f to 34f, 72f to 9f, 55f to 21f)
    polygon(p.accent, u, 28f to 28f, 30f to 15f, 39f to 22f, stroke = false)
    polygon(p.accent, u, 72f to 28f, 70f to 15f, 61f to 22f, stroke = false)
    filledOval(p.fur, 50f, 46f, 60f, 54f, u)
    // Forehead stripes
    listOf(44f, 50f, 56f).forEach { x ->
        drawLine(p.furDark, Offset(x * u, 21 * u), Offset(x * u, 28 * u), strokeWidth = 2.4f * u, cap = StrokeCap.Round)
    }
    filledOval(p.muzzle, 50f, 57f, 22f, 13f, u, stroke = false)
    drawFace(u, mood, cat = true)
    // Whiskers
    val whisker = Outline.copy(alpha = 0.45f)
    drawLine(whisker, Offset(30 * u, 56 * u), Offset(17 * u, 54 * u), 1.2f * u, StrokeCap.Round)
    drawLine(whisker, Offset(30 * u, 59 * u), Offset(17 * u, 61 * u), 1.2f * u, StrokeCap.Round)
    drawLine(whisker, Offset(70 * u, 56 * u), Offset(83 * u, 54 * u), 1.2f * u, StrokeCap.Round)
    drawLine(whisker, Offset(70 * u, 59 * u), Offset(83 * u, 61 * u), 1.2f * u, StrokeCap.Round)
    drawPaws(u, p.fur)
}

private fun DrawScope.drawDog(u: Float, p: PetPalette, mood: PetMood) {
    val tail = Path().apply {
        moveTo(73 * u, 76 * u)
        quadraticTo(86 * u, 72 * u, 88 * u, 60 * u)
    }
    drawPath(tail, Outline, style = Stroke(8.6f * u, cap = StrokeCap.Round))
    drawPath(tail, p.furDark, style = Stroke(5.4f * u, cap = StrokeCap.Round))
    filledOval(p.fur, 50f, 74f, 52f, 34f, u)
    filledOval(p.fur, 50f, 46f, 60f, 54f, u)
    // Eye patch
    filledOval(p.furDark.copy(alpha = 0.55f), 61f, 44f, 14f, 13f, u, stroke = false)
    // Floppy ears
    rotate(degrees = 18f, pivot = Offset(22 * u, 40 * u)) { filledOval(p.furDark, 22f, 44f, 15f, 30f, u) }
    rotate(degrees = -18f, pivot = Offset(78 * u, 40 * u)) { filledOval(p.furDark, 78f, 44f, 15f, 30f, u) }
    filledOval(p.muzzle, 50f, 58f, 24f, 15f, u, stroke = false)
    drawFace(u, mood, cat = false)
    drawPaws(u, p.fur)
}

private fun DrawScope.drawPaws(u: Float, fur: Color) {
    filledOval(fur, 38f, 89f, 13f, 7f, u)
    filledOval(fur, 62f, 89f, 13f, 7f, u)
}

private fun DrawScope.drawFace(u: Float, mood: PetMood, cat: Boolean) {
    // Blush
    drawCircle(Blush.copy(alpha = 0.55f), radius = 4.2f * u, center = Offset(31 * u, 55 * u))
    drawCircle(Blush.copy(alpha = 0.55f), radius = 4.2f * u, center = Offset(69 * u, 55 * u))

    val eyeY = 46f
    listOf(39f, 61f).forEach { x ->
        when (mood) {
            PetMood.NORMAL -> {
                drawCircle(Outline, radius = 3.6f * u, center = Offset(x * u, eyeY * u))
                drawCircle(Color.White, radius = 1.2f * u, center = Offset((x - 1.1f) * u, (eyeY - 1.2f) * u))
            }
            PetMood.EXCITED -> {
                drawCircle(Outline, radius = 5f * u, center = Offset(x * u, eyeY * u))
                drawCircle(Color.White, radius = 1.8f * u, center = Offset((x - 1.6f) * u, (eyeY - 1.8f) * u))
                drawCircle(Color.White, radius = 0.9f * u, center = Offset((x + 1.8f) * u, (eyeY + 1.6f) * u))
            }
            PetMood.HAPPY, PetMood.CELEBRATING -> {
                val arc = Path().apply {
                    moveTo((x - 4) * u, (eyeY + 1.5f) * u)
                    quadraticTo(x * u, (eyeY - 4.5f) * u, (x + 4) * u, (eyeY + 1.5f) * u)
                }
                drawPath(arc, Outline, style = Stroke(2.4f * u, cap = StrokeCap.Round))
            }
            PetMood.SLEEPY -> {
                val arc = Path().apply {
                    moveTo((x - 4) * u, eyeY * u)
                    quadraticTo(x * u, (eyeY + 3.5f) * u, (x + 4) * u, eyeY * u)
                }
                drawPath(arc, Outline, style = Stroke(2.2f * u, cap = StrokeCap.Round))
            }
        }
    }

    // Nose
    if (cat) {
        polygon(Blush, u, 47f to 52.5f, 53f to 52.5f, 50f to 56f, stroke = false)
    } else {
        filledOval(Outline, 50f, 53.5f, 8f, 5.5f, u, stroke = false)
        drawCircle(Color.White.copy(alpha = 0.6f), radius = 0.9f * u, center = Offset(48.5f * u, 52.5f * u))
    }

    // Mouth
    val openMouth = mood == PetMood.EXCITED || mood == PetMood.CELEBRATING
    if (openMouth) {
        filledOval(Outline, 50f, 61f, 9f, 7f, u, stroke = false)
        filledOval(Tongue, 50f, 63f, 5.5f, 3.2f, u, stroke = false)
    } else {
        val mouth = Path().apply {
            moveTo(50 * u, 56.5f * u)
            quadraticTo(47.5f * u, 60.5f * u, 44.5f * u, 58.5f * u)
            moveTo(50 * u, 56.5f * u)
            quadraticTo(52.5f * u, 60.5f * u, 55.5f * u, 58.5f * u)
        }
        drawPath(mouth, Outline, style = Stroke(1.6f * u, cap = StrokeCap.Round))
    }

    if (mood == PetMood.SLEEPY) {
        // Small "z z" above the head
        listOf(Triple(78f, 18f, 5f), Triple(86f, 9f, 3.5f)).forEach { (x, y, s) ->
            val z = Path().apply {
                moveTo(x * u, y * u)
                lineTo((x + s) * u, y * u)
                lineTo(x * u, (y + s) * u)
                lineTo((x + s) * u, (y + s) * u)
            }
            drawPath(z, Outline.copy(alpha = 0.6f), style = Stroke(1.4f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
    if (mood == PetMood.CELEBRATING) {
        // Sparkles
        listOf(Offset(14f, 22f), Offset(86f, 30f), Offset(20f, 70f)).forEach { c ->
            drawLine(Color(0xFFF5C24C), Offset((c.x - 3) * u, c.y * u), Offset((c.x + 3) * u, c.y * u), 1.6f * u, StrokeCap.Round)
            drawLine(Color(0xFFF5C24C), Offset(c.x * u, (c.y - 3) * u), Offset(c.x * u, (c.y + 3) * u), 1.6f * u, StrokeCap.Round)
        }
    }
}
