package com.lsk0522.nightstand.feature.main.tab

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.feature.main.R

@Composable
fun DonateTab(modifier: Modifier = Modifier) {
    val palette = NightstandTheme.palette

    IosScreen(
        title = stringResource(R.string.donate_title),
        modifier = modifier,
    ) {
        listSection(key = "intro") {
            Text(
                text = stringResource(R.string.donate_intro),
                style = NightstandType.Body,
                color = palette.label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }

        // TODO(next): Phase 9 — replace the static prices with the real
        // Play Billing product list; the amounts here are placeholders.
        listSection(
            key = "items",
            header = stringResource(R.string.donate_items_header),
            footer = stringResource(R.string.donate_footer),
        ) {
            ListRow(
                title = stringResource(R.string.donate_coffee),
                value = stringResource(R.string.donate_preparing),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.donate_meal),
                value = stringResource(R.string.donate_preparing),
                showChevron = true,
                enabled = false,
                onClick = null,
            )
            ListRow(
                title = stringResource(R.string.donate_generous),
                value = stringResource(R.string.donate_preparing),
                showChevron = true,
                enabled = false,
                showSeparator = false,
                onClick = null,
            )
        }
    }
}
