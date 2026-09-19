package com.lsk0522.nightstand.core.design.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/**
 * The Liquid Glass material, as iOS 27 tuned it.
 *
 * Four layers, and dropping any one of them turns it back into a plain
 * translucent panel:
 *
 *  1. a **blur of whatever is painted behind it**, sampled through Haze.
 *     Compose on its own can only blur a layer's *own* content, so this is the
 *     one part that needs a library.
 *  2. a tint that is mostly opaque. iOS 26 shipped glass far more transparent
 *     and Apple pulled it back in 27 because text over busy content was hard
 *     to read; this follows the 27 setting.
 *  3. a **specular highlight** down the top, as if a light source sat above.
 *  4. a rim that runs bright at the top and **dark at the bottom** — the
 *     darkened edge iOS 27 added so glass separates from its background.
 *
 * @param hazeState the state whose [dev.chrisbanes.haze.hazeSource] content
 *   gets blurred. Pass the state belonging to the content *behind* this
 *   surface; never one whose source contains this surface, or the effect feeds
 *   on its own output. Null falls back to a flat tint.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape,
    hazeState: HazeState? = null,
): Modifier {
    val palette = NightstandTheme.palette
    val specular = palette.glassSpecular

    return this
        .clip(shape)
        .then(
            if (hazeState != null) {
                Modifier.hazeEffect(state = hazeState) {
                    blurRadius = BLUR_RADIUS
                    // Noise reads as grain on a phone screen; Apple's glass has none.
                    noiseFactor = 0f
                    backgroundColor = palette.groupedBackground
                    tints = listOf(HazeTint(palette.glass))
                    // Below API 31 there is no hardware blur, so the surface has
                    // to carry its own weight as a plain tint.
                    fallbackTint = HazeTint(palette.glass.copy(alpha = FALLBACK_ALPHA))
                }
            } else {
                Modifier.background(palette.glass)
            },
        )
        // Light falling on the top face of the surface.
        .background(
            Brush.verticalGradient(
                0f to specular.copy(alpha = specular.alpha * 0.55f),
                0.42f to Color.Transparent,
            ),
        )
        // Bright where it catches the light, dark where it turns away.
        .border(
            BorderStroke(
                width = RIM_WIDTH,
                brush = Brush.verticalGradient(listOf(specular, palette.glassEdge)),
            ),
            shape = shape,
        )
}

private val BLUR_RADIUS = 24.dp
private val RIM_WIDTH = 0.8.dp
private const val FALLBACK_ALPHA = 0.94f
