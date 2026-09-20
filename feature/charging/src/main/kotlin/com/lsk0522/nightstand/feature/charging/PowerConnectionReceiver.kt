package com.lsk0522.nightstand.feature.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lsk0522.nightstand.core.data.charging.ChargingEventStore
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Wakes when power is connected or disconnected and records what happened.
 *
 * `ACTION_POWER_CONNECTED` and `ACTION_POWER_DISCONNECTED` are on the
 * exemption list for the Android 8 implicit-broadcast restrictions, so unlike
 * most system broadcasts they can still be declared in the manifest and reach
 * a stopped app.
 *
 * TODO(next): Phase 3 — this is where StandBy gets launched. For now it only
 * records the event, which is enough to prove detection works with the app
 * closed.
 */
@AndroidEntryPoint
class PowerConnectionReceiver : BroadcastReceiver() {

    @Inject lateinit var monitor: ChargingStatusMonitor

    @Inject lateinit var eventStore: ChargingEventStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_POWER_CONNECTED &&
            intent.action != Intent.ACTION_POWER_DISCONNECTED
        ) {
            return
        }

        // A receiver is killed as soon as onReceive returns, so the store write
        // has to hold the process open until it finishes.
        val pendingResult = goAsync()
        val type = monitor.currentStatus().type
        val now = System.currentTimeMillis()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                eventStore.record(type, now)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
