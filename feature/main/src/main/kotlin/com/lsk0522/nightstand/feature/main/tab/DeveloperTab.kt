package com.lsk0522.nightstand.feature.main.tab

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptOutcome
import com.lsk0522.nightstand.core.design.component.IosButton
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.feature.main.DeveloperBlocker
import com.lsk0522.nightstand.feature.main.DeveloperHostHolder
import com.lsk0522.nightstand.feature.main.DeveloperUiState
import com.lsk0522.nightstand.feature.main.DeveloperViewModel
import com.lsk0522.nightstand.feature.main.R
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost

/** Which of the tab's pages is showing. */
private enum class DevPage { Root, Simulate, Logs, Sensors, WidgetHost, Crash, Battery }

/**
 * Diagnostics for the parts of the app that run with nothing on screen.
 *
 * There is no navigation graph in the app yet, so the pages are swapped in
 * place — the same thing the widget picker does. A back row stands in for the
 * navigation bar's chevron.
 */
@Composable
fun DeveloperTab(
    modifier: Modifier = Modifier,
    viewModel: DeveloperViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var page by remember { mutableStateOf(DevPage.Root) }

    // Permission states change behind the app's back, in system settings.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    when (page) {
        DevPage.Root -> RootPage(
            state = state,
            viewModel = viewModel,
            onOpen = { page = it },
            modifier = modifier,
        )

        DevPage.Simulate -> SimulatePage(state, onBack = { page = DevPage.Root }, modifier)
        DevPage.Logs -> LogsPage(state, viewModel, onBack = { page = DevPage.Root }, modifier)
        DevPage.Sensors -> SensorsPage(state, onBack = { page = DevPage.Root }, modifier)
        DevPage.WidgetHost -> WidgetHostPage(state, onBack = { page = DevPage.Root }, modifier)
        DevPage.Crash -> CrashPage(state, viewModel, onBack = { page = DevPage.Root }, modifier)
        DevPage.Battery -> BatteryPage(state, viewModel, onBack = { page = DevPage.Root }, modifier)
    }
}

