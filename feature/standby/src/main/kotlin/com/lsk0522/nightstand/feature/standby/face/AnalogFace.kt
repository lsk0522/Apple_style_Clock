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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A ticked dial with sweeping hands.
 *
 * Twelve heavy indices and forty-eight hairlines, as on a watch face: the eye
 * finds the hour from the heavy marks without the dial turning into a ring of
 * identical lines. The minute hand advances smoothly with the seconds rather
 * than stepping, which is what stops it looking mechanical in the wrong way.
 */
@Composable
fun AnalogFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val dial = min(maxWidth, maxHeight) - Margin * 2

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
                val radius = size.minDimension / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)

                // Ticks
                repeat(60) { index ->
                    val heavy = index % 5 == 0
                    val angle = Math.toRadians(index * 6.0 - 90.0)
                    val outer = radius * 0.96f
                    val inner = radius * if (heavy) 0.83f else 0.90f
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
                        strokeWidth = if (heavy) radius * 0.028f else radius * 0.012f,
                        cap = StrokeCap.Round,
                    )
                }

                val seconds = data.now.second + data.now.nano / 1_000_000_000f
                val minutes = data.now.minute + seconds / 60f
                val hours = (data.now.hour % 12) + minutes / 60f

                fun hand(fraction: Float, length: Float, width: Float, colour: androidx.compose.ui.graphics.Color) {
                    val angle = Math.toRadians(fraction * 360.0 - 90.0)
                    drawLine(
                        color = colour,
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

                hand(hours / 12f, 0.52f, 0.055f, palette.textPrimary)
                hand(minutes / 60f, 0.76f, 0.040f, palette.textPrimary)
                if (data.showSeconds) {
                    hand(seconds / 60f, 0.84f, 0.016f, palette.accent)
                }

                drawCircle(palette.accent, radius = radius * 0.035f, center = centre)
            }
        }
    }
}

private val Margin = 24.dp
private val Gap = 44.dp
