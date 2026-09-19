package com.lsk0522.nightstand.feature.main.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SelectionRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.feature.main.R

/** Which kind of charging should wake the clock. */
private enum class ChargingTrigger { Wireless, Wired, Both, Dock }

@Composable
fun ChargingTab(modifier: Modifier = Modifier) {
    // TODO(next): Phase 2 — persist through :core:data and drive the real
    // ChargeType detector.
    var trigger by rememberSaveable { mutableStateOf(ChargingTrigger.Wireless) }

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
                selected = trigger == ChargingTrigger.Wireless,
                onSelect = { trigger = ChargingTrigger.Wireless },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_wired),
                selected = trigger == ChargingTrigger.Wired,
                onSelect = { trigger = ChargingTrigger.Wired },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_both),
                selected = trigger == ChargingTrigger.Both,
                onSelect = { trigger = ChargingTrigger.Both },
            )
            SelectionRow(
                title = stringResource(R.string.charging_mode_dock),
                selected = trigger == ChargingTrigger.Dock,
                onSelect = { trigger = ChargingTrigger.Dock },
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
                value = stringResource(R.string.charging_status_pending),
            )
            ListRow(
                title = stringResource(R.string.charging_status_battery),
                value = stringResource(R.string.charging_status_unknown),
                showSeparator = false,
            )
        }
    }
}
