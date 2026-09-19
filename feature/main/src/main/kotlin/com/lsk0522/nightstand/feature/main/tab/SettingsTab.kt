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
import com.lsk0522.nightstand.core.design.component.SwitchRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.feature.main.R

/** The middle tab: the app's own settings. */
@Composable
fun SettingsTab(modifier: Modifier = Modifier) {
    // TODO(next): Phase 2 — back these with DataStore in :core:data instead of
    // in-memory state, so they survive process death.
    var use24Hour by rememberSaveable { mutableStateOf(true) }
    var showSeconds by rememberSaveable { mutableStateOf(false) }
    var nightMode by rememberSaveable { mutableStateOf(true) }
    var burnInProtection by rememberSaveable { mutableStateOf(true) }

    IosScreen(
        title = stringResource(R.string.settings_title),
        modifier = modifier,
    ) {
        listSection(
            key = "clock",
            header = stringResource(R.string.settings_clock_header),
            footer = stringResource(R.string.settings_clock_footer),
        ) {
            ListRow(
                title = stringResource(R.string.settings_clock_face),
                value = stringResource(R.string.settings_clock_face_value),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            SwitchRow(
                title = stringResource(R.string.settings_clock_24h),
                checked = use24Hour,
                onCheckedChange = { use24Hour = it },
            )
            SwitchRow(
                title = stringResource(R.string.settings_clock_seconds),
                checked = showSeconds,
                onCheckedChange = { showSeconds = it },
                showSeparator = false,
            )
        }

        listSection(
            key = "display",
            header = stringResource(R.string.settings_display_header),
        ) {
            SwitchRow(
                title = stringResource(R.string.settings_display_night),
                subtitle = stringResource(R.string.settings_display_night_sub),
                checked = nightMode,
                onCheckedChange = { nightMode = it },
            )
            SwitchRow(
                title = stringResource(R.string.settings_display_burnin),
                subtitle = stringResource(R.string.settings_display_burnin_sub),
                checked = burnInProtection,
                onCheckedChange = { burnInProtection = it },
            )
            ListRow(
                title = stringResource(R.string.settings_display_brightness),
                value = stringResource(R.string.settings_display_brightness_value),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }

        listSection(
            key = "permissions",
            header = stringResource(R.string.settings_permission_header),
            footer = stringResource(R.string.settings_permission_footer),
        ) {
            // TODO(next): Phase 3 — read the real grant state and deep-link to
            // the system screens instead of showing a static value.
            ListRow(
                title = stringResource(R.string.settings_permission_overlay),
                value = stringResource(R.string.settings_permission_pending),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.settings_permission_battery),
                value = stringResource(R.string.settings_permission_pending),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }

        listSection(
            key = "about",
            header = stringResource(R.string.settings_about_header),
        ) {
            ListRow(
                title = stringResource(R.string.settings_about_version),
                value = APP_VERSION,
            )
            ListRow(
                title = stringResource(R.string.settings_about_licenses),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.settings_about_github),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }
    }
}

// TODO(next): Phase 11 — surface the real versionName from the app module
// rather than repeating it here.
private const val APP_VERSION = "0.1.0"
