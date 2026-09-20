package com.lsk0522.nightstand.feature.standby.face

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lsk0522.nightstand.core.design.theme.NightstandFontFamily
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * A ticked dial with numerals and sweeping hands.
 *
 * Twelve heavy indices and forty-eight hairlines, as on a watch face: the eye
 * finds the hour from the heavy marks without the dial turning into a ring of
 * identical lines. The numerals sit just inside them.
 *
 * With seconds on, the hands are driven by the frame clock so the second hand
 * glides rather than steps — and that is a real trade against the 1Hz idle the
 * rest of the app works for. A sweeping hand means redrawing every frame, so
 * the panel cannot stay parked. Turning seconds off drops this face back to
 * one update a minute.
 */
@Composable
fun AnalogFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby
    val measurer = rememberTextMeasurer()
    val sweep = rememberSweepTime(enabled = data.showSeconds, fallback = data.now)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val dial = minOf(maxWidth, maxHeight) - Margin * 2

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Margin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = data.now.format(weekdayFormatter()),
                    style = NightstandType.Title1,
                    color = palette.textPrimary,
                )
                Text(
                    text = data.now.format(dateFormatter()),
                    style = NightstandType.Title3,
                    color = palette.textSecondary,
                )
                if (data.batteryPercent != null) {
                    Spacer(Modifier.height(10.dp))
                    BatteryLine(data)
                }
            }

            Spacer(Modifier.width(Gap))

            Canvas(modifier = Modifier.size(dial)) {
                // Read inside the draw lambda on purpose: the frame clock then
                // invalidates only the drawing, never the composition.
                val time = sweep.value

                val radius = size.minDimension / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)

                repeat(60) { index ->
                    val heavy = index % 5 == 0
                    val angle = Math.toRadians(index * 6.0 - 90.0)
                    val outer = radius * 0.97f
                    val inner = radius * if (heavy) 0.90f else 0.94f
                    drawLine(
                        color = if (heavy) palette.textPrimary else palette.textTertiary,
                        start = centre + Offset(
                            (cos(angle) * inner).toFloat(),
                            (sin(angle) * inner).toFloat(),
                        ),
                        end = centre + Offset(
                            (cos(angle) * outer).toFloat(),
                            (sin(angle) * outer).toFloat(),
                        ),
                        strokeWidth = if (heavy) radius * 0.026f else radius * 0.010f,
                        cap = StrokeCap.Round,
                    )
                }

                // Numerals, sitting just inside the ticks.
                val numeralStyle = TextStyle(
                    fontFamily = NightstandFontFamily,
                    fontSize = (radius * 0.155f).toSp(),
                    fontWeight = FontWeight.Medium,
                    color = palette.textPrimary,
                )
                for (hour in 1..12) {
                    val angle = Math.toRadians(hour * 30.0 - 90.0)
                    val laid = measurer.measure(hour.toString(), numeralStyle)
                    val ring = radius * 0.77f
                    drawText(
                        textLayoutResult = laid,
                        topLeft = centre + Offset(
                            (cos(angle) * ring).toFloat() - laid.size.width / 2f,
                            (sin(angle) * ring).toFloat() - laid.size.height / 2f,
                        ),
                    )
                }

                val seconds = time.second + time.nano / 1_000_000_000f
                val minutes = time.minute + seconds / 60f
                val hours = (time.hour % 12) + minutes / 60f

                fun hand(fraction: Float, length: Float, width: Float, colour: Color) {
                    val angle = Math.toRadians(fraction * 360.0 - 90.0)
                    drawLine(
                        color = colour,
                        // Overhangs the centre slightly, the way a real hand is
                        // counterweighted.
                        start = centre - Offset(
                            (cos(angle) * radius * 0.08f).toFloat(),
                            (sin(angle) * radius * 0.08f).toFloat(),
                        ),
                        end = centre + Offset(
                            (cos(angle) * radius * length).toFloat(),
                            (sin(angle) * radius * length).toFloat(),
                        ),
                        strokeWidth = radius * width,
                        cap = StrokeCap.Round,
                    )
                }

                hand(hours / 12f, 0.48f, 0.055f, palette.textPrimary)
                hand(minutes / 60f, 0.68f, 0.038f, palette.textPrimary)
                if (data.showSeconds) {
                    hand(seconds / 60f, 0.78f, 0.014f, palette.accent)
                }

                drawCircle(palette.accent, radius = radius * 0.032f, center = centre)
            }
        }
    }
}

/**
 * The time, re-read every frame so the second hand glides.
 *
 * Only while [enabled]; otherwise it hands back [fallback], which ticks once a
 * minute and lets the display idle.
 */
@Composable
private fun rememberSweepTime(
    enabled: Boolean,
    fallback: LocalDateTime,
): State<LocalDateTime> = produceState(initialValue = fallback, key1 = enabled, key2 = fallback) {
    if (!enabled) {
        value = fallback
        return@produceState
    }
    while (true) {
        withFrameNanos { }
        value = LocalDateTime.now()
    }
}

private val Margin = 24.dp
private val Gap = 44.dp
