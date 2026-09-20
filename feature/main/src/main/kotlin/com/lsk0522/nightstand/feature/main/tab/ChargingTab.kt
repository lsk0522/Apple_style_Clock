package com.lsk0522.nightstand.feature.main.tab

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SelectionRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.feature.main.ChargingViewModel
import com.lsk0522.nightstand.feature.main.R

@Composable
fun ChargingTab(
    modifier: Modifier = Modifier,
    viewModel: ChargingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    IosScreen(
        title = stringResource(R.string.charging_title),
        modifier = modifier,
    ) {
        listSection(
            key = "mode",
            header = R.string.charging_mode_header,
            footer = R.string.charging_mode_footer,
        ) {
            SelectionRow(
                title = stringResource(R.string.charging_mode_wireless),
                selected = state.trigger == ChargingTrigger.WIRELESS_ONLY,
                onSelect = { viewModel.setTrigger(ChargingTrigger.WIRELESS_ONLY) },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_wired),
                selected = state.trigger == ChargingTrigger.WIRED_ONLY,
                onSelect = { viewModel.setTrigger(ChargingTrigger.WIRED_ONLY) },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_both),
                selected = state.trigger == ChargingTrigger.ANY,
                onSelect = { viewModel.setTrigger(ChargingTrigger.ANY) },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_dock),
                selected = state.trigger == ChargingTrigger.DOCK_ONLY,
                onSelect = { viewModel.setTrigger(ChargingTrigger.DOCK_ONLY) },
                showSeparator = false,
            )
        }

        listSection(
            key = "persistence",
            header = R.string.charging_persistence_header,
            footer = R.string.charging_persistence_footer,
        ) {
            SelectionRow(
                title = stringResource(R.string.charging_persistence_once),
                subtitle = stringResource(R.string.charging_persistence_once_why),
                selected = state.persistence == StandbyPersistence.ONCE_PER_CHARGE,
                onSelect = { viewModel.setPersistence(StandbyPersistence.ONCE_PER_CHARGE) },
            )
            SelectionRow(
                title = stringResource(R.string.charging_persistence_always),
                subtitle = stringResource(R.string.charging_persistence_always_why),
                selected = state.persistence == StandbyPersistence.WHILE_CHARGING,
                onSelect = { viewModel.setPersistence(StandbyPersistence.WHILE_CHARGING) },
                showSeparator = false,
            )
        }

        listSection(
            key = "status",
            header = R.string.charging_status_header,
            footer = R.string.charging_status_footer,
        ) {
            ListRow(
                title = stringResource(R.string.charging_status_method),
                value = stringResource(state.status.type.labelRes()),
            )
            ListRow(
                title = stringResource(R.string.charging_status_would_trigger),
                value = stringResource(
                    if (state.wouldTrigger) {
                        R.string.charging_status_would_trigger_yes
                    } else {
                        R.string.charging_status_would_trigger_no
                    },
                ),
            )
            ListRow(
                title = stringResource(R.string.charging_status_battery),
                value = state.status.levelPercent
                    ?.let { stringResource(R.string.charging_status_percent, it) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.charging_status_voltage),
                value = state.status.voltageMilliVolts
                    ?.let { stringResource(R.string.charging_status_millivolts, it) }
                    ?: stringResource(R.string.charging_status_unknown),
            )
            ListRow(
                title = stringResource(R.string.charging_status_temperature),
                value = state.status.temperatureCelsius
                    ?.let { stringResource(R.string.charging_status_celsius, it) }
                    ?: stringResource(R.string.charging_status_unknown),
                showSeparator = false,
            )
        }

        listSection(
            key = "lastEvent",
            header = R.string.charging_event_header,
            footer = R.string.charging_event_footer,
        ) {
            val event = state.lastEvent
            ListRow(
                title = stringResource(R.string.charging_event_last),
                value = if (event == null) {
                    stringResource(R.string.charging_event_none)
                } else {
                    stringResource(
                        R.string.charging_event_value,
                        stringResource(event.type.labelRes()),
                        DateUtils.getRelativeTimeSpanString(
                            event.atEpochMillis,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS,
                        ).toString(),
                    )
                },
                showSeparator = false,
            )
        }
    }
}

private fun ChargeType.labelRes(): Int = when (this) {
    ChargeType.NONE -> R.string.charge_type_none
    ChargeType.WIRED_AC -> R.string.charge_type_wired_ac
    ChargeType.WIRED_USB -> R.string.charge_type_wired_usb
    ChargeType.WIRELESS -> R.string.charge_type_wireless
    ChargeType.DOCK -> R.string.charge_type_dock
}
