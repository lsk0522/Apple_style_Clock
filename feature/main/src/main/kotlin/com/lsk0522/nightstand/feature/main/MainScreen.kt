package com.lsk0522.nightstand.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.lsk0522.nightstand.core.design.component.NightstandTabBar
import com.lsk0522.nightstand.core.design.component.TabItem
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.feature.main.tab.ChargingTab
import com.lsk0522.nightstand.feature.main.tab.DeveloperTab
import com.lsk0522.nightstand.feature.main.tab.DonateTab
import com.lsk0522.nightstand.feature.main.tab.SettingsTab
import com.lsk0522.nightstand.feature.main.tab.WidgetsTab
import com.lsk0522.nightstand.core.design.R as DesignR

/**
 * The app shell: five sections over a grouped-list ground.
 *
 * Tab order is fixed by product: widgets, charging, main, developer, donate.
 *
 * TODO(next): each pane currently lives here. As the sibling feature modules
 * get built, the app module will supply the real screens through slots so
 * `:feature:main` never has to depend on them.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val palette = NightstandTheme.palette
    var selectedIndex by rememberSaveable { mutableIntStateOf(MAIN_TAB_INDEX) }

    val tabs = listOf(
        TabItem(stringResource(R.string.tab_widgets), DesignR.drawable.ic_tab_widgets),
        TabItem(stringResource(R.string.tab_charging), DesignR.drawable.ic_tab_charging),
        TabItem(stringResource(R.string.tab_main), DesignR.drawable.ic_tab_main),
        TabItem(stringResource(R.string.tab_developer), DesignR.drawable.ic_tab_developer),
        TabItem(stringResource(R.string.tab_donate), DesignR.drawable.ic_tab_donate),
    )

    // A Box, not a Column: since iOS 26 the tab bar floats *over* the content
    // rather than sitting below it, and the list scrolls underneath.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.groupedBackground),
    ) {
        when (selectedIndex) {
            0 -> WidgetsTab()
            1 -> ChargingTab()
            2 -> SettingsTab()
            3 -> DeveloperTab()
            else -> DonateTab()
        }

        NightstandTabBar(
            items = tabs,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** Tabs are zero-indexed; the app opens on the middle one. */
private const val MAIN_TAB_INDEX = 2

@Preview(showBackground = true, widthDp = 400, heightDp = 860)
@Composable
private fun MainScreenLightPreview() {
    NightstandTheme(darkTheme = false) { MainScreen() }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 400, heightDp = 860)
@Composable
private fun MainScreenDarkPreview() {
    NightstandTheme(darkTheme = true) { MainScreen() }
}