@Composable
private fun RootPage(
    state: DeveloperUiState,
    viewModel: DeveloperViewModel,
    onOpen: (DevPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    IosScreen(title = stringResource(R.string.developer_title), modifier = modifier) {
        listSection(
            key = "tools",
            header = R.string.developer_tools_header,
            footer = R.string.developer_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_force_standby),
                subtitle = stringResource(R.string.developer_force_standby_why),
                showChevron = true,
                onClick = viewModel::launchStandby,
            )
            ListRow(
                title = stringResource(R.string.developer_simulate),
                subtitle = stringResource(R.string.developer_simulate_why),
                showChevron = true,
                onClick = { onOpen(DevPage.Simulate) },
            )
            ListRow(
                title = stringResource(R.string.developer_logs),
                value = state.history.size.toString(),
                showChevron = true,
                onClick = { onOpen(DevPage.Logs) },
            )
            ListRow(
                title = stringResource(R.string.developer_sensors),
                showChevron = true,
                showSeparator = false,
                onClick = { onOpen(DevPage.Sensors) },
            )
        }

        // The single most useful line in the tab: of the four conditions the
        // service checks in order, which one is currently false.
        listSection(
            key = "verdict",
            header = R.string.developer_verdict_header,
            footer = R.string.developer_verdict_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_verdict_now),
                value = stringResource(state.blockedBy.labelRes()),
                showSeparator = false,
            )
        }

        listSection(
            key = "attempt",
            header = R.string.developer_attempt_header,
            footer = R.string.developer_attempt_footer,
        ) {
            val attempt = state.history.firstOrNull()
            ListRow(
                title = stringResource(R.string.developer_attempt_result),
                value = attempt?.let { stringResource(it.outcome.labelRes()) }
                    ?: stringResource(R.string.developer_attempt_none),
            )
            ListRow(
                title = stringResource(R.string.developer_attempt_detected),
                value = attempt?.let { stringResource(it.detectedType.labelRes()) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.developer_attempt_trigger),
                value = attempt?.let { stringResource(it.trigger.labelRes()) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.developer_attempt_when),
                value = attempt?.let { relativeTime(it.atEpochMillis) }
                    ?: stringResource(R.string.charging_status_unknown),
                showSeparator = false,
            )
        }

        listSection(
            key = "permissions",
            header = R.string.settings_permission_header,
        ) {
            PermissionRow(R.string.settings_permission_overlay, state.overlayGranted)
            PermissionRow(R.string.settings_permission_battery, state.batteryUnrestricted)
            PermissionRow(
                label = R.string.setup_notifications,
                granted = state.notificationsGranted,
                showSeparator = false,
            )
        }

        listSection(
            key = "diagnostics",
            header = R.string.developer_diagnostics_header,
            footer = R.string.developer_export_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_battery),
                value = state.averageDrainPerHour
                    ?.let { stringResource(R.string.developer_battery_rate, it) }
                    ?: stringResource(R.string.developer_battery_unmeasured),
                showChevron = true,
                onClick = { onOpen(DevPage.Battery) },
            )
            ListRow(
                title = stringResource(R.string.developer_crash),
                value = stringResource(
                    if (state.lastCrash == null) {
                        R.string.developer_crash_none
                    } else {
                        R.string.developer_crash_present
                    },
                ),
                showChevron = state.lastCrash != null,
                enabled = state.lastCrash != null,
                onClick = if (state.lastCrash == null) null else { { onOpen(DevPage.Crash) } },
            )
            ListRow(
                title = stringResource(R.string.developer_widget_host),
                value = state.widgets.size.toString(),
                showChevron = true,
                onClick = { onOpen(DevPage.WidgetHost) },
            )
            ListRow(
                title = stringResource(R.string.developer_export),
                showChevron = true,
                showSeparator = false,
                onClick = {
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, viewModel.diagnosticsText())
                    }
                    runCatching {
                        context.startActivity(
                            Intent.createChooser(share, null)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
            )
        }

        listSection(key = "build", header = R.string.developer_build_header) {
            ListRow(
                title = stringResource(R.string.developer_version),
                value = state.appVersion,
            )
            ListRow(title = stringResource(R.string.developer_build_type), value = "debug")
            ListRow(
                title = stringResource(R.string.developer_min_sdk),
                value = "Android 10 (API 29)",
                showSeparator = false,
            )
        }
    }
}

/**
 * What each kind of charger would do under the rule in force.
 *
 * A dry run rather than a fake power event: the app cannot make the system
 * believe a charger was connected, and pretending otherwise would produce a
 * result that proves nothing about the real path.
 */
@Composable
private fun SimulatePage(
    state: DeveloperUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IosScreen(title = stringResource(R.string.developer_simulate), modifier = modifier) {
        backRow(onBack)

        listSection(
            key = "matrix",
            header = R.string.developer_simulate_header,
            footer = R.string.developer_simulate_footer,
        ) {
            val types = ChargeType.entries.filter { it != ChargeType.NONE }
            types.forEachIndexed { index, type ->
                ListRow(
                    title = stringResource(type.labelRes()),
                    value = stringResource(
                        if (state.wouldTrigger(type)) {
                            R.string.charging_status_would_trigger_yes
                        } else {
                            R.string.charging_status_would_trigger_no
                        },
                    ),
                    showSeparator = index != types.lastIndex,
                )
            }
        }

        listSection(key = "rule", header = R.string.developer_simulate_rule) {
            ListRow(
                title = stringResource(R.string.charging_mode_header),
                value = stringResource(state.trigger.labelRes()),
            )
            ListRow(
                title = stringResource(R.string.charging_persistence_header),
                value = stringResource(state.persistence.labelRes()),
                showSeparator = false,
            )
        }
    }
}

