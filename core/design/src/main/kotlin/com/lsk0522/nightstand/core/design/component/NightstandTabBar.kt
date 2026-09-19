package com.lsk0522.nightstand.core.design.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import dev.chrisbanes.haze.HazeState

/** One entry in the bottom bar. */
@Immutable
data class TabItem(
    val label: String,
    @param:DrawableRes val icon: Int,
)

/**
 * The tab bar, in the Liquid Glass shape iOS 26 introduced and iOS 27 refined:
 * a capsule that **floats over** the content, inset from all three edges,
 * rather than a full-width strip welded to the bottom of the screen.
 *
 * Because it floats, the caller must lay it over the content (a `Box`, not a
 * `Column`) and leave [FLOATING_BAR_CLEARANCE] of scroll padding underneath.
 */
@Composable
fun NightstandTabBar(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = BAR_SIDE_INSET, vertical = BAR_BOTTOM_INSET)
            .liquidGlass(RoundedCornerShape(percent = 50), hazeState)
            .height(BAR_HEIGHT),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            TabBarItem(
                item = item,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TabBarItem(
    item: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val tint by animateColorAsState(
        targetValue = if (selected) palette.tint else palette.secondaryLabel,
        animationSpec = NightstandMotion.press(),
        label = "tabTint",
    )
    // Pressing shrinks the item slightly instead of flashing a ripple.
    val scale by animateFloatAsState(
        targetValue = if (pressed) NightstandMotion.PRESS_SCALE else 1f,
        animationSpec = NightstandMotion.press(),
        label = "tabScale",
    )

    Column(
        modifier = modifier
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null, // the label below already names it
            tint = tint,
            modifier = Modifier.size(ICON_SIZE),
        )
        Text(
            text = item.label,
            style = NightstandType.Caption2,
            color = tint,
        )
    }
}

private val BAR_HEIGHT = 56.dp
private val BAR_SIDE_INSET = 16.dp
private val BAR_BOTTOM_INSET = 10.dp
private val ICON_SIZE = 24.dp

/**
 * How much room a scrolling screen must leave at its bottom so the last row
 * does not end up underneath the floating bar.
 */
val FLOATING_BAR_CLEARANCE: Dp = BAR_HEIGHT + BAR_BOTTOM_INSET * 2 + 8.dp
