package com.lsk0522.nightstand.feature.standby

import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.lsk0522.nightstand.core.data.diagnostics.StandbySessionLog
import com.lsk0522.nightstand.core.design.theme.StandbyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * Hosts the StandBy clock.
 *
 * Launched by the charging service, which is why so much of the setup here is
 * about appearing over whatever was already on screen:
 *
 *  - `setShowWhenLocked` puts it above the lock screen. The keyguard is left
 *    locked on purpose — a clock is not a reason to expose the phone.
 *  - `setTurnScreenOn` lights a dark display, since the phone was very likely
 *    asleep on the pad.
 *  - `FLAG_KEEP_SCREEN_ON` holds it awake without a wake lock, which the
 *    system can account for properly.
 */
@AndroidEntryPoint
class StandByActivity : ComponentActivity() {

    /** Measures what a session of this actually costs the battery. */
    @Inject lateinit var sessionLog: StandbySessionLog

    /** Mirrors the Compose dim state so the touch handler can read it. */
    private var dimmed = true

    /** True unless something on screen is animating continuously. */
    private var lowRefreshAllowed = true

    private var idleJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        super.onCreate(savedInstanceState)

        sessionLog.begin()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        goFullscreen()

        setContent {
            val viewModel: StandByViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Starts dimmed so the clock can be left on all night the way an
            // always-on display is; a tap brings it back to normal.
            var dim by remember { mutableStateOf(true) }

            // A sweeping second hand redraws every frame, so the panel must
            // not be parked while one is on screen.
            LaunchedEffect(dim, state.showSeconds, state.autoBrightness, state.ambientLux) {
                applyDim(
                    dim = dim,
                    allowLowRefresh = !state.showSeconds,
                    dimBrightness = if (state.autoBrightness) {
                        brightnessFor(state.ambientLux)
                    } else {
                        FIXED_DIM_BRIGHTNESS
                    },
                )
            }

            // Charging stopped, or stopped matching what the user asked for.
            // Null means the first reading has not arrived yet, so it must not
            // be treated as "no longer charging" -- that would close the screen
            // the instant it opened.
            LaunchedEffect(state.stillCharging) {
                if (state.stillCharging == false) finish()
            }

            StandbyTheme(nightVision = state.nightVision) {
                StandByScreen(
                    state = state,
                    host = viewModel.widgetHost,
                    onSingleTap = { dim = !dim },
                    onExit = ::finish,
                )
            }
        }
    }

    /**
     * Any touch pulls the panel back up to full speed.
     *
     * Without this a swipe is drawn at one frame per second, because the
     * window is still asking for the rate it idles at. The gesture is then
     * unusable — which is exactly what asking for 1Hz unconditionally did.
     */
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        wake()
        return super.dispatchTouchEvent(event)
    }

    override fun onDestroy() {
        sessionLog.end()
        super.onDestroy()
    }

    /** Nothing but the clock; the system bars would only add clutter. */
    private fun goFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun applyDim(dim: Boolean, allowLowRefresh: Boolean, dimBrightness: Float) {
        dimmed = dim
        lowRefreshAllowed = allowLowRefresh

        val target = if (dim) dimBrightness else BRIGHT_BRIGHTNESS
        if (window.attributes.screenBrightness != target) {
            window.attributes = window.attributes.apply { screenBrightness = target }
        }

        if (dim) {
            // Settling back down: let the idle timer decide, so the tap that
            // dimmed it is not itself drawn at 1Hz.
            wake()
        } else {
            idleJob?.cancel()
            requestRefreshRate(low = false)
        }
    }

    /**
     * Full speed now, and back down once the screen has been left alone.
     *
     * The delay is what makes the panel usable and still lets it park: a
     * gesture is a burst of events, and dropping the rate between two of them
     * would stutter the very thing being touched.
     */
    private fun wake() {
        requestRefreshRate(low = false)
        idleJob?.cancel()
        if (!dimmed || !lowRefreshAllowed) return
        idleJob = lifecycleScope.launch {
            delay(IDLE_BEFORE_PARKING_MILLIS)
            requestRefreshRate(low = true)
        }
    }

    /**
     * Asks the panel to idle as slowly as it can, or to stop asking.
     *
     * Requesting 1Hz outright rather than the slowest mode the display
     * advertises: an LTPO panel like the S25 Ultra's can hold a single frame
     * for a second, but it does not publish that as a `Display.Mode`, so
     * picking from `supportedModes` only ever gets down to 60Hz. Zero means no
     * preference at all, which hands the choice back to the system.
     *
     * The other half of this is in the screen itself — it only recomposes when
     * a digit changes. A view that redrew every frame would hold the refresh
     * rate up no matter what the window asked for.
     */
    private fun requestRefreshRate(low: Boolean) {
        val target = if (low) IDLE_REFRESH_HZ else SYSTEM_CHOOSES
        if (window.attributes.preferredRefreshRate == target) return
        window.attributes = window.attributes.apply { preferredRefreshRate = target }
    }

    /**
     * How bright to sit in a room this bright.
     *
     * Logarithmic, because that is how the eye reads it: the step from one lux
     * to ten matters far more than the step from five hundred to a thousand. A
     * linear map leaves the clock either invisible in the dark or dim enough
     * to be useless in daylight.
     *
     * Quantised to twentieths so a flickering sensor does not relayout the
     * window on every reading.
     */
    private fun brightnessFor(lux: Float?): Float {
        if (lux == null) return FIXED_DIM_BRIGHTNESS
        val scale = ln(1f + lux.coerceIn(0f, BRIGHT_ROOM_LUX)) / ln(1f + BRIGHT_ROOM_LUX)
        val raw = MIN_AUTO_BRIGHTNESS + scale * (MAX_AUTO_BRIGHTNESS - MIN_AUTO_BRIGHTNESS)
        return (raw * BRIGHTNESS_STEPS).roundToInt() / BRIGHTNESS_STEPS
    }

    private companion object {

        /** What the panel is asked to hold while the clock just sits there. */
        const val IDLE_REFRESH_HZ = 1f

        /** No preference: the system picks, which is what interaction needs. */
        const val SYSTEM_CHOOSES = 0f

        /** Long enough to outlast a gesture, short enough to still save power. */
        const val IDLE_BEFORE_PARKING_MILLIS = 3_000L

        /** Readable in a dark room without lighting it up, when not measuring. */
        const val FIXED_DIM_BRIGHTNESS = 0.25f

        /** Floor: still legible across a dark bedroom, and no brighter. */
        const val MIN_AUTO_BRIGHTNESS = 0.03f

        /** Ceiling: enough for a lit room, short of full daylight glare. */
        const val MAX_AUTO_BRIGHTNESS = 0.65f

        /** Roughly an office. Past this the clock does not need to keep rising. */
        const val BRIGHT_ROOM_LUX = 800f

        const val BRIGHTNESS_STEPS = 20f

        /** Hand control back to the system's own brightness. */
        const val BRIGHT_BRIGHTNESS = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }
}
