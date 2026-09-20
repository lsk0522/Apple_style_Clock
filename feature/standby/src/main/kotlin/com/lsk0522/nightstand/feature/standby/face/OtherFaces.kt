package com.lsk0522.nightstand.feature.standby.face

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.lsk0522.nightstand.core.design.theme.NightstandColor
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Local time beside a handful of other cities.
 *
 * No map: drawing a world map at this size would be decoration rather than
 * information, and the thing people actually read off the World face is what
 * time it is somewhere else.
 */
@Composable
fun WorldFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usable = maxHeight - Margin * 2
        val digitSize = with(LocalDensity.current) { (usable * 0.30f).toSp() }

        Row(
            modifier = Modifier.align(Alignment.Center).padding(Margin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = data.now.format(hourFormatter(data.use24Hour)),
                    style = digitStyle(digitSize),
                    color = palette.textPrimary,
                )
                Text(
                    text = data.now.format(MinuteFormat),
                    style = digitStyle(digitSize),
                    color = palette.textPrimary,
                )
            }

            Spacer(Modifier.width(48.dp))

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Cities.forEach { zone ->
                    val there = ZonedDateTime.now(ZoneId.of(zone))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = cityLabel(zone),
                            style = NightstandType.Title3,
                            color = palette.textSecondary,
                            modifier = Modifier.width(96.dp),
                        )
                        Text(
                            text = there.format(shortTimeFormatter(data.use24Hour)),
                            style = NightstandType.Title2,
                            color = palette.textPrimary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The sun's path across the day, with the current hour marked on it.
 *
 * The arc runs from one horizon to the other and the marker sits where the sun
 * is now, so a glance says roughly how much daylight is left without reading a
 * number.
 */
@Composable
fun SolarFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby
    val minutesIntoDay = data.now.hour * 60 + data.now.minute
    val progress = minutesIntoDay / (24f * 60f)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usable = maxHeight - Margin * 2
        val digitSize = with(LocalDensity.current) { (usable * 0.34f).toSp() }

        Canvas(modifier = Modifier.fillMaxSize().padding(Margin)) {
            val radius = size.width * 0.42f
            val centre = Offset(size.width / 2f, size.height * 0.92f)

            // The arc: dawn on the left, dusk on the right.
            drawArc(
                brush = Brush.horizontalGradient(
                    listOf(
                        NightstandColor.Standby.NightRed,
                        NightstandColor.Ios.OrangeDark,
                        NightstandColor.Ios.BlueDark,
                    ),
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(centre.x - radius, centre.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
            )

            val angle = Math.toRadians(180.0 + progress * 180.0)
            val sun = centre + Offset(
                (cos(angle) * radius).toFloat(),
                (sin(angle) * radius).toFloat(),
            )
            drawCircle(palette.accent, radius = 9.dp.toPx(), center = sun)
        }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(bottom = usable * 0.12f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = data.now.format(shortTimeFormatter(data.use24Hour)),
                style = digitStyle(digitSize),
                color = palette.textPrimary,
            )
            Text(
                text = data.now.format(dateFormatter()),
                style = NightstandType.Title3,
                color = palette.textSecondary,
            )
        }
    }
}

/**
 * Oversized rounded numerals in colour, and nothing else.
 *
 * The loudest of the faces: the time fills the screen edge to edge with no
 * date or battery competing for attention.
 */
@Composable
fun FloatFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usable = maxHeight - Margin * 2
        val digitSize = with(LocalDensity.current) { (usable * 0.48f).toSp() }

        Column(
            modifier = Modifier.align(Alignment.Center).padding(Margin),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = data.now.format(hourFormatter(data.use24Hour)),
                style = digitStyle(digitSize).copy(letterSpacing = (-0.02).em),
                color = palette.accent,
            )
            Text(
                text = data.now.format(MinuteFormat),
                style = digitStyle(digitSize).copy(letterSpacing = (-0.02).em),
                color = palette.accent,
            )
        }
    }
}

/**
 * As little as possible: one thin weight, one colour, generous space.
 *
 * Where Float shouts, this one is meant to disappear into a dark room.
 */
@Composable
fun MinimalMonoFace(data: ClockFaceData, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val usable = maxHeight - Margin * 2
        val digitSize = with(LocalDensity.current) { (usable * 0.34f).toSp() }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = data.now.format(shortTimeFormatter(data.use24Hour)),
                    style = digitStyle(digitSize, weight = FontWeight.Light)
                        .copy(letterSpacing = 0.em),
                    color = palette.textPrimary,
                )
                Text(
                    text = data.now.format(dateFormatter()),
                    style = NightstandType.Title3.copy(fontWeight = FontWeight.Light),
                    color = palette.textTertiary,
                )
            }
        }
    }
}

/**
 * TODO(next): let the user pick these. Labels are derived from the zone id so
 * no city name is hard-coded in one language.
 */
private val Cities = listOf(
    "America/New_York",
    "Europe/London",
    "Asia/Tokyo",
)

private fun cityLabel(zoneId: String) =
    zoneId.substringAfterLast('/').replace('_', ' ')

private val Margin = 28.dp
