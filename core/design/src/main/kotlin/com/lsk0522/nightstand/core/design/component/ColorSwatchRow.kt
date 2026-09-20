package com.lsk0522.nightstand.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType

/** One colour offered by a [ColorSwatchRow]. */
data class ColorChoice(
    val id: String,
    val label: String,
    val color: Color,
)

/**
 * A row of colour dots inside a list card, the way iOS lets you tint a
 * calendar or a Focus.
 *
 * The chosen one is marked with a ring set off the dot rather than with a
 * checkmark drawn on top: a tick would have to be light or dark depending on
 * the colour under it, and would stop being legible on at least one of them.
 *
 * @param title the label above the dots; omit for a bare row.
 */
@Composable
fun ColorSwatchRow(
    choices: List<ColorChoice>,
    selectedId: String,
    onSelect: (ColorChoice) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    showSeparator: Boolean = true,
) {
    val palette = NightstandTheme.palette

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = SWATCH_ROW_PADDING, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = NightstandType.Body,
                    color = palette.label,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                choices.forEach { choice ->
                    val selected = choice.id == selectedId
                    Box(
                        modifier = Modifier
                            .size(SWATCH_TARGET)
                            .then(
                                if (selected) {
                                    Modifier.border(RING_WIDTH, palette.label, CircleShape)
                                } else {
                                    Modifier
                                },
                            )
                            .selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = { onSelect(choice) },
                            )
                            // The dot carries no text, so the colour's name
                            // is the only thing a screen reader can announce.
                            .semantics { contentDescription = choice.label },
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(SWATCH_DOT)
                                .background(choice.color, CircleShape),
                        )
                    }
                }
            }
        }

        if (showSeparator) {
            Box(
                modifier = Modifier
                    .padding(start = SWATCH_ROW_PADDING)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(palette.separator),
            )
        }
    }
}

private val SWATCH_ROW_PADDING: Dp = 16.dp
private val SWATCH_TARGET: Dp = 30.dp
private val SWATCH_DOT: Dp = 22.dp
private val RING_WIDTH: Dp = 2.dp
