package com.lsk0522.nightstand.feature.standby

import android.os.Bundle
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
import com.lsk0522.nightstand.core.design.theme.StandbyTheme
import dagger.hilt.android.AndroidEntryPoint

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

    override fun onCreate(savedInstanceState: Bundle?) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        goFullscreen()
        requestLowRefreshRate()

        setContent {
            val viewModel: StandByViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Starts dimmed so the clock can be left on all night the way an
            // always-on display is; a tap brings it back to normal.
            var dimmed by remember { mutableStateOf(true) }
            LaunchedEffect(dimmed) { applyBrightness(dimmed) }

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
                    onSingleTap = { dimmed = !dimmed },
                    onExit = ::finish,
                )
            }
        }
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

    /**
     * Asks the panel to idle as slowly as it can.
     *
     * Requesting 1Hz outright rather than the slowest mode the display
     * advertises: an LTPO panel like the S25 Ultra's can hold a single frame
     * for a second, but it does not publish that as a `Display.Mode`, so
     * picking from `supportedModes` only ever gets down to 60Hz. The system
     * clamps this to whatever it can actually do.
     *
     * The other half of this is in the screen itself — it only recomposes when
     * a digit changes. A view that redrew every frame would hold the refresh
     * rate up no matter what the window asked for.
     */
    private fun requestLowRefreshRate() {
        window.attributes = window.attributes.apply {
            preferredRefreshRate = TARGET_REFRESH_HZ
        }
    }

    private fun applyBrightness(dimmed: Boolean) {
        window.attributes = window.attributes.apply {
            screenBrightness = if (dimmed) DIM_BRIGHTNESS else BRIGHT_BRIGHTNESS
        }
    }

    private companion object {
        const val TARGET_REFRESH_HZ = 1f

        /** Readable in a dark room without lighting it up. */
        const val DIM_BRIGHTNESS = 0.25f

        /** Hand control back to the system's own brightness. */
        const val BRIGHT_BRIGHTNESS = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
    }
}
