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

/**
 * The Liquid Glass material, as iOS 27 tuned it.
 *
 * Three layers, and all three are needed — drop any one and it reads as a
 * plain translucent panel rather than glass:
 *
 *  1. a fill that is *mostly* opaque. iOS 26 shipped this far more transparent
 *     and Apple pulled it back in 27 because text over busy content was hard
 *     to read; this follows the 27 setting.
 *  2. a **specular highlight** down the top of the surface, as if a light
 *     source sat above it.
 *  3. a rim that runs bright at the top and **dark at the bottom** — the
 *     darkened edge iOS 27 added so glass separates from what is behind it.
 *
 * Real backdrop blur is not applied here. Compose can blur a layer's own
 * content but not what is painted behind it, and the pre-Android-12 fallback
 * would be worse than none. At iOS 27's reduced transparency the difference is
 * small.
 *
 * TODO(next): on API 31+ sample the content behind through a RenderEffect so
 * the material actually refracts, rather than only tinting.
 */
@Composable
fun Modifier.liquidGlass(shape: Shape): Modifier {
    val palette = NightstandTheme.palette
    return liquidGlass(
        shape = shape,
        fill = palette.glass,
        specular = palette.glassSpecular,
        edge = palette.glassEdge,
    )
}

fun Modifier.liquidGlass(
    shape: Shape,
    fill: Color,
    specular: Color,
    edge: Color,
): Modifier = this
    .clip(shape)
    .background(fill)
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
            brush = Brush.verticalGradient(listOf(specular, edge)),
        ),
        shape = shape,
    )

private val RIM_WIDTH = 0.8.dp