/** Every recorded outcome, newest first. */
@Composable
private fun LogsPage(
    state: DeveloperUiState,
    viewModel: DeveloperViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IosScreen(title = stringResource(R.string.developer_logs), modifier = modifier) {
        backRow(onBack)

        listSection(
            key = "history",
            header = R.string.developer_logs_header,
            footer = R.string.developer_logs_footer,
        ) {
            if (state.history.isEmpty()) {
                ListRow(
                    title = stringResource(R.string.developer_attempt_none),
                    enabled = false,
                    showSeparator = false,
                    onClick = null,
                )
            } else {
                state.history.forEachIndexed { index, attempt ->
                    ListRow(
                        title = stringResource(attempt.outcome.labelRes()),
                        subtitle = stringResource(
                            R.string.developer_logs_detail,
                            stringResource(attempt.detectedType.labelRes()),
                            stringResource(attempt.trigger.labelRes()),
                        ),
                        value = relativeTime(attempt.atEpochMillis),
                        showSeparator = index != state.history.lastIndex,
                    )
                }
            }
        }

        if (state.history.isNotEmpty()) {
            item(key = "clear") {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    IosButton(
                        label = stringResource(R.string.developer_logs_clear),
                        prominent = false,
                        onClick = viewModel::clearHistory,
                    )
                }
            }
        }
    }
}

/** Live readings, so a threshold can be checked against the actual room. */
@Composable
private fun SensorsPage(
    state: DeveloperUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IosScreen(title = stringResource(R.string.developer_sensors), modifier = modifier) {
        backRow(onBack)

        listSection(
            key = "light",
            header = R.string.developer_sensors_light,
            footer = R.string.developer_sensors_light_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_sensors_lux),
                value = state.lux?.let { stringResource(R.string.developer_sensors_lux_value, it) }
                    ?: stringResource(R.string.settings_display_night_no_sensor),
            )
            ListRow(
                title = stringResource(R.string.developer_sensors_dark),
                value = stringResource(state.isDark.yesNoRes()),
                showSeparator = false,
            )
        }

        listSection(key = "screen", header = R.string.developer_sensors_screen) {
            ListRow(
                title = stringResource(R.string.developer_sensors_interactive),
                value = stringResource(state.screen.isInteractive.yesNoRes()),
            )
            ListRow(
                title = stringResource(R.string.developer_sensors_locked),
                value = stringResource(state.screen.isLocked.yesNoRes()),
            )
            ListRow(
                title = stringResource(R.string.developer_sensors_idle),
                value = stringResource(state.screen.isIdle.yesNoRes()),
                showSeparator = false,
            )
        }

        listSection(key = "power", header = R.string.charging_status_header) {
            ListRow(
                title = stringResource(R.string.charging_status_method),
                value = stringResource(state.charging.type.labelRes()),
            )
            ListRow(
                title = stringResource(R.string.charging_status_battery),
                value = state.charging.levelPercent
                    ?.let { stringResource(R.string.charging_status_percent, it) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.charging_status_voltage),
                value = state.charging.voltageMilliVolts
                    ?.let { stringResource(R.string.charging_status_millivolts, it) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.charging_status_temperature),
                value = state.charging.temperatureCelsius
                    ?.let { stringResource(R.string.charging_status_celsius, it) }
                    ?: stringResource(R.string.charging_status_unknown),
                showSeparator = false,
            )
        }
    }
}

/**
 * What the widget host is actually holding.
 *
 * A widget whose provider has been uninstalled still has a stored id, and the
 * only symptom on the clock is a blank tile — this is where that becomes
 * legible.
 */
