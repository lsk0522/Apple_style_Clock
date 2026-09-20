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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
 * Laid out the way Apple's is: the time is the whole point, so it is set as
 * large as the screen allows, with the quieter information in a column beside
 * it rather than stacked underneath. Centring everything and hanging the date
 * below reads like a screensaver; this reads like a clock.
 *
 * The remaining five faces arrive in Phase 4.
 *
 * @param onExit called on a double tap, the gesture that dismisses StandBy.
 */
@Composable
fun StandByScreen(
    state: StandByUiState,
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas)
            .pointerInput(onExit) {
                detectTapGestures(onDoubleTap = { onExit() })
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .alpha(fade)
                .offset(x = shift.first, y = shift.second)
                .padding(horizontal = SIDE_MARGIN, vertical = TOP_MARGIN),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SideInfo(
                now = now,
                batteryPercent = state.batteryPercent,
                charging = state.chargeType.isCharging,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(SIDE_MARGIN))
            TimeStack(
                now = now,
                use24Hour = state.use24Hour,
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

/**
 * Hour above minute, sized to whatever height it is given.
 *
 * A fixed point size would either leave the screen half empty or overflow it,
 * depending on the phone. Measuring first is what lets the same face fill a
 * tall panel and a short one.
 */
@Composable
private fun TimeStack(
    now: LocalDateTime,
    use24Hour: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby

    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val digitSize = with(LocalDensity.current) { (maxHeight * DIGIT_HEIGHT_RATIO).toSp() }
        val style = NightstandType.HeroClockDisplay.copy(
            fontSize = digitSize,
            lineHeight = digitSize * LINE_HEIGHT_RATIO,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.04).em,
            lineHeightStyle = LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Center,
                trim = LineHeightStyle.Trim.Both,
            ),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = now.format(hourFormatter(use24Hour)),
                style = style,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = now.format(MINUTE_FORMAT),
                style = style,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
        }
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
            Spacer(Modifier.size(10.dp))
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

/** How much of the available height one of the two digit rows takes. */
private const val DIGIT_HEIGHT_RATIO = 0.46f
private const val LINE_HEIGHT_RATIO = 0.95f

private val SIDE_MARGIN = 40.dp
private val TOP_MARGIN = 28.dp
