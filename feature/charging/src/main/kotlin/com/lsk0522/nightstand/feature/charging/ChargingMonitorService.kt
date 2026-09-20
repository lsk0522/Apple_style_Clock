package com.lsk0522.nightstand.feature.charging

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.charging.ScreenStateMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptLog
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptOutcome
import com.lsk0522.nightstand.core.data.standby.StandbyLauncher
import com.lsk0522.nightstand.core.data.system.SystemRequirements
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Lives only while the phone is charging, and decides when to put the clock up.
 *
 * Something has to be running to catch the right moment: the screen turning
 * off is reported through a broadcast that cannot be declared in a manifest,
 * and charging has to be watched continuously so the service knows when to
 * stand down. A job cannot do either.
 *
 * Because it starts and stops with the charger, the notification it has to
 * show is only ever visible while the phone is plugged in — which is also the
 * one time its battery cost does not matter.
 */
@AndroidEntryPoint
class ChargingMonitorService : Service() {

    @Inject lateinit var chargingMonitor: ChargingStatusMonitor

    @Inject lateinit var screenMonitor: ScreenStateMonitor

    @Inject lateinit var settings: SettingsRepository

    @Inject lateinit var requirements: SystemRequirements

    @Inject lateinit var attemptLog: StandbyAttemptLog

    @Inject lateinit var standbyLauncher: StandbyLauncher

    @Inject lateinit var scheduler: ChargingJobScheduler

    private val scope = CoroutineScope(SupervisorJob())
    private var watcher: Job? = null

    /**
     * Whether the clock has already been put up for this charging session.
     *
     * Re-armed when the phone is unplugged, and — if the user asked for the
     * clock to keep coming back — every time the screen goes off again.
     */
    private var alreadyShown = false

    /**
     * The last screen reading, so a screen-off can be told from a repeat.
     *
     * The flow re-emits on every battery tick, so "the screen is off" is not
     * the same event as "the screen just went off", and only the transition
     * should bring the clock back.
     */
    private var wasInteractive = true

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundNotification()
        if (watcher == null) watcher = scope.launch { watch() }
        return START_STICKY
    }

    private suspend fun watch() {
        // Give the reading a moment to settle before the first decision. At the
        // instant charging begins the battery broadcast often still reads as
        // discharging, and a wireless pad can report AC before settling on
        // WIRELESS.
        delay(SETTLE_MILLIS)

        combine(
            chargingMonitor.status,
            screenMonitor.state,
        ) { status, screen -> status to screen }
            .collect { (status, screen) ->
                val prefs = settings.settings.first()
                val trigger = prefs.chargingTrigger

                if (!status.type.isCharging) {
                    // Unplugged. Hand the wake-up duty back to the scheduler
                    // and get out of the notification shade.
                    attemptLog.record(
                        StandbyAttemptOutcome.DISCONNECTED,
                        ChargeType.NONE,
                        trigger,
                    )
                    standDown()
                    return@collect
                }

                // The screen going dark means the clock is not up: StandBy
                // holds it on for as long as it is showing. So this is the
                // moment the phone was set back down, and the moment to offer
                // the clock again for anyone who asked to have it every time
                // rather than once a charge.
                val wentDark = wasInteractive && !screen.isInteractive
                wasInteractive = screen.isInteractive
                if (wentDark && prefs.persistence == StandbyPersistence.WHILE_CHARGING) {
                    alreadyShown = false
                }

                if (alreadyShown) return@collect

                val outcome = decide(trigger, status.type, screen.isIdle)
                attemptLog.record(outcome, status.type, trigger)
                if (outcome == StandbyAttemptOutcome.LAUNCHED) alreadyShown = true
            }
    }

    private fun decide(
        trigger: ChargingTrigger,
        type: ChargeType,
        screenIdle: Boolean,
    ): StandbyAttemptOutcome = when {
        !trigger.matches(type) -> StandbyAttemptOutcome.TRIGGER_MISMATCH

        // The phone is in someone's hand. Wait — the next screen-off will
        // bring this block round again.
        !screenIdle -> StandbyAttemptOutcome.SCREEN_IN_USE

        !requirements.canDrawOverlays() -> StandbyAttemptOutcome.NO_OVERLAY_PERMISSION

        else -> runCatching {
            standbyLauncher.launch()
            StandbyAttemptOutcome.LAUNCHED
        }.getOrDefault(StandbyAttemptOutcome.LAUNCH_FAILED)
    }

    private fun standDown() {
        scheduler.schedule()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundNotification() {
        createChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.charging_service_title))
            .setContentText(getString(R.string.charging_service_text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            },
        )
    }

    private fun createChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.charging_service_channel),
            // Lowest that still permits a foreground service: no sound, and
            // collapsed out of the way.
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.charging_service_channel_why)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "charging_monitor"
        const val NOTIFICATION_ID = 1
        const val SETTLE_MILLIS = 2_000L
    }
}