@Composable
private fun WidgetHostPage(
    state: DeveloperUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    host: StandbyWidgetHost = hiltViewModel<DeveloperHostHolder>().host,
) {
    val context = LocalContext.current

    IosScreen(title = stringResource(R.string.developer_widget_host), modifier = modifier) {
        backRow(onBack)

        listSection(
            key = "hosted",
            header = R.string.developer_host_header,
            footer = R.string.developer_host_footer,
        ) {
            if (state.widgets.isEmpty()) {
                ListRow(
                    title = stringResource(R.string.developer_host_empty),
                    enabled = false,
                    showSeparator = false,
                    onClick = null,
                )
            } else {
                state.widgets.forEachIndexed { index, widget ->
                    val info = host.providerInfo(widget.appWidgetId)
                    ListRow(
                        title = info?.loadLabel(context.packageManager)
                            ?: stringResource(R.string.developer_host_unresolved),
                        subtitle = stringResource(
                            R.string.developer_host_detail,
                            widget.appWidgetId,
                            widget.providerFlattened,
                        ),
                        showSeparator = index != state.widgets.lastIndex,
                    )
                }
            }
        }
    }
}

/**
 * What StandBy costs the battery, measured on this phone.
 *
 * The plan set a target of under 5% an hour and nothing was ever held against
 * it, because measuring needs a phone left alone with a stopwatch. The phone
 * keeps the stopwatch now; this is where the readings land.
 */
@Composable
private fun BatteryPage(
    state: DeveloperUiState,
    viewModel: DeveloperViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IosScreen(title = stringResource(R.string.developer_battery), modifier = modifier) {
        backRow(onBack)

        listSection(
            key = "average",
            header = R.string.developer_battery_average,
            footer = R.string.developer_battery_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_battery_per_hour),
                value = state.averageDrainPerHour
                    ?.let { stringResource(R.string.developer_battery_rate, it) }
                    ?: stringResource(R.string.developer_battery_unmeasured),
            )
            ListRow(
                title = stringResource(R.string.developer_battery_target),
                value = stringResource(R.string.developer_battery_target_value),
                showSeparator = false,
            )
        }

        listSection(
            key = "sessions",
            header = R.string.developer_battery_sessions,
            footer = R.string.developer_battery_sessions_footer,
        ) {
            if (state.sessions.isEmpty()) {
                ListRow(
                    title = stringResource(R.string.developer_battery_none),
                    enabled = false,
                    showSeparator = false,
                    onClick = null,
                )
            } else {
                state.sessions.forEachIndexed { index, session ->
                    ListRow(
                        title = stringResource(
                            R.string.developer_battery_span,
                            session.startPercent,
                            session.endPercent,
                        ),
                        subtitle = stringResource(
                            R.string.developer_battery_detail,
                            session.durationMillis / 60_000,
                            relativeTime(session.startedAtEpochMillis),
                        ),
                        value = when {
                            session.charging ->
                                stringResource(R.string.developer_battery_on_charger)

                            session.drainPerHour != null ->
                                stringResource(
                                    R.string.developer_battery_rate,
                                    session.drainPerHour!!,
                                )

                            else -> stringResource(R.string.developer_battery_too_short)
                        },
                        showSeparator = index != state.sessions.lastIndex,
                    )
                }
            }
        }

        if (state.sessions.isNotEmpty()) {
            item(key = "clearSessions") {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    IosButton(
                        label = stringResource(R.string.developer_battery_clear),
                        prominent = false,
                        onClick = viewModel::clearSessions,
                    )
                }
            }
        }
    }
}

/**
 * The last stack trace, in full.

 *
 * Shown as plain monospaced text rather than parsed into rows: what is useful
 * about a trace is every line of it, in order.
 */
