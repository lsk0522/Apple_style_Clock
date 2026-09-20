package com.lsk0522.nightstand.feature.widgets

import android.appwidget.AppWidgetProviderInfo
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lsk0522.nightstand.core.design.component.SECTION_INSET
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.Radius
import com.lsk0522.nightstand.core.design.theme.SquircleShape

/**
 * The widget picker as a gallery rather than a list.
 *
 * Widgets are the one kind of list entry whose name tells you almost nothing —
 * "Clock" could be a dial, a stack of digits or a world map. Every provider
 * ships a preview image for exactly this reason, and iOS shows them at a size
 * you can actually read.
 *
 * Two to a row: three would put a Samsung 4x2 preview at a width where the
 * type inside it stops being legible, and the point of a preview is to be
 * looked at.
 */
fun LazyListScope.widgetGallery(
    providers: List<AppWidgetProviderInfo>,
    onPick: (AppWidgetProviderInfo) -> Unit,
) {
    val rows = providers.chunked(COLUMNS)
    rows.forEachIndexed { index, row ->
        item(key = "gallery-$index") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SECTION_INSET, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { info ->
                    WidgetPreviewCard(
                        info = info,
                        onClick = { onPick(info) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keeps a lone card on the last row the same width as the rest
                // instead of letting it stretch across.
                repeat(COLUMNS - row.size) {
                    Box(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WidgetPreviewCard(
    info: AppWidgetProviderInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val palette = NightstandTheme.palette

    Column(
        modifier = modifier
            .clip(SquircleShape(Radius.listCard))
            .background(palette.groupedCard)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(PREVIEW_HEIGHT),
            factory = { viewContext ->
                ImageView(viewContext).apply {
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    adjustViewBounds = true
                    // The label underneath already names it.
                    importantForAccessibility = ImageView.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
            },
            update = { view ->
                // Not every provider ships a preview; the launcher icon is the
                // fallback the platform itself uses.
                val art = runCatching { info.loadPreviewImage(context, 0) }.getOrNull()
                    ?: runCatching { info.loadIcon(context, 0) }.getOrNull()
                view.setImageDrawable(art)
            },
        )

        Text(
            text = info.loadLabel(context.packageManager),
            style = NightstandType.Footnote,
            color = palette.label,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private const val COLUMNS = 2
private val PREVIEW_HEIGHT = 104.dp
