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
import com.lsk0522.nightstand.feature.main.AppSettingsViewModel
import com.lsk0522.nightstand.feature.main.R

@Composable
fun WidgetsTab(
    modifier: Modifier = Modifier,
    viewModel: AppSettingsViewModel = hiltViewModel(),
) {
    // TODO(next): Phase 5 — replace the empty state with the real AppWidgetHost
    // list and the system widget picker.
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    IosScreen(
        title = stringResource(R.string.widgets_title),
        modifier = modifier,
    ) {
        listSection(
            key = "added",
            header = R.string.widgets_added_header,
            footer = R.string.widgets_add_footer,
        ) {
            ListRow(
                title = stringResource(R.string.widgets_empty),
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.widgets_add),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }

        listSection(
            key = "rotate",
            header = R.string.widgets_rotate_header,
            footer = R.string.widgets_rotate_footer,
        ) {
            SwitchRow(
                title = stringResource(R.string.widgets_rotate_enabled),
                checked = settings.autoRotateWidgets,
                onCheckedChange = viewModel::setAutoRotateWidgets,
            )
            ListRow(
                title = stringResource(R.string.widgets_rotate_interval),
                value = stringResource(R.string.widgets_rotate_interval_value),
                showChevron = true,
                enabled = settings.autoRotateWidgets,
                showSeparator = false,
                onClick = null,
            )
        }
    }
}