@Composable
private fun CrashPage(
    state: DeveloperUiState,
    viewModel: DeveloperViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val palette = NightstandTheme.palette
    val trace = state.lastCrash ?: return

    IosScreen(title = stringResource(R.string.developer_crash), modifier = modifier) {
        backRow(onBack)

        listSection(key = "trace", footer = R.string.developer_crash_footer) {
            Text(
                text = trace,
                style = NightstandType.Footnote.copy(fontFamily = FontFamily.Monospace),
                color = palette.label,
                modifier = Modifier.padding(16.dp),
            )
        }

        item(key = "actions") {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IosButton(
                    label = stringResource(R.string.developer_crash_share),
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, trace)
                        }
                        runCatching {
                            context.startActivity(
                                Intent.createChooser(share, null)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                )
                IosButton(
                    label = stringResource(R.string.developer_crash_clear),
                    prominent = false,
                    onClick = {
                        viewModel.clearCrash()
                        onBack()
                    },
                )
            }
        }
    }
}

@Composable
private fun PermissionRow(
label: Int, granted: Boolean, showSeparator: Boolean = true) {
    ListRow(
        title = stringResource(label),
        value = stringResource(
            if (granted) R.string.settings_permission_on else R.string.settings_permission_needed,
        ),
        showSeparator = showSeparator,
    )
}

/** Stands in for the navigation chevron until there is a real back stack. */
private fun LazyListScope.backRow(onBack: () -> Unit) {
    item(key = "back") {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            IosButton(
                label = stringResource(R.string.developer_back),
                prominent = false,
                onClick = onBack,
            )
        }
    }
}

@Composable
private fun relativeTime(epochMillis: Long): String = DateUtils.getRelativeTimeSpanString(
    epochMillis,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
).toString()

private fun Boolean.yesNoRes(): Int =
    if (this) R.string.developer_yes else R.string.developer_no

private fun DeveloperBlocker.labelRes(): Int = when (this) {
    DeveloperBlocker.NOTHING -> R.string.developer_verdict_ready
    DeveloperBlocker.NOT_CHARGING -> R.string.developer_verdict_not_charging
    DeveloperBlocker.TRIGGER_MISMATCH -> R.string.attempt_trigger_mismatch
    DeveloperBlocker.SCREEN_IN_USE -> R.string.attempt_screen_in_use
    DeveloperBlocker.NO_OVERLAY -> R.string.attempt_no_overlay
}

private fun StandbyAttemptOutcome.labelRes(): Int = when (this) {
    StandbyAttemptOutcome.LAUNCHED -> R.string.attempt_launched
    StandbyAttemptOutcome.TRIGGER_MISMATCH -> R.string.attempt_trigger_mismatch
    StandbyAttemptOutcome.NO_OVERLAY_PERMISSION -> R.string.attempt_no_overlay
    StandbyAttemptOutcome.LAUNCH_FAILED -> R.string.attempt_launch_failed
    StandbyAttemptOutcome.CANCELLED_EARLY -> R.string.attempt_cancelled_early
    StandbyAttemptOutcome.SCREEN_IN_USE -> R.string.attempt_screen_in_use
    StandbyAttemptOutcome.DISCONNECTED -> R.string.attempt_disconnected
}

private fun ChargeType.labelRes(): Int = when (this) {
    ChargeType.NONE -> R.string.charge_type_none
    ChargeType.WIRED_AC -> R.string.charge_type_wired_ac
    ChargeType.WIRED_USB -> R.string.charge_type_wired_usb
    ChargeType.WIRELESS -> R.string.charge_type_wireless
    ChargeType.DOCK -> R.string.charge_type_dock
}

private fun ChargingTrigger.labelRes(): Int = when (this) {
    ChargingTrigger.WIRELESS_ONLY -> R.string.charging_mode_wireless
    ChargingTrigger.WIRED_ONLY -> R.string.charging_mode_wired
    ChargingTrigger.ANY -> R.string.charging_mode_both
    ChargingTrigger.DOCK_ONLY -> R.string.charging_mode_dock
}

private fun StandbyPersistence.labelRes(): Int = when (this) {
    StandbyPersistence.ONCE_PER_CHARGE -> R.string.charging_persistence_once
    StandbyPersistence.WHILE_CHARGING -> R.string.charging_persistence_always
}
