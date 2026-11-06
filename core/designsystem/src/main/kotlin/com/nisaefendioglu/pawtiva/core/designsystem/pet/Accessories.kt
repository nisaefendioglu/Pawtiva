package com.nisaefendioglu.pawtiva.core.designsystem.pet

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nisaefendioglu.pawtiva.core.model.AccessoryId
import kotlin.math.cos
import kotlin.math.sin

/**
 * Original, generic accessory artwork (no brands or logos), drawn on the same 100 x 100 grid
 * as the pets. Both pets share head and neck geometry, so one drawing fits both.
 */
internal fun DrawScope.drawAccessory(id: AccessoryId, u: Float) {
    when (id) {
        AccessoryId.BASIC_CAP -> drawCap(u)
        AccessoryId.ROYAL_CROWN -> drawCrown(u)
        AccessoryId.ROUND_GLASSES -> drawGlasses(u)
        AccessoryId.WIRELESS_HEADPHONES -> drawHeadphones(u)
        AccessoryId.WINTER_SCARF -> drawScarf(u)
        AccessoryId.FLOWER_CLIP -> drawFlower(u)
    }
}

private val CapBlue = Color(0xFF7FB6E8)
private val CapBlueDark = Color(0xFF5E97CC)
private val Gold = Color(0xFFF5C24C)
private val GemPink = Color(0xFFE86A8A)
private val GemBlue = Color(0xFF7FB6E8)
private val Lavender = Color(0xFF9C86E0)
private val Coral = Color(0xFFF28B82)
private val PetalPink = Color(0xFFF9B4CB)

private fun DrawScope.shape(u: Float, fill: Color, build: Path.() -> Unit) {
    val path = Path().apply(build)
    drawPath(path, fill)
    drawPath(path, Outline, style = outline(u))
}

private fun DrawScope.drawCap(u: Float) {
    shape(u, CapBlue) {
        moveTo(29 * u, 28 * u)
        cubicTo(29 * u, 11 * u, 71 * u, 11 * u, 71 * u, 28 * u)
        close()
    }
    shape(u, CapBlueDark) {
        moveTo(60 * u, 26.5f * u)
        quadraticTo(80 * u, 24 * u, 86 * u, 29 * u)
        quadraticTo(74 * u, 32 * u, 60 * u, 30 * u)
        close()
    }
    drawCircle(CapBlueDark, radius = 2.2f * u, center = Offset(50 * u, 13.5f * u))
    drawCircle(Outline, radius = 2.2f * u, center = Offset(50 * u, 13.5f * u), style = Stroke(1.4f * u))
}

private fun DrawScope.drawCrown(u: Float) {
    polygon(Gold, u, 35f to 24f, 35f to 10f, 42.5f to 16.5f, 50f to 6f, 57.5f to 16.5f, 65f to 10f, 65f to 24f)
    drawCircle(GemPink, radius = 2.4f * u, center = Offset(50 * u, 18 * u))
    drawCircle(GemBlue, radius = 1.6f * u, center = Offset(41 * u, 20 * u))
    drawCircle(GemBlue, radius = 1.6f * u, center = Offset(59 * u, 20 * u))
}

private fun DrawScope.drawGlasses(u: Float) {
    val lens = Stroke(1.7f * u)
    listOf(39f, 61f).forEach { x ->
        drawCircle(Color.White.copy(alpha = 0.25f), radius = 7 * u, center = Offset(x * u, 46 * u))
        drawCircle(Outline, radius = 7 * u, center = Offset(x * u, 46 * u), style = lens)
    }
    val frame = Path().apply {
        moveTo(46 * u, 45 * u)
        quadraticTo(50 * u, 43 * u, 54 * u, 45 * u)
        moveTo(32 * u, 45 * u)
        lineTo(22 * u, 42.5f * u)
        moveTo(68 * u, 45 * u)
        lineTo(78 * u, 42.5f * u)
    }
    drawPath(frame, Outline, style = Stroke(1.7f * u, cap = StrokeCap.Round))
}

private fun DrawScope.drawHeadphones(u: Float) {
    val band = Path().apply {
        moveTo(21 * u, 47 * u)
        cubicTo(21 * u, 9 * u, 79 * u, 9 * u, 79 * u, 47 * u)
    }
    drawPath(band, Outline, style = Stroke(7f * u, cap = StrokeCap.Round))
    drawPath(band, Lavender, style = Stroke(4.2f * u, cap = StrokeCap.Round))
    listOf(14f, 74f).forEach { x ->
        val topLeft = Offset(x * u, 39 * u)
        val cupSize = Size(12 * u, 18 * u)
        val radius = CornerRadius(5 * u, 5 * u)
        drawRoundRect(Lavender, topLeft, cupSize, radius)
        drawRoundRect(Outline, topLeft, cupSize, radius, style = outline(u))
    }
}

private fun DrawScope.drawScarf(u: Float) {
    // Hanging end first, so the wrap covers its top edge.
    shape(u, Coral) {
        moveTo(60 * u, 70 * u)
        lineTo(68 * u, 70 * u)
        lineTo(70 * u, 84 * u)
        lineTo(61 * u, 84 * u)
        close()
    }
    shape(u, Coral) {
        moveTo(27 * u, 66 * u)
        quadraticTo(50 * u, 76 * u, 73 * u, 66 * u)
        lineTo(73 * u, 73 * u)
        quadraticTo(50 * u, 83 * u, 27 * u, 73 * u)
        close()
    }
    listOf(Triple(38f, 71.5f, 78.5f), Triple(50f, 74f, 81f), Triple(62f, 71.5f, 78.5f)).forEach { (x, y1, y2) ->
        drawLine(Color.White.copy(alpha = 0.7f), Offset(x * u, y1 * u), Offset(x * u, y2 * u), 2f * u, StrokeCap.Round)
    }
    listOf(62f, 65.5f, 69f).forEach { x ->
        drawLine(Outline, Offset(x * u, 84 * u), Offset(x * u, 87 * u), 1.4f * u, StrokeCap.Round)
    }
}

private fun DrawScope.drawFlower(u: Float) {
    val cx = 70f
    val cy = 24f
    repeat(5) { i ->
        val angle = i * 1.2566f - 1.5708f
        val center = Offset((cx + 4.6f * cos(angle)) * u, (cy + 4.6f * sin(angle)) * u)
        drawCircle(PetalPink, radius = 3.4f * u, center = center)
        drawCircle(Outline, radius = 3.4f * u, center = center, style = Stroke(1.3f * u))
    }
    drawCircle(Gold, radius = 2.6f * u, center = Offset(cx * u, cy * u))
    drawCircle(Outline, radius = 2.6f * u, center = Offset(cx * u, cy * u), style = Stroke(1.2f * u))
}
