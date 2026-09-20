package com.lsk0522.nightstand.feature.standby

import android.text.format.DateFormat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.lsk0522.nightstand.core.design.R as DesignR
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * The clock that takes over the screen while charging — the Digital face.
 *
 * The date column and the time sit together as one block, centred on the
 * screen. An earlier version gave the column `weight(1f)`, which pushed the
 * two halves to opposite edges: the date pinned far left, the clock far right,
 * and nothing in between. They read as two things rather than one clock.
 *
 * The remaining five faces arrive in Phase 4.
 *
 * @param onSingleTap toggles the dimmed, always-on brightness.
 * @param onExit called on a double tap, the gesture that dismisses StandBy.
 */
@Composable
fun StandByScreen(
    state: StandByUiState,
    onSingleTap: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby
    val now by rememberClock(showSeconds = state.showSeconds)

    // Eased up from black rather than cut in, which is what makes it feel like
    // the phone settled rather than switched.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val fade by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = NightstandMotion.STANDBY_FADE_IN_MS,
            easing = LinearEasing,
        ),
        label = "standbyFade",
    )

    val shift = rememberPixelShift(enabled = state.burnInProtection)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { onExit() },
                )
            },
    ) {
        // Sized from the screen rather than a fixed point size, so the same
        // face fills a tall panel and a short one without clipping either.
        val usableHeight = maxHeight - VERTICAL_MARGIN * 2
        val digitSize = with(LocalDensity.current) {
            (usableHeight * DIGIT_HEIGHT_RATIO).toSp()
        }

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(fade)
                .offset(x = shift.first, y = shift.second)
                .padding(horizontal = SIDE_MARGIN, vertical = VERTICAL_MARGIN),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SideInfo(
                now = now,
                batteryPercent = state.batteryPercent,
                charging = state.chargeType.isCharging,
            )
            Spacer(Modifier.width(COLUMN_GAP))
            TimeStack(
                now = now,
                use24Hour = state.use24Hour,
                digitSize = digitSize,
            )
        }
    }
}

/** Hour above minute, at the size the screen was measured for. */
@Composable
private fun TimeStack(
    now: LocalDateTime,
    use24Hour: Boolean,
    digitSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby

    // Trimming to the glyph bounds is what lets the two rows sit close. Left
    // to its default, a font this large carries enough built-in leading to
    // open a visible gap between the hour and the minute.
    val style: TextStyle = NightstandType.HeroClockDisplay.copy(
        fontSize = digitSize,
        lineHeight = digitSize * LINE_HEIGHT_RATIO,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.04).em,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Center,
            trim = LineHeightStyle.Trim.Both,
        ),
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = now.format(hourFormatter(use24Hour)),
            style = style,
            color = palette.textPrimary,
        )
        Text(
            text = now.format(MINUTE_FORMAT),
            style = style,
            color = palette.textPrimary,
        )
    }
}

/** Date and charge, kept deliberately quiet next to the time. */
@Composable
private fun SideInfo(
    now: LocalDateTime,
    batteryPercent: Int?,
    charging: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = now.format(weekdayFormatter()),
            style = NightstandType.Title1,
            color = palette.textPrimary,
        )
        Text(
            text = now.format(dateFormatter()),
            style = NightstandType.Title3,
            color = palette.textSecondary,
        )

        if (batteryPercent != null) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (charging) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_bolt),
                        contentDescription = null,
                        tint = palette.accent,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = "$batteryPercent%",
                    style = NightstandType.Title3,
                    color = if (charging) palette.accent else palette.textTertiary,
                )
            }
        }
    }
}

/**
 * Ticks the clock, waking only as often as the display actually changes —
 * once a second when seconds are shown, otherwise on the minute.
 *
 * This is what lets the panel idle: a recomposition every frame would hold the
 * refresh rate up no matter what the window asks for.
 */
@Composable
private fun rememberClock(showSeconds: Boolean) = produceState(
    initialValue = LocalDateTime.now(),
    key1 = showSeconds,
) {
    while (true) {
        val now = LocalDateTime.now()
        value = now
        val millisIntoSecond = now.nano / 1_000_000L
        delay(
            if (showSeconds) {
                1_000L - millisIntoSecond
            } else {
                (60L - now.second) * 1_000L - millisIntoSecond
            },
        )
    }
}

/**
 * Nudges the whole face a pixel or two every so often so a static image never
 * sits on the same OLED sub-pixels long enough to burn in.
 */
@Composable
private fun rememberPixelShift(enabled: Boolean) = produceState(
    initialValue = 0.dp to 0.dp,
    key1 = enabled,
) {
    if (!enabled) {
        value = 0.dp to 0.dp
        return@produceState
    }
    val max = NightstandMotion.PIXEL_SHIFT_MAX_DP
    while (true) {
        value = Random.nextDouble(-max.toDouble(), max.toDouble()).dp to
            Random.nextDouble(-max.toDouble(), max.toDouble()).dp
        delay(NightstandMotion.PIXEL_SHIFT_INTERVAL)
    }
}.value

private val MINUTE_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("mm", Locale.getDefault())

private fun hourFormatter(use24Hour: Boolean): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (use24Hour) "HH" else "hh", Locale.getDefault())

/**
 * Dates written the way the device's locale writes them. Asking the platform
 * for the pattern keeps this correct in every language rather than only the
 * one it was written in.
 */
private fun dateFormatter(): DateTimeFormatter = localizedFormatter("MMMd")

private fun weekdayFormatter(): DateTimeFormatter = localizedFormatter("EEEE")

private fun localizedFormatter(skeleton: String): DateTimeFormatter {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(
        DateFormat.getBestDateTimePattern(locale, skeleton),
        locale,
    )
}

/** How much of the usable height one of the two digit rows takes. */
private const val DIGIT_HEIGHT_RATIO = 0.42f
private const val LINE_HEIGHT_RATIO = 0.95f

private val SIDE_MARGIN: Dp = 40.dp
private val VERTICAL_MARGIN: Dp = 24.dp
private val COLUMN_GAP: Dp = 44.dp
