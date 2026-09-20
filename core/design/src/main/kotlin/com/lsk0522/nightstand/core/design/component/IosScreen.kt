package com.lsk0522.nightstand.core.design.component

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType

/**
 * A screen with an iOS navigation bar: the title starts large and
 * left-aligned, then collapses into a small centred title with a hairline
 * once the list scrolls under it.
 *
 * That collapse is doing a lot of the work — a static header reads as a
 * generic mobile app no matter how the type is set.
 */
@Composable
fun IosScreen(
    title: String,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    bottomClearance: Dp = FLOATING_BAR_CLEARANCE,
    content: LazyListScope.() -> Unit,
) {
    val palette = NightstandTheme.palette
    val density = LocalDensity.current

    // The list is what the navigation bar blurs. It has its own state rather
    // than the tab bar's, because the bar is drawn inside this screen -- if it
    // shared a source that contained itself the effect would feed on itself.
    val navHaze = remember { HazeState() }
    val collapseAfter = remember(density) { with(density) { 36.dp.toPx() } }

    val collapsed by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > collapseAfter
        }
    }
    val barAlpha by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "navBarAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.groupedBackground),
    ) {
        LazyColumn(
            modifier = Modifier.hazeSource(navHaze),
            state = listState,
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + NAV_BAR_HEIGHT,
                // The tab bar floats over this list, so the last row needs room
                // to clear it.
                bottom = 24.dp + bottomClearance,
            ),
        ) {
            item(key = "largeTitle") {
                Text(
                    text = title,
                    style = NightstandType.LargeTitle,
                    color = palette.label,
                    modifier = Modifier.padding(
                        start = SECTION_INSET,
                        end = SECTION_INSET,
                        bottom = 18.dp,
                    ),
                )
            }
            content()
        }

        // iOS 27 calls this the uniform toolbar: once content scrolls under
        // the bar it turns to glass so the title stays legible.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .alpha(barAlpha)
                .liquidGlass(RectangleShape, navHaze),
        ) {
            Box(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(NAV_BAR_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = NightstandType.Headline,
                    color = palette.label,
                )
            }
        }
    }
}

/**
 * Adds a [ListSection] as a list item, with the gap iOS leaves between
 * grouped sections.
 *
 * Takes string *resources* rather than strings: this runs in `LazyListScope`,
 * which is not a composable context, so the caller could not resolve them.
 */
fun LazyListScope.listSection(
    key: String,
    @StringRes header: Int? = null,
    @StringRes footer: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    item(key = key) {
        Column(verticalArrangement = Arrangement.Top) {
            ListSection(
                header = header?.let { stringResource(it) },
                footer = footer?.let { stringResource(it) },
                content = content,
            )
            Box(Modifier.height(SECTION_GAP))
        }
    }
}

/**
 * A section header with nothing under it yet.
 *
 * For content that is not a grouped card — a grid, say — but still belongs to
 * a section and should carry the same label above it. [listSection] cannot be
 * used there because its content goes inside the card.
 */
fun LazyListScope.sectionHeading(key: String, @StringRes text: Int) {
    item(key = key) {
        Text(
            text = stringResource(text),
            style = NightstandType.Footnote,
            color = NightstandTheme.palette.secondaryLabel,
            modifier = Modifier.padding(
                start = SECTION_INSET + SECTION_LABEL_INSET,
                end = SECTION_INSET + SECTION_LABEL_INSET,
                bottom = 7.dp,
            ),
        )
    }
}

/** The closing half of [sectionHeading], with the gap iOS leaves after a section. */
fun LazyListScope.sectionFooting(key: String, @StringRes text: Int) {
    item(key = key) {
        Column {
            Text(
                text = stringResource(text),
                style = NightstandType.Footnote,
                color = NightstandTheme.palette.secondaryLabel,
                modifier = Modifier.padding(
                    start = SECTION_INSET + SECTION_LABEL_INSET,
                    end = SECTION_INSET + SECTION_LABEL_INSET,
                    top = 7.dp,
                ),
            )
            Box(Modifier.height(SECTION_GAP))
        }
    }
}

private val NAV_BAR_HEIGHT = 44.dp

/** Matches the row padding inside a card, so labels line up with row text. */
private val SECTION_LABEL_INSET = 16.dp

private val SECTION_GAP = 35.dp
