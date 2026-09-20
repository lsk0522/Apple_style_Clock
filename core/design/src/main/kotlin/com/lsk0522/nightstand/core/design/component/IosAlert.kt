package com.lsk0522.nightstand.core.design.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.SquircleShape

/**
 * The iOS alert: a narrow centred card with the choices as full-width buttons
 * divided by hairlines.
 *
 * Material's `AlertDialog` puts its actions in a right-aligned row at the
 * bottom, which is the one detail that gives an imitation away immediately.
 *
 * @param confirmIsDestructive draws the confirming action in red, for choices
 *   that lose something.
 */
@Composable
fun IosAlert(
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmIsDestructive: Boolean = false,
) {
    val palette = NightstandTheme.palette

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .width(ALERT_WIDTH)
                .clip(SquircleShape(CORNER))
                .background(palette.groupedCard),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = NightstandType.Headline,
                    color = palette.label,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = NightstandType.Footnote,
                    color = palette.label,
                    textAlign = TextAlign.Center,
                )
            }

            Hairline()

            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                AlertAction(
                    label = cancelLabel,
                    emphasised = false,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier
                        .width(HAIRLINE)
                        .fillMaxHeight()
                        .background(palette.separator),
                )
                AlertAction(
                    label = confirmLabel,
                    emphasised = true,
                    destructive = confirmIsDestructive,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AlertAction(
    label: String,
    emphasised: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    val palette = NightstandTheme.palette
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .height(ACTION_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = NightstandType.Body.copy(
                fontWeight = if (emphasised) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (destructive) palette.destructive else palette.tint,
        )
    }
}

@Composable
private fun Hairline() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(HAIRLINE)
            .background(NightstandTheme.palette.separator),
    )
}

/** UIKit's alert width. */
private val ALERT_WIDTH = 270.dp
private val CORNER = 14.dp
private val ACTION_HEIGHT = 44.dp
private val HAIRLINE = 0.5.dp
