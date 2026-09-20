package com.lsk0522.nightstand.core.design.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.design.theme.CapsuleShape
import com.lsk0522.nightstand.core.design.theme.NightstandMotion
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType

/**
 * A full-width capsule button, the shape iOS uses for the primary action on a
 * sheet or a setup step.
 *
 * [prominent] fills it with the tint; otherwise it is a quiet tinted-text
 * button, which is how iOS marks the secondary way out of a screen.
 */
@Composable
fun IosButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    prominent: Boolean = true,
) {
    val palette = NightstandTheme.palette
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) NightstandMotion.PRESS_SCALE else 1f,
        animationSpec = NightstandMotion.press(),
        label = "buttonScale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(CapsuleShape)
            .background(if (prominent) palette.tint else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = MIN_HEIGHT)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = NightstandType.Headline,
            color = if (prominent) Color.White else palette.tint,
        )
    }
}

private val MIN_HEIGHT = 50.dp
