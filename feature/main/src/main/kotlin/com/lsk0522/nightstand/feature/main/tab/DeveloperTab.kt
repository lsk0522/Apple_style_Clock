package com.lsk0522.nightstand.feature.main.tab

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.data.standby.StandbyAttemptOutcome
import com.lsk0522.nightstand.core.data.system.SystemRequirementId
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.feature.main.R
import com.lsk0522.nightstand.feature.main.setup.SetupViewModel

@Composable
fun DeveloperTab(
    modifier: Modifier = Modifier,
    setupViewModel: SetupViewModel = hiltViewModel(),
) {
    val state by setupViewModel.uiState.collectAsStateWithLifecycle()

    IosScreen(
        title = stringResource(R.string.developer_title),
        modifier = modifier,
    ) {
        // TODO(next): Phase 8 — wire these to the real diagnostics.
        listSection(
            key = "tools",
            header = R.string.developer_tools_header,
            footer = R.string.developer_footer,
        ) {
            ListRow(
                title = stringResource(R.string.developer_force_standby),
                subtitle = stringResource(R.string.developer_force_standby_why),
                showChevron = true,
                onClick = setupViewModel::launchStandby,
            )
            ListRow(
                title = stringResource(R.string.developer_simulate),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.developer_logs),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.developer_sensors),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }

        listSection(
            key = "attempt",
            header = R.string.developer_attempt_header,
            footer = R.string.developer_attempt_footer,
        ) {
            val attempt = state.lastAttempt
            ListRow(
                title = stringResource(R.string.developer_attempt_result),
                value = if (attempt == null) {
                    stringResource(R.string.developer_attempt_none)
                } else {
                    stringResource(attempt.outcome.labelRes())
                },
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
                value = attempt?.let {
                    DateUtils.getRelativeTimeSpanString(
                        it.atEpochMillis,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString()
                } ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.settings_permission_overlay),
                value = stringResource(
                    if (state.isSatisfied(SystemRequirementId.OVERLAY)) {
                        R.string.settings_permission_on
                    } else {
                        R.string.settings_permission_needed
                    },
                ),
                showSeparator = false,
            )
        }

        listSection(
            key = "diagnostics",
            header = R.string.developer_diagnostics_header,
        ) {
            ListRow(
                title = stringResource(R.string.developer_widget_host),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.developer_export),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }

        listSection(
            key = "build",
            header = R.string.developer_build_header,
        ) {
            ListRow(
                title = stringResource(R.string.developer_version),
                value = state.appVersion,
            )
            ListRow(
                title = stringResource(R.string.developer_build_type),
                value = "debug",
            )
            ListRow(
                title = stringResource(R.string.developer_min_sdk),
                value = "Android 10 (API 29)",
                showSeparator = false,
            )
        }
    }
}

private fun StandbyAttemptOutcome.labelRes(): Int = when (this) {
    StandbyAttemptOutcome.LAUNCHED -> R.string.attempt_launched
    StandbyAttemptOutcome.TRIGGER_MISMATCH -> R.string.attempt_trigger_mismatch
    StandbyAttemptOutcome.NO_OVERLAY_PERMISSION -> R.string.attempt_no_overlay
    StandbyAttemptOutcome.LAUNCH_FAILED -> R.string.attempt_launch_failed
    StandbyAttemptOutcome.CANCELLED_EARLY -> R.string.attempt_cancelled_early
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
