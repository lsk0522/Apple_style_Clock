package com.lsk0522.nightstand.feature.standby

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.lsk0522.nightstand.core.design.theme.StandbyTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hosts the StandBy clock.
 *
 * Launched by the charging receiver, which is why so much of the setup here is
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
        requestLowestRefreshRate()

        setContent {
            val viewModel: StandByViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Charging stopped, or stopped matching what the user asked for.
            // Null means the first reading has not arrived yet, so it must not
            // be treated as "no longer charging" -- that would close the screen
            // the instant it opened.
            LaunchedEffect(state.stillCharging) {
                if (state.stillCharging == false) finish()
            }

            StandbyTheme(nightVision = false) {
                StandByScreen(state = state, onExit = ::finish)
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
     * Asks for the slowest mode the panel offers.
     *
     * The S25 Ultra's display runs from 1Hz to 120Hz, and a clock that changes
     * once a minute has no use for the top of that range. On a phone left
     * charging all night the difference in heat and power is worth asking for.
     */
    private fun requestLowestRefreshRate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val slowest = display?.supportedModes
            ?.minByOrNull { it.refreshRate }
            ?.refreshRate
            ?: return
        window.attributes = window.attributes.apply { preferredRefreshRate = slowest }
    }
}
