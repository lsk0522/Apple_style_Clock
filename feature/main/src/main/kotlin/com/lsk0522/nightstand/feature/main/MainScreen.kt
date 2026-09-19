package com.lsk0522.nightstand.feature.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lsk0522.nightstand.core.design.component.NightstandTabBar
import com.lsk0522.nightstand.core.design.component.TabItem
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.Spacing
import com.lsk0522.nightstand.core.design.R as DesignR

/**
 * The app shell: a five-section bottom bar over a full-bleed black canvas.
 *
 * The tab order is fixed by product: widgets, charging, main, developer, donate.
 *
 * TODO(next): each pane is a placeholder. As the feature modules get built, the
 * app module will supply the real screens through slots so that `:feature:main`
 * never has to depend on its siblings.
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

    val placeholders = remember {
        listOf(
            R.string.placeholder_widgets,
            R.string.placeholder_charging,
            R.string.placeholder_main,
            R.string.placeholder_developer,
            R.string.placeholder_donate,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas),
    ) {
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = { fadeIn(NightstandMotionFade) togetherWith fadeOut(NightstandMotionFade) },
            label = "tabContent",
            modifier = Modifier.weight(1f),
        ) { index ->
            PlaceholderPane(
                title = tabs[index].label,
                body = stringResource(placeholders[index]),
            )
        }

        NightstandTabBar(
            items = tabs,
            selectedIndex = selectedIndex,
            onSelect = { selectedIndex = it },
        )
    }
}

@Composable
private fun PlaceholderPane(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = title,
                style = NightstandType.DisplayTitle,
                color = palette.textPrimary,
            )
            Text(
                text = body,
                style = NightstandType.Body,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Tabs are zero-indexed; the app opens on the middle one. */
private const val MAIN_TAB_INDEX = 2

private val NightstandMotionFade =
    androidx.compose.animation.core.tween<Float>(durationMillis = 180)

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 400, heightDp = 860)
@Composable
private fun MainScreenPreview() {
    NightstandTheme { MainScreen() }
}
