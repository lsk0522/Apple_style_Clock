package com.lsk0522.nightstand.feature.standby

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.feature.standby.face.ClockFaceData
import com.lsk0522.nightstand.feature.standby.face.ClockFaceHost
import com.lsk0522.nightstand.feature.standby.face.needsSecondTicks
import com.lsk0522.nightstand.feature.standby.face.rememberClock
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * The clock that takes over the screen while charging.
 *
 * Holds what every face shares — the fade in, the burn-in shift, the tick and
 * the gestures — so each face only has to draw itself.
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
    val now by rememberClock(
        tickEverySecond = state.clockFace.needsSecondTicks(state.showSeconds),
    )

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
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onSingleTap() },
                    onDoubleTap = { onExit() },
                )
            },
    ) {
        ClockFaceHost(
            face = state.clockFace,
            data = ClockFaceData(
                now = now,
                use24Hour = state.use24Hour,
                showSeconds = state.showSeconds,
                batteryPercent = state.batteryPercent,
                charging = state.chargeType.isCharging,
            ),
            modifier = Modifier
                .fillMaxSize()
                .alpha(fade)
                .offset(x = shift.first, y = shift.second),
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
