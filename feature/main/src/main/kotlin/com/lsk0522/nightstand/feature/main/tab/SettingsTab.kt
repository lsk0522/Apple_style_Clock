package com.lsk0522.nightstand.feature.main.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SwitchRow
import com.lsk0522.nightstand.core.design.component.listSection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.lsk0522.nightstand.core.data.system.SystemRequirementId
import com.lsk0522.nightstand.feature.main.AppSettingsViewModel
import com.lsk0522.nightstand.feature.main.setup.SetupViewModel
import com.lsk0522.nightstand.feature.main.R

/** The middle tab: the app's own settings. */
@Composable
fun SettingsTab(
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel = hiltViewModel(),
    setupViewModel: SetupViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val setupState by setupViewModel.uiState.collectAsStateWithLifecycle()

    // These live in system settings and change behind the app's back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { setupViewModel.refresh() }

    IosScreen(
        title = stringResource(R.string.settings_title),
        modifier = modifier,
    ) {
        listSection(
            key = "clock",
            header = R.string.settings_clock_header,
            footer = R.string.settings_clock_footer,
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
                checked = settings.use24Hour,
                onCheckedChange = viewModel::setUse24Hour,
            )
            SwitchRow(
                title = stringResource(R.string.settings_clock_seconds),
                checked = settings.showSeconds,
                onCheckedChange = viewModel::setShowSeconds,
                showSeparator = false,
            )
        }

        listSection(
            key = "display",
            header = R.string.settings_display_header,
        ) {
            SwitchRow(
                title = stringResource(R.string.settings_display_night),
                subtitle = stringResource(R.string.settings_display_night_sub),
                checked = settings.nightMode,
                onCheckedChange = viewModel::setNightMode,
            )
            SwitchRow(
                title = stringResource(R.string.settings_display_burnin),
                subtitle = stringResource(R.string.settings_display_burnin_sub),
                checked = settings.burnInProtection,
                onCheckedChange = viewModel::setBurnInProtection,
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
            header = R.string.settings_permission_header,
            footer = R.string.settings_permission_footer,
        ) {
            ListRow(
                title = stringResource(R.string.settings_permission_overlay),
                value = stringResource(
                    if (setupState.isSatisfied(SystemRequirementId.OVERLAY)) {
                        R.string.settings_permission_on
                    } else {
                        R.string.settings_permission_needed
                    },
                ),
            )
            ListRow(
                title = stringResource(R.string.settings_permission_battery),
                value = stringResource(
                    if (setupState.isSatisfied(SystemRequirementId.BATTERY_OPTIMIZATION)) {
                        R.string.settings_permission_on
                    } else {
                        R.string.settings_permission_needed
                    },
                ),
            )
            ListRow(
                title = stringResource(R.string.settings_permission_reopen),
                showChevron = true,
                showSeparator = false,
                onClick = setupViewModel::reopenSetup,
            )
        }

        listSection(
            key = "about",
            header = R.string.settings_about_header,
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
