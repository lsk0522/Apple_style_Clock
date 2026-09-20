package com.lsk0522.nightstand.feature.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lsk0522.nightstand.core.data.charging.ChargingEventStore
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.standby.StandbyLauncher
import com.lsk0522.nightstand.core.data.system.SystemRequirements
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Wakes when power is connected or disconnected, and brings up StandBy when
 * the charging matches what the user asked for.
 *
 * `ACTION_POWER_CONNECTED` and `ACTION_POWER_DISCONNECTED` are on the
 * exemption list for the Android 8 implicit-broadcast restrictions, so unlike
 * most system broadcasts they still reach a stopped app from the manifest.
 *
 * **No foreground service.** Holding the overlay permission — which the setup
 * screen walks the user through — already exempts the app from Android 10's
 * background activity-start restriction, so the receiver can launch the screen
 * directly. A service that existed only to wait two seconds would cost a
 * permanent notification, battery, and a Play policy justification, and buy
 * nothing.
 */
@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {

    @Inject lateinit var monitor: ChargingStatusMonitor

    @Inject lateinit var eventStore: ChargingEventStore

    @Inject lateinit var settings: SettingsRepository

    @Inject lateinit var requirements: SystemRequirements

    @Inject lateinit var standbyLauncher: StandbyLauncher

    override fun onReceive(context: Context, intent: Intent) {
        val connected = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> true
            Intent.ACTION_POWER_DISCONNECTED -> false
            else -> return
        }

        // A receiver is killed as soon as onReceive returns, so the work has to
        // hold the process open. The budget is ten seconds; the wait below is
        // two, which leaves plenty of room.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val status = monitor.currentStatus()
                eventStore.record(status.type, System.currentTimeMillis())

                if (!connected) return@launch

                val trigger = settings.settings.first().chargingTrigger
                if (!trigger.matches(status.type)) return@launch

                // Without this the start is silently dropped, so skip the wait
                // entirely rather than lighting up the CPU for nothing.
                // TODO(next): fall back to the DreamService path here.
                if (!requirements.canDrawOverlays()) return@launch

                delay(ENTER_DELAY_MILLIS)

                // The cable may have been pulled during the wait. Confirm
                // before taking over the screen.
                if (trigger.matches(monitor.currentStatus().type)) {
                    standbyLauncher.launch()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        /** Apple waits about this long before StandBy appears. */
        const val ENTER_DELAY_MILLIS = 2_000L
    }
}
