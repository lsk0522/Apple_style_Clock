package com.lsk0522.nightstand.core.design.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * The iOS switch, at its real proportions: a 51×31 track with a 27pt knob.
 *
 * Material's `Switch` is a different object — narrower track, smaller thumb,
 * a tick inside it — so it reads wrong next to anything else Apple-shaped.
 */
@Composable
fun IosSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val palette = com.lsk0522.nightstand.core.design.theme.NightstandTheme.palette
    val interactionSource = remember { MutableInteractionSource() }

    val trackColor by animateColorAsState(
        targetValue = if (checked) palette.positive else palette.switchTrackOff,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchTrack",
    )
    val knobOffset by animateDpAsState(
        targetValue = if (checked) TRACK_WIDTH - KNOB_SIZE - KNOB_MARGIN * 2 else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "switchKnob",
    )

    Box(
        modifier = modifier
            .size(width = TRACK_WIDTH, height = TRACK_HEIGHT)
            .clip(CircleShape)
            .background(if (enabled) trackColor else trackColor.copy(alpha = 0.5f))
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null,
                onValueChange = onCheckedChange,
            )
            .padding(KNOB_MARGIN),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(KNOB_SIZE)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

private val TRACK_WIDTH = 51.dp
private val TRACK_HEIGHT = 31.dp
private val KNOB_SIZE = 27.dp
private val KNOB_MARGIN = 2.dp
