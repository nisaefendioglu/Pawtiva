package com.nisaefendioglu.pawtiva.core.designsystem.pet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.nisaefendioglu.pawtiva.core.designsystem.theme.PawtivaTheme
import com.nisaefendioglu.pawtiva.core.model.PetAppearance
import com.nisaefendioglu.pawtiva.core.model.PetMood
import kotlinx.coroutines.delay

/** A one-off reaction the pet plays on top of its resting mood. */
enum class PetReaction {
    /** Small happy hop, e.g. a habit was completed or the pet was tapped. */
    HOP,

    /** Double hop with the celebrating face, e.g. level-up or all habits done. */
    CELEBRATE,
}

/**
 * The pet as shown in the app: illustration, visible accessories and reactions.
 *
 * To trigger a reaction, pass a new [reactionKey] (any value that changes, such as a counter)
 * together with the [reaction] to play. With reduced motion the pet does not move, but the
 * celebrating face is still shown briefly so the feedback is not lost.
 *
 * This is the single rendering entry point, so a future animated or 3D renderer can replace
 * [PetIllustration] here without touching any screen.
 */
@Composable
fun PetDisplay(
    appearance: PetAppearance,
    contentDescription: String,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    reaction: PetReaction = PetReaction.HOP,
    reactionKey: Any? = null,
) {
    val reduceMotion = PawtivaTheme.reduceMotion || !animate
    val hop = remember { Animatable(0f) }
    var celebrating by remember { mutableStateOf(false) }

    LaunchedEffect(reactionKey) {
        if (reactionKey == null) return@LaunchedEffect
        val hops = if (reaction == PetReaction.CELEBRATE) 2 else 1
        celebrating = reaction == PetReaction.CELEBRATE
        if (!reduceMotion) {
            repeat(hops) {
                hop.animateTo(-0.08f, tween(durationMillis = 140, easing = FastOutSlowInEasing))
                hop.animateTo(0f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium))
            }
        }
        if (celebrating) {
            delay(CELEBRATION_MILLIS)
            celebrating = false
        }
    }

    PetIllustration(
        type = appearance.type,
        mood = if (celebrating) PetMood.CELEBRATING else appearance.mood,
        contentDescription = contentDescription,
        modifier = modifier.graphicsLayer { translationY = hop.value * size.height },
        animate = animate,
        accessories = appearance.visibleAccessories,
    )
}

private const val CELEBRATION_MILLIS = 1_600L
