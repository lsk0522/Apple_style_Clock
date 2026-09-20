package com.lsk0522.nightstand.feature.standby

import android.text.format.DateFormat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.Spacing
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * The clock that takes over the screen while charging.
 *
 * For now this is the Digital face — hour stacked over minute, the way Apple's
 * default StandBy clock reads from across a room. The other five faces arrive
 * in Phase 4.
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
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier
                .alpha(fade)
                .offset(x = shift.first, y = shift.second)
                .padding(Spacing.screenMargin),
        ) {
            Text(
                text = now.format(hourFormatter(state.use24Hour)),
                style = NightstandType.HeroClockDisplay,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = now.format(MINUTE_FORMAT),
                style = NightstandType.HeroClockDisplay,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = now.format(dateFormatter()),
                style = NightstandType.Title2,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
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
 * Nudges the whole clock a pixel or two every so often so a static image never
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

private val MINUTE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("mm", Locale.getDefault())

private fun hourFormatter(use24Hour: Boolean): DateTimeFormatter =
    DateTimeFormatter.ofPattern(if (use24Hour) "HH" else "hh", Locale.getDefault())

/**
 * The date, written the way the device's locale writes it. Asking the
 * platform for the pattern is what keeps this correct in every language
 * instead of only the one it was written in.
 */
private fun dateFormatter(): DateTimeFormatter {
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, "EEEEMMMd")
    return DateTimeFormatter.ofPattern(pattern, locale)
}
