package com.lsk0522.nightstand.feature.widgets

import android.app.Activity
import android.widget.ImageView
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.design.component.IosButton
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.listSection

/**
 * Picks and manages the widgets shown on the StandBy screen.
 *
 * The list is whatever is installed on the phone, so on a Galaxy that is the
 * Samsung clock, weather and calendar — the thing iOS StandBy cannot do, since
 * it only accepts widgets built for it.
 */
@Composable
fun WidgetsScreen(
    modifier: Modifier = Modifier,
    viewModel: WidgetsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val added by viewModel.widgets.collectAsStateWithLifecycle()
    val providers = remember { viewModel.installedProviders() }

    var picking by remember { mutableStateOf(false) }
    var pendingId by remember { mutableStateOf<Int?>(null) }
    var pendingProvider by remember { mutableStateOf<ComponentName?>(null) }

    // The system's consent prompt. Its result is the only way to learn whether
    // the user allowed this app to bind the widget.
    val bindLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val id = pendingId
        val provider = pendingProvider
        if (id != null && provider != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                viewModel.confirmAdded(id, provider)
            } else {
                // Declined. Hand the id back rather than leaking it.
                viewModel.abandon(id)
            }
        }
        pendingId = null
        pendingProvider = null
        picking = false
    }

    fun add(info: AppWidgetProviderInfo) {
        val (id, alreadyBound) = viewModel.prepareBinding(info.provider)
        if (alreadyBound) {
            viewModel.confirmAdded(id, info.provider)
            picking = false
            return
        }
        pendingId = id
        pendingProvider = info.provider
        bindLauncher.launch(viewModel.host.bindIntent(id, info.provider))
    }

    IosScreen(
        title = stringResource(
            if (picking) R.string.widgets_pick_title else R.string.widgets_title,
        ),
        modifier = modifier,
    ) {
        if (picking) {
            listSection(key = "available", header = R.string.widgets_available_header) {
                providers.forEach { info ->
                    ListRow(
                        title = info.loadLabel(context.packageManager),
                        leading = { ProviderIcon(info) },
                        showChevron = true,
                        showSeparator = info != providers.lastOrNull(),
                        onClick = { add(info) },
                    )
                }
            }
            item(key = "cancel") {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    IosButton(
                        label = stringResource(R.string.widgets_cancel),
                        prominent = false,
                        onClick = { picking = false },
                    )
                }
            }
            return@IosScreen
        }

        listSection(
            key = "added",
            header = R.string.widgets_added_header,
            footer = R.string.widgets_add_footer,
        ) {
            if (added.isEmpty()) {
                ListRow(
                    title = stringResource(R.string.widgets_empty),
                    enabled = false,
                    onClick = null,
                )
            } else {
                added.forEach { widget ->
                    val info = viewModel.host.providerInfo(widget.appWidgetId)
                    ListRow(
                        title = info?.loadLabel(context.packageManager)
                            ?: stringResource(R.string.widgets_unavailable),
                        subtitle = stringResource(R.string.widgets_remove_hint),
                        onClick = { viewModel.remove(widget) },
                    )
                }
            }
            ListRow(
                title = stringResource(R.string.widgets_add),
                showChevron = true,
                showSeparator = false,
                onClick = { picking = true },
            )
        }
    }
}

/** The provider's own icon, loaded as a plain view so no image library is needed. */
@Composable
private fun ProviderIcon(info: AppWidgetProviderInfo) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.size(29.dp),
        factory = { ImageView(it) },
        update = { view ->
            runCatching {
                view.setImageDrawable(info.loadIcon(context, 0))
            }
        },
    )
}
