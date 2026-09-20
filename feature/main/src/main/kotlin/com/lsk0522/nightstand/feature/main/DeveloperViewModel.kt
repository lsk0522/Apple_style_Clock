package com.lsk0522.nightstand.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingStatus
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.charging.ScreenState
import com.lsk0522.nightstand.core.data.charging.ScreenStateMonitor
import com.lsk0522.nightstand.core.data.diagnostics.CrashRecorder
import com.lsk0522.nightstand.core.data.diagnostics.StandbySession
import com.lsk0522.nightstand.core.data.diagnostics.StandbySessionLog
import com.lsk0522.nightstand.core.data.sensor.AmbientLightMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.standby.StandbyAttempt
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptLog
import com.lsk0522.nightstand.core.data.standby.StandbyLauncher
import com.lsk0522.nightstand.core.data.system.AppVersion
import com.lsk0522.nightstand.core.data.system.SystemRequirements
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.data.widget.HostedWidgetStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

data class DeveloperUiState(
    val charging: ChargingStatus = ChargingStatus.Unknown,
    val screen: ScreenState = ScreenState(isInteractive = true, isLocked = false),
    /** Null when the phone has no light sensor, which is a real answer. */
    val lux: Float? = null,
    val lightSensorAvailable: Boolean = false,
    val isDark: Boolean = false,
    val trigger: ChargingTrigger = ChargingTrigger.WIRELESS_ONLY,
    val persistence: StandbyPersistence = StandbyPersistence.ONCE_PER_CHARGE,
    val widgets: List<HostedWidget> = emptyList(),
    val history: List<StandbyAttempt> = emptyList(),
    val appVersion: String = "",
    val overlayGranted: Boolean = false,
    val batteryUnrestricted: Boolean = false,
    val notificationsGranted: Boolean = false,
    val sessions: List<StandbySession> = emptyList(),

    /** The last stack trace, or null if nothing has crashed. */
    val lastCrash: String? = null,
) {
    /** Whether the current rule would let [type] bring the clock up. */
    fun wouldTrigger(type: ChargeType): Boolean = trigger.matches(type)

    /**
     * Average battery cost of a StandBy session, in percent per hour.
     *
     * Only the sessions that can answer the question: long enough to move a
     * whole-percent reading, and off the charger. Null when none of them
     * qualify, which is a more honest answer than averaging noise.
     */
    val averageDrainPerHour: Float?
        get() = sessions.mapNotNull { it.drainPerHour }.takeIf { it.isNotEmpty() }?.average()
            ?.toFloat()

    /**
     * The first thing standing in the way right now, in the order the charging
     * service actually checks them.
     */
    val blockedBy: DeveloperBlocker
        get() = when {
            !charging.type.isCharging -> DeveloperBlocker.NOT_CHARGING
            !trigger.matches(charging.type) -> DeveloperBlocker.TRIGGER_MISMATCH
            !screen.isIdle -> DeveloperBlocker.SCREEN_IN_USE
            !overlayGranted -> DeveloperBlocker.NO_OVERLAY
            else -> DeveloperBlocker.NOTHING
        }
}

/** Why the clock would not come up if a power event arrived this instant. */
enum class DeveloperBlocker { NOTHING, NOT_CHARGING, TRIGGER_MISMATCH, SCREEN_IN_USE, NO_OVERLAY }

/**
 * Backs the developer tab.
 *
 * Everything here exists because the interesting failures happen with the app
 * closed and the screen off, where nothing at all is observable from outside.
 */
