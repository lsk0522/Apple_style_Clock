package com.lsk0522.nightstand.feature.standby

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.common.model.WidgetRotationInterval
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost
import com.lsk0522.nightstand.feature.widgets.WidgetHostLifecycle
import com.lsk0522.nightstand.feature.widgets.WidgetTile
import kotlinx.coroutines.delay

/**
 * The widget half of StandBy: slots side by side, each its own stack.
 *
 * This is the iOS arrangement, and the reason for it is that a stack is what
 * makes more than two widgets reachable at all. The alternative — one page of
 * two, then another page of two — means the pair you put together is fixed,
 * and it is a different gesture from the one people already know.
 *
 * Widgets are dealt round-robin into the stacks, so the first ones you add are
 * the ones you see.
 */
@Composable
fun WidgetPage(
    host: StandbyWidgetHost,
    widgets: List<HostedWidget>,
    autoRotate: Boolean,
    interval: WidgetRotationInterval,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby
    WidgetHostLifecycle(host)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        if (widgets.isEmpty()) {
            Text(
                text = "",
                style = NightstandType.Title3,
                color = palette.textTertiary,
                modifier = Modifier.align(Alignment.Center),
            )
            return@BoxWithConstraints
        }

        // Two slots on a phone, the way iOS StandBy has it. A tablet or an
        // unfolded foldable takes a third: the short edge is what decides,
        // which is the same thing `sw600dp` means, and on those screens two
        // tiles leave a conspicuous stripe of black between them.
        val slots = if (maxHeight >= LargeScreenShortEdge) 3 else 2

        // Dealt round-robin rather than split into blocks: with three widgets
        // and two slots, splitting would bury the second one under the first.
        val stacks = remember(widgets, slots) {
            widgets.withIndex()
                .groupBy { it.index % slots }
                .toSortedMap()
                .map { (_, entries) -> entries.map { it.value } }
        }

        // Square, and small enough that two of them plus their dots fit across.
        val tile = minOf(
            maxHeight - Margin * 2,
            (maxWidth - Margin * 2 - Gap * (stacks.size - 1)) / stacks.size - DotsColumn,
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Margin),
            horizontalArrangement = Arrangement.spacedBy(Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            stacks.forEach { stack ->
                WidgetStack(
                    host = host,
                    widgets = stack,
                    tile = tile,
                    autoRotate = autoRotate,
                    interval = interval,
                )
            }
        }
    }
}

/**
 * One slot: a vertical pager of widgets with dots down its side.
 *
 * Auto-advance is re-armed from the settled page rather than run off a single
 * loop, so a swipe by hand restarts the countdown instead of being overruled a
 * moment later by a timer that was already running.
 */
@Composable
private fun WidgetStack(
    host: StandbyWidgetHost,
    widgets: List<HostedWidget>,
    tile: Dp,
    autoRotate: Boolean,
    interval: WidgetRotationInterval,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { widgets.size })

    LaunchedEffect(autoRotate, interval, widgets.size, pagerState.settledPage) {
        if (!autoRotate || widgets.size <= 1) return@LaunchedEffect
        delay(interval.millis)
        pagerState.animateScrollToPage((pagerState.settledPage + 1) % widgets.size)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.size(tile),
        ) { page ->
            WidgetTile(
                host = host,
                appWidgetId = widgets[page].appWidgetId,
                size = tile,
            )
        }

        if (widgets.size > 1) {
            Spacer(Modifier.width(DotGap))
            StackDots(count = widgets.size, current = pagerState.currentPage)
        }
    }
}

/** The page dots, running down the side of the slot as they do on iOS. */
@Composable
private fun StackDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    val palette = NightstandTheme.standby

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DotGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(count) { index ->
            val colour by animateColorAsState(
                targetValue = if (index == current) {
                    palette.textPrimary
                } else {
                    palette.textTertiary
                },
                label = "stackDot",
            )
            Box(
                Modifier
                    .size(DotSize)
                    .clip(CircleShape)
                    .background(colour),
            )
        }
    }
}

/** The short edge at which a third slot starts being worth it. */
private val LargeScreenShortEdge = 600.dp

private val Margin = 24.dp
private val Gap = 20.dp
private val DotSize = 6.dp
private val DotGap = 6.dp

/** Space set aside beside each tile for its dots. */
private val DotsColumn = DotSize + DotGap
