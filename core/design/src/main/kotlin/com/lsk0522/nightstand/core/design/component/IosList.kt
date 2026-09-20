package com.lsk0522.nightstand.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.R
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.ListCardShape
import com.lsk0522.nightstand.core.design.theme.Radius
import com.lsk0522.nightstand.core.design.theme.SquircleShape

/**
 * An inset-grouped list section — the single most recognisable piece of iOS
 * chrome, and the thing whose absence makes an app read as "Android with a
 * dark theme".
 *
 * The details that carry it, all of which are easy to get subtly wrong:
 *  - the card is inset from the screen edge, not full-bleed
 *  - corners are continuous, not circular ([SquircleShape])
 *  - separators start where the *label* starts, never at the card edge, and
 *    the last row has none at all
 *  - the header sits outside the card, in footnote grey
 */
@Composable
fun ListSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = NightstandTheme.palette

    Column(modifier = modifier.fillMaxWidth()) {
        if (header != null) {
            Text(
                text = header,
                style = NightstandType.Footnote,
                color = palette.secondaryLabel,
                modifier = Modifier.padding(
                    start = SECTION_INSET + ROW_PADDING,
                    end = SECTION_INSET + ROW_PADDING,
                    bottom = 7.dp,
                ),
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = SECTION_INSET)
                .clip(ListCardShape)
                .background(palette.groupedCard),
            content = content,
        )

        if (footer != null) {
            Text(
                text = footer,
                style = NightstandType.Footnote,
                color = palette.secondaryLabel,
                modifier = Modifier.padding(
                    start = SECTION_INSET + ROW_PADDING,
                    end = SECTION_INSET + ROW_PADDING,
                    top = 7.dp,
                ),
            )
        }
    }
}

/**
 * One row inside a [ListSection].
 *
 * @param showSeparator false for the last row in its section.
 * @param onClick when set, the row presses and shows a disclosure chevron.
 */
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    showChevron: Boolean = false,
    showSeparator: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val palette = NightstandTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val rowModifier = if (onClick != null && enabled) {
        modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
        )
    } else {
        modifier
    }

    Column(
        modifier = rowModifier
            .fillMaxWidth()
            // iOS tints the whole row while held rather than drawing a ripple.
            .background(if (pressed) palette.rowPressed else Color.Transparent),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = ROW_MIN_HEIGHT)
                .padding(horizontal = ROW_PADDING, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(ROW_PADDING))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = NightstandType.Body,
                    color = if (enabled) palette.label else palette.tertiaryLabel,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = NightstandType.Footnote,
                        color = palette.secondaryLabel,
                    )
                }
            }

            if (value != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = value,
                    style = NightstandType.Body,
                    color = palette.secondaryLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }

            if (showChevron) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = palette.tertiaryLabel,
                    modifier = Modifier.size(width = 7.dp, height = 12.dp),
                )
            }
        }

        if (showSeparator) {
            // Starts at the label, not at the card edge — and shifts further in
            // when the row has a leading icon, matching the text it aligns to.
            val inset = if (leading != null) ROW_PADDING + LEADING_ICON_SIZE + ROW_PADDING else ROW_PADDING
            Box(
                modifier = Modifier
                    .padding(start = inset)
                    .fillMaxWidth()
                    .height(HAIRLINE)
                    .background(palette.separator),
            )
        }
    }
}

/** A row whose trailing control is a switch; the whole row toggles it. */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    showSeparator: Boolean = true,
) {
    ListRow(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        leading = leading,
        showSeparator = showSeparator,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = {
            IosSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}

/**
 * A row that is one option in a mutually exclusive set. iOS marks the chosen
 * one with a tinted checkmark at the trailing edge rather than a radio button.
 */
@Composable
fun SelectionRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    showSeparator: Boolean = true,
) {
    val palette = NightstandTheme.palette
    ListRow(
        title = title,
        modifier = modifier,
        subtitle = subtitle,
        showSeparator = showSeparator,
        enabled = enabled,
        onClick = onSelect,
        trailing = {
            if (selected) {
                Icon(
                    painter = painterResource(R.drawable.ic_checkmark),
                    contentDescription = null,
                    tint = if (enabled) palette.tint else palette.tertiaryLabel,
                    modifier = Modifier.size(16.dp),
                )
            } else {
                Spacer(Modifier.size(16.dp))
            }
        },
    )
}

/**
 * A rounded square tile behind a row's glyph, the way Settings marks each
 * entry. Keeps the concentric-radius rule: the tile is small, so its corner
 * stays proportionally tighter than the card's.
 */
@Composable
fun RowIconTile(
    tint: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(LEADING_ICON_SIZE)
            // Concentric with the card it sits in: 18 - 11 vertical padding.
            .clip(SquircleShape(Radius.concentric(Radius.listCard, 11.dp)))
            .background(tint),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

/** iOS inset-grouped side margin. */
val SECTION_INSET: Dp = 16.dp
private val ROW_PADDING: Dp = 16.dp
private val ROW_MIN_HEIGHT: Dp = 44.dp
private val LEADING_ICON_SIZE: Dp = 29.dp
private val HAIRLINE: Dp = 0.5.dp
