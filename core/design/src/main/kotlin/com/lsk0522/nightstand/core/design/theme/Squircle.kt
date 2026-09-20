package com.lsk0522.nightstand.core.design.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

/**
 * Apple's continuous corner, drawn as a superellipse quadrant.
 *
 * A circular corner (`RoundedCornerShape`) jumps from zero curvature along the
 * straight edge to `1/r` the instant the arc begins, and the eye reads that
 * discontinuity as a faint crease. A superellipse ramps the curvature up
 * instead, which is what makes Apple's corners look poured rather than cut.
 *
 * Each corner follows `|dx/r|^n + |dy/r|^n = 1` across its quadrant:
 *
 *  - `n = 2` is exactly a circle
 *  - `n ~ 4` is the continuous corner used across iOS chrome -- the default
 *  - larger `n` squares the corner off
 *
 * The curve still meets each edge exactly `r` from the corner, so the shape
 * honours the radius it was handed.
 *
 * An earlier version spread the curve `1.6 x r` along the edge while pulling
 * its control points only `0.55 x r` inward. That rendered an 18dp corner as a
 * shallow ~29dp sweep -- visibly too round and too flat, which is why this is
 * now solved geometrically instead of with hand-tuned Bezier constants.
 */
class SquircleShape(
    private val radius: Dp,
    private val exponent: Float = DEFAULT_EXPONENT,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        // A corner can never take more than half of the shorter side.
        val maxRadius = minOf(size.width, size.height) / 2f
        val r = with(density) { radius.toPx() }.coerceIn(0f, maxRadius)

        if (r <= 0f) return Outline.Rectangle(Rect(0f, 0f, size.width, size.height))

        val w = size.width
        val h = size.height
        // Enough segments to read smooth at any size, without building a
        // needlessly long path for a small tile.
        val segments = (r * 0.9f).toInt().coerceIn(10, 48)

        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            superellipseQuadrant(w - r, r, r, exponent, START_TOP_RIGHT, segments)

            lineTo(w, h - r)
            superellipseQuadrant(w - r, h - r, r, exponent, START_BOTTOM_RIGHT, segments)

            lineTo(r, h)
            superellipseQuadrant(r, h - r, r, exponent, START_BOTTOM_LEFT, segments)

            lineTo(0f, r)
            superellipseQuadrant(r, r, r, exponent, START_TOP_LEFT, segments)

            close()
        }
        return Outline.Generic(path)
    }

    override fun equals(other: Any?): Boolean =
        other is SquircleShape && other.radius == radius && other.exponent == exponent

    override fun hashCode(): Int = 31 * radius.hashCode() + exponent.hashCode()

    companion object {
        /** The continuous corner iOS uses. 2 would be a plain circle. */
        const val DEFAULT_EXPONENT = 4f

        private const val START_TOP_RIGHT = -90f
        private const val START_BOTTOM_RIGHT = 0f
        private const val START_BOTTOM_LEFT = 90f
        private const val START_TOP_LEFT = 180f
    }
}

/**
 * Walks one 90-degree quadrant of the superellipse centred on ([cx], [cy]),
 * beginning at [startDegrees].
 */
private fun Path.superellipseQuadrant(
    cx: Float,
    cy: Float,
    r: Float,
    exponent: Float,
    startDegrees: Float,
    segments: Int,
) {
    val power = 2.0 / exponent
    for (i in 0..segments) {
        val angle = Math.toRadians((startDegrees + QUADRANT_DEGREES * i / segments).toDouble())
        val c = cos(angle)
        val s = sin(angle)
        val x = cx + r * (c.sign * c.absoluteValue.pow(power)).toFloat()
        val y = cy + r * (s.sign * s.absoluteValue.pow(power)).toFloat()
        lineTo(x, y)
    }
}

private const val QUADRANT_DEGREES = 90f

/**
 * A true capsule -- both ends are half circles.
 *
 * The one place a circular corner is correct rather than a compromise: a
 * floating tab bar or a pill button is a stadium, and running a superellipse
 * through it would flatten the ends into something Apple never draws.
 */
val CapsuleShape: Shape = RoundedCornerShape(percent = 50)

/** App UI grouped list card. */
val ListCardShape = SquircleShape(Radius.listCard)

/** StandBy widget tile. */
val WidgetTileShape = SquircleShape(Radius.widgetTile)

/** Large panel. */
val PanelShape = SquircleShape(Radius.panel)
