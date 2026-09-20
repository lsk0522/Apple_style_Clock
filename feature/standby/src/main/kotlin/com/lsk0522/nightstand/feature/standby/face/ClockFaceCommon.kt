package com.lsk0522.nightstand.feature.standby.face

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import com.lsk0522.nightstand.core.design.theme.NightstandType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/** Everything a face needs to draw itself. */
data class ClockFaceData(
    val now: LocalDateTime,
    val use24Hour: Boolean,
    val showSeconds: Boolean,
    val batteryPercent: Int?,
    val charging: Boolean,
)

/**
 * Ticks the clock, waking only as often as the display actually changes.
 *
 * Analog faces with a second hand need every second; a face showing only hours
 * and minutes should sleep until the minute turns. Waking more often than the
 * picture changes would hold the panel's refresh rate up and undo the point of
 * asking for 1Hz.
 */
@Composable
fun rememberClock(tickEverySecond: Boolean) = produceState(
    initialValue = LocalDateTime.now(),
    key1 = tickEverySecond,
) {
    while (true) {
        val now = LocalDateTime.now()
        value = now
        val millisIntoSecond = now.nano / 1_000_000L
        delay(
            if (tickEverySecond) {
                1_000L - millisIntoSecond
            } else {
                (60L - now.second) * 1_000L - millisIntoSecond
            },
        )
    }
}

/**
 * A digit style scaled to the space it has.
 *
 * Trimming to the glyph bounds is what lets stacked rows sit close: left to
 * its default, type this large carries enough built-in leading to open a
 * visible gap.
 */
fun digitStyle(size: TextUnit, weight: FontWeight = FontWeight.Bold): TextStyle =
    NightstandType.HeroClockDisplay.copy(
        fontSize = size,
        lineHeight = size * 0.95f,
        fontWeight = weight,
        letterSpacing = (-0.04).em,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

fun hourFormatter(use24Hour: Boolean): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (use24Hour) "HH" else "hh", Locale.getDefault())

val MinuteFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("mm", Locale.getDefault())

val SecondFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("ss", Locale.getDefault())

/**
 * Dates written the way the device's locale writes them. Asking the platform
 * for the pattern keeps this correct in every language rather than only the
 * one it was written in.
 */
fun dateFormatter(): DateTimeFormatter = localizedFormatter("MMMd")

fun weekdayFormatter(): DateTimeFormatter = localizedFormatter("EEEE")

fun shortTimeFormatter(use24Hour: Boolean): DateTimeFormatter =
    localizedFormatter(if (use24Hour) "Hm" else "hm")

private fun localizedFormatter(skeleton: String): DateTimeFormatter {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(locale, skeleton),
        locale,
    )
}