@HiltViewModel
class DeveloperViewModel @Inject constructor(
    settings: SettingsRepository,
    chargingMonitor: ChargingStatusMonitor,
    screenMonitor: ScreenStateMonitor,
    private val ambientLight: AmbientLightMonitor,
    widgetStore: HostedWidgetStore,
    private val attemptLog: StandbyAttemptLog,
    private val crashRecorder: CrashRecorder,
    private val sessionLog: StandbySessionLog,
    private val requirements: SystemRequirements,
    appVersion: AppVersion,
    private val standbyLauncher: StandbyLauncher,
) : ViewModel() {

    /** None of the permission states emit a change; re-read on return. */
    private val refreshes = MutableStateFlow(0)

    private val live = combine(
        chargingMonitor.status,
        screenMonitor.state,
        ambientLight.lux,
        ambientLight.isDark,
    ) { charging, screen, lux, dark -> LiveReadings(charging, screen, lux, dark) }

    private val stored = combine(
        settings.settings,
        widgetStore.widgets,
        attemptLog.history,
        sessionLog.sessions,
    ) { prefs, widgets, history, sessions ->
        StoredReadings(prefs.chargingTrigger, prefs.persistence, widgets, history, sessions)
    }

    val uiState: StateFlow<DeveloperUiState> =
        combine(live, stored, refreshes) { now, saved, _ ->
            DeveloperUiState(
                charging = now.charging,
                screen = now.screen,
                lux = now.lux,
                lightSensorAvailable = ambientLight.isAvailable,
                isDark = now.dark,
                trigger = saved.trigger,
                persistence = saved.persistence,
                widgets = saved.widgets,
                history = saved.history,
                sessions = saved.sessions,
                appVersion = appVersion.display,
                overlayGranted = requirements.canDrawOverlays(),
                batteryUnrestricted = requirements.isIgnoringBatteryOptimizations(),
                notificationsGranted = requirements.hasNotificationPermission(),
                // A file read, so off the main thread even though it is tiny.
                lastCrash = withContext(Dispatchers.IO) { crashRecorder.lastCrash() },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            // The monitored flows are debounced, so they stay quiet for a
            // moment after subscribing. Seed with direct reads so the screen
            // is never blank on open.
            initialValue = DeveloperUiState(
                charging = chargingMonitor.currentStatus(),
                screen = screenMonitor.current(),
                lightSensorAvailable = ambientLight.isAvailable,
                appVersion = appVersion.display,
            ),
        )

    fun refresh() {
        refreshes.value += 1
    }

    /** Opens the clock straight away, without waiting for a charger. */
    fun launchStandby() = standbyLauncher.launch()

    fun clearCrash() {
        crashRecorder.clear()
        refresh()
    }

    fun clearSessions() {
        viewModelScope.launch { sessionLog.clear() }
    }

    fun clearHistory() {
        viewModelScope.launch { attemptLog.clearHistory() }
    }

    /**
     * The whole diagnostic picture as plain text, for pasting into a report.
     *
     * Written in English whatever the app's language: this is pasted into a
     * bug report, not read in the UI.
     *
     * Carries no identifiers on purpose: this gets shared, and the useful part
     * is the configuration and the outcomes, not whose phone it is.
     */
    fun diagnosticsText(): String {
        val s = uiState.value
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val unknown = "?"
        return buildString {
            appendLine("Nightstand " + s.appVersion)
            appendLine("generated " + stamp.format(Date()))
            appendLine()
            appendLine("[settings]")
            appendLine("trigger: " + s.trigger)
            appendLine("persistence: " + s.persistence)
            appendLine()
            appendLine("[now]")
            appendLine("charging: " + s.charging.type + " / " + (s.charging.levelPercent ?: unknown) + "%")
            appendLine("screen: interactive=" + s.screen.isInteractive + " locked=" + s.screen.isLocked)
            appendLine("light: " + (s.lux?.toString() ?: "no sensor") + " (dark=" + s.isDark + ")")
            appendLine("blocked by: " + s.blockedBy)
            appendLine()
            appendLine("[permissions]")
            appendLine("overlay: " + s.overlayGranted)
            appendLine("battery unrestricted: " + s.batteryUnrestricted)
            appendLine("notifications: " + s.notificationsGranted)
            appendLine()
            appendLine("[widgets: " + s.widgets.size + "]")
            s.widgets.forEach { appendLine(it.appWidgetId.toString() + " " + it.providerFlattened) }
            appendLine()
            s.lastCrash?.let {
                appendLine("[last crash]")
                appendLine(it)
                appendLine()
            }
            appendLine("[recent attempts: " + s.history.size + "]")
            s.history.forEach {
                appendLine(
                    stamp.format(Date(it.atEpochMillis)) + " " + it.outcome +
                        " detected=" + it.detectedType + " rule=" + it.trigger,
                )
            }
        }

    }

    private data class LiveReadings(
        val charging: ChargingStatus,
        val screen: ScreenState,
        val lux: Float?,
        val dark: Boolean,
    )

    private data class StoredReadings(
        val trigger: ChargingTrigger,
        val persistence: StandbyPersistence,
        val widgets: List<HostedWidget>,
        val history: List<StandbyAttempt>,
        val sessions: List<StandbySession>,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
