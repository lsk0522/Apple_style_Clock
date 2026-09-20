package com.lsk0522.nightstand.feature.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.data.charging.ChargingEventStore
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptLog
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptOutcome
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
 * directly.
 */
@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {

    @Inject lateinit var monitor: ChargingStatusMonitor

    @Inject lateinit var eventStore: ChargingEventStore

    @Inject lateinit var attemptLog: StandbyAttemptLog

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
        // two, which leaves room.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val trigger = settings.settings.first().chargingTrigger

                if (!connected) {
                    eventStore.record(ChargeType.NONE, System.currentTimeMillis())
                    attemptLog.record(
                        StandbyAttemptOutcome.DISCONNECTED,
                        ChargeType.NONE,
                        trigger,
                    )
                    return@launch
                }

                // Wait *before* deciding.
                //
                // At the instant POWER_CONNECTED arrives the battery broadcast
                // has usually not caught up: EXTRA_STATUS can still read as
                // discharging, and a wireless pad frequently reports AC for a
                // moment before settling on WIRELESS. Reading the type here
                // rather than after the wait is what made the clock never
                // appear on a real charger, even though a forced launch worked.
                delay(ENTER_DELAY_MILLIS)

                val status = monitor.currentStatus()
                eventStore.record(status.type, System.currentTimeMillis())

                val outcome = resolveOutcome(trigger, status.type)
                attemptLog.record(outcome, status.type, trigger)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun resolveOutcome(
        trigger: ChargingTrigger,
        type: ChargeType,
    ): StandbyAttemptOutcome = when {
        // Power was pulled during the wait, or never really started.
        !type.isCharging -> StandbyAttemptOutcome.CANCELLED_EARLY

        !trigger.matches(type) -> StandbyAttemptOutcome.TRIGGER_MISMATCH

        // A background start without this is silently dropped by the system.
        !requirements.canDrawOverlays() -> StandbyAttemptOutcome.NO_OVERLAY_PERMISSION

        else -> runCatching {
            standbyLauncher.launch()
            StandbyAttemptOutcome.LAUNCHED
        }.getOrDefault(StandbyAttemptOutcome.LAUNCH_FAILED)
    }

    private companion object {
        /** Apple waits about this long before StandBy appears. */
        const val ENTER_DELAY_MILLIS = 2_000L
    }
}
