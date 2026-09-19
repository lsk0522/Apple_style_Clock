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

@Composable
fun WidgetsTab(modifier: Modifier = Modifier) {
    // TODO(next): Phase 5 — replace with the real AppWidgetHost list and the
    // system widget picker.
    var autoRotate by rememberSaveable { mutableStateOf(true) }

    IosScreen(
        title = stringResource(R.string.widgets_title),
        modifier = modifier,
    ) {
        listSection(
            key = "added",
            header = stringResource(R.string.widgets_added_header),
            footer = stringResource(R.string.widgets_add_footer),
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
            header = stringResource(R.string.widgets_rotate_header),
            footer = stringResource(R.string.widgets_rotate_footer),
        ) {
            SwitchRow(
                title = stringResource(R.string.widgets_rotate_enabled),
                checked = autoRotate,
                onCheckedChange = { autoRotate = it },
            )
            ListRow(
                title = stringResource(R.string.widgets_rotate_interval),
                value = stringResource(R.string.widgets_rotate_interval_value),
                showChevron = true,
                enabled = autoRotate,
                showSeparator = false,
                onClick = null,
            )
        }
    }
}
