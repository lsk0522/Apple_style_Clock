package com.lsk0522.nightstand.feature.main.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.lsk0522.nightstand.core.common.model.ClockFace
import com.lsk0522.nightstand.core.design.component.IosAlert
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SelectionRow
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

    // Only asked when turning it on; switching it back off costs nothing.
    var confirmingSeconds by remember { mutableStateOf(false) }

    // These live in system settings and change behind the app's back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { setupViewModel.refresh() }

    if (confirmingSeconds) {
        IosAlert(
            title = stringResource(R.string.seconds_alert_title),
            message = stringResource(R.string.seconds_alert_message),
            confirmLabel = stringResource(R.string.seconds_alert_confirm),
            cancelLabel = stringResource(R.string.common_cancel),
            onConfirm = {
                viewModel.setShowSeconds(true)
                confirmingSeconds = false
            },
            onDismiss = { confirmingSeconds = false },
        )
    }

    IosScreen(
        title = stringResource(R.string.settings_title),
        modifier = modifier,
    ) {
        listSection(
            key = "face",
            header = R.string.settings_face_header,
            footer = R.string.settings_face_footer,
        ) {
            ClockFace.entries.forEachIndexed { index, face ->
                SelectionRow(
                    title = stringResource(face.labelRes()),
                    subtitle = stringResource(face.captionRes()),
                    selected = settings.clockFace == face,
                    onSelect = { viewModel.setClockFace(face) },
                    showSeparator = index != ClockFace.entries.lastIndex,
                )
            }
        }

        listSection(
            key = "clock",
            header = R.string.settings_clock_header,
        ) {
            SwitchRow(
                title = stringResource(R.string.settings_clock_24h),
                checked = settings.use24Hour,
                onCheckedChange = viewModel::setUse24Hour,
            )
            SwitchRow(
                title = stringResource(R.string.settings_clock_seconds),
                subtitle = stringResource(R.string.settings_clock_seconds_why),
                checked = settings.showSeconds,
                onCheckedChange = { wanted ->
                    if (wanted) confirmingSeconds = true else viewModel.setShowSeconds(false)
                },
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
                value = setupState.appVersion,
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

private fun ClockFace.labelRes(): Int = when (this) {
    ClockFace.DIGITAL -> R.string.face_digital
    ClockFace.ANALOG -> R.string.face_analog
    ClockFace.WORLD -> R.string.face_world
    ClockFace.SOLAR -> R.string.face_solar
    ClockFace.FLOAT -> R.string.face_float
    ClockFace.MINIMAL_MONO -> R.string.face_minimal_mono
}

private fun ClockFace.captionRes(): Int = when (this) {
    ClockFace.DIGITAL -> R.string.face_digital_why
    ClockFace.ANALOG -> R.string.face_analog_why
    ClockFace.WORLD -> R.string.face_world_why
    ClockFace.SOLAR -> R.string.face_solar_why
    ClockFace.FLOAT -> R.string.face_float_why
    ClockFace.MINIMAL_MONO -> R.string.face_minimal_mono_why
}
