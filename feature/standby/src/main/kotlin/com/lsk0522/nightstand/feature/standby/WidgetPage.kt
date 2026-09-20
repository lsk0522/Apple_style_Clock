package com.lsk0522.nightstand.feature.standby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost
import com.lsk0522.nightstand.feature.widgets.WidgetHostLifecycle
import com.lsk0522.nightstand.feature.widgets.WidgetTile

/**
 * The widget half of StandBy: square tiles side by side, as on iOS.
 *
 * Tiles are sized from the screen height rather than a fixed dp so they stay
 * square and fill the panel on any phone.
 */
@Composable
fun WidgetPage(
    host: StandbyWidgetHost,
    widgets: List<HostedWidget>,
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

        val tile = maxHeight - Margin * 2

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Margin),
            horizontalArrangement = Arrangement.spacedBy(Gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Two at a time is what fits legibly on a phone in landscape.
            widgets.take(2).forEach { widget ->
                Box {
                    WidgetTile(
                        host = host,
                        appWidgetId = widget.appWidgetId,
                        size = tile,
                    )
                }
            }
        }
    }
}

private val Margin = 24.dp
private val Gap = 20.dp
