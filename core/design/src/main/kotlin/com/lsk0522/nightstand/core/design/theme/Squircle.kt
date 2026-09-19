package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Apple's continuous corner — a superellipse, not a circular arc.
 *
 * `RoundedCornerShape` draws a quarter circle, which meets the straight edge
 * with a visible curvature jump. A continuous corner spreads the curvature over
 * a longer stretch of the edge so the transition reads as smooth (G2 continuity).
 *
 * Each corner is drawn as two cubic bezier curves: the curve leaves the edge at
 * `radius * (1 + smoothing)` from the corner and is pulled toward the corner
 * point, which is what stretches the curvature out.
 *
 * @param radius nominal corner radius, as in [Radius].
 * @param smoothing how far the curve spreads along the edge. 0f degenerates to a
 *   plain rounded rect; Apple's corners sit around 0.6f, which is the default.
 */
class SquircleShape(
    private val radius: Dp,
    private val smoothing: Float = DEFAULT_SMOOTHING,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val r = with(density) { radius.toPx() }
        // A corner can never eat more than half of the shorter side.
        val maxR = minOf(size.width, size.height) / 2f
        val cr = r.coerceIn(0f, maxR)

        if (cr == 0f) {
            return Outline.Rectangle(size.toRect())
        }

        // How far the curve extends along the straight edge, clamped so that
        // opposite corners never overlap on a small tile.
        val spread = (cr * (1f + smoothing))
            .coerceAtMost(minOf(size.width, size.height) / 2f)

        // Control-point pull toward the corner. 0.5523 is the circular-arc
        // constant; easing it down as smoothing rises is what flattens the apex.
        val k = CIRCLE_K / (1f + smoothing)

        val w = size.width
        val h = size.height

        val path = Path().apply {
            // top edge, left to right
            moveTo(spread, 0f)
            lineTo(w - spread, 0f)
            // top-right corner
            cubicTo(
                w - spread + spread * k, 0f,
                w, spread - spread * k,
                w, spread,
            )
            // right edge
            lineTo(w, h - spread)
            // bottom-right corner
            cubicTo(
                w, h - spread + spread * k,
                w - spread + spread * k, h,
                w - spread, h,
            )
            // bottom edge
            lineTo(spread, h)
            // bottom-left corner
            cubicTo(
                spread - spread * k, h,
                0f, h - spread + spread * k,
                0f, h - spread,
            )
            // left edge
            lineTo(0f, spread)
            // top-left corner
            cubicTo(
                0f, spread - spread * k,
                spread - spread * k, 0f,
                spread, 0f,
            )
            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleShape && other.radius == radius && other.smoothing == smoothing

    override fun hashCode(): Int = 31 * radius.hashCode() + smoothing.hashCode()

    companion object {
        const val DEFAULT_SMOOTHING = 0.6f
        private const val CIRCLE_K = 0.5522847f
    }
}

private fun Size.toRect() = androidx.compose.ui.geometry.Rect(0f, 0f, width, height)

/** Widget-card corner — Design.md §3.2. */
val WidgetCardShape = SquircleShape(Radius.lg)

/** Large panel corner. */
val PanelShape = SquircleShape(Radius.xl)
