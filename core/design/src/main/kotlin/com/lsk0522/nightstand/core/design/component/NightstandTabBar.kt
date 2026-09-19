package com.lsk0522.nightstand.core.design.component

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.Spacing

/** One entry in the bottom bar. */
@Immutable
data class TabItem(
    val label: String,
    @param:DrawableRes val icon: Int,
)

/**
 * The bottom tab bar — Design.md `components.tab-bar-container`.
 *
 * Deliberately not Material's `NavigationBar`: that one draws a pill-shaped
 * selection indicator and uses Material's own spacing, neither of which belongs
 * in an Apple-style bar. Selection here reads only through colour and a small
 * scale change, the way a UITabBar does.
 */
@Composable
fun NightstandTabBar(
    items: List<TabItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.palette

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.tabBar)
            // 1px hairline along the top edge, the way iOS separates the bar
            // from the content behind it.
            .drawBehind {
                drawLine(
                    color = palette.glassRim,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1f,
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(TAB_BAR_HEIGHT),
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
        targetValue = if (selected) palette.accent else palette.textTertiary,
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
            .scale(scale)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(
            painter = painterResource(item.icon),
            contentDescription = null, // the label below already names it
            tint = tint,
            modifier = Modifier.size(ICON_SIZE),
        )
        Text(
            text = item.label,
            style = NightstandType.Footnote,
            color = tint,
        )
    }
}

private val TAB_BAR_HEIGHT = 64.dp
private val ICON_SIZE = 25.dp
