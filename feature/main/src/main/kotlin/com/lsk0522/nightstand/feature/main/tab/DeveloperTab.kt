package com.lsk0522.nightstand.feature.main.tab

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
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
