package com.lsk0522.nightstand.feature.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.WidgetTileShape

/**
 * One hosted widget, framed as a StandBy tile.
 *
 * The widget draws itself with its own app's styling, so the only thing this
 * can do is give it a consistent shape and size. Clipping to the tile's
 * continuous corner is what keeps a grid of widgets from every app looking
 * like a pile of unrelated rectangles.
 */
@Composable
fun WidgetTile(
    host: StandbyWidgetHost,
    appWidgetId: Int,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val palette = NightstandTheme.standby
    val sizeDp = remember(size) { size.value.toInt() }

    Box(
        modifier = modifier
            .size(size)
            .clip(WidgetTileShape)
            .background(palette.tile),
        contentAlignment = Alignment.Center,
    ) {
        val view = remember(appWidgetId, sizeDp) {
            host.createView(appWidgetId, sizeDp, sizeDp)
        }

        if (view == null) {
            // The provider was uninstalled, or the binding was revoked. Saying
            // so beats an empty square the user cannot explain.
            Text(
                text = "—",
                style = NightstandType.Title2,
                color = palette.textTertiary,
            )
            return@Box
        }

        DisposableEffect(view) {
            onDispose { (view.parent as? android.view.ViewGroup)?.removeView(view) }
        }

        AndroidView(
            factory = { view },
            modifier = Modifier.fillMaxSize(),
            update = { hostView ->
                host.applySize(hostView, sizeDp, sizeDp)
            },
        )
    }
}

/** Keeps the host listening only while widgets are actually on screen. */
@Composable
fun WidgetHostLifecycle(host: StandbyWidgetHost) {
    DisposableEffect(host) {
        host.startListening()
        onDispose { host.stopListening() }
    }
}
