package com.lsk0522.nightstand.feature.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.common.model.WidgetRotationInterval
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.design.component.IosAlert
import com.lsk0522.nightstand.core.design.component.IosButton
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SelectionRow
import com.lsk0522.nightstand.core.design.component.SwitchRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.core.design.component.sectionFooting
import com.lsk0522.nightstand.core.design.component.sectionHeading
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.R as DesignR

/**
 * Picks and manages the widgets shown on the StandBy screen.
 *
 * The list is whatever is installed on the phone, so on a Galaxy that is the
 * Samsung clock, weather and calendar — the thing iOS StandBy cannot do, since
 * it only accepts widgets built for it.
 *
 * Adding one is up to two prompts, neither of which this app controls:
 * the system asks whether to allow binding, and then the widget itself may
 * insist on being configured before it will draw anything. Neither is allowed
 * to fail silently here — a tap that does nothing is the worst outcome, and it
 * was the outcome for every widget that named a configuration screen.
 */
@Composable
fun WidgetsScreen(
    modifier: Modifier = Modifier,
    viewModel: WidgetsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val added by viewModel.widgets.collectAsStateWithLifecycle()
    val autoRotate by viewModel.autoRotate.collectAsStateWithLifecycle()
    val interval by viewModel.rotationInterval.collectAsStateWithLifecycle()
    val providers = remember { viewModel.installedProviders() }

    var picking by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingWidget?>(null) }

    /** Something the user has to be told about an add; null most of the time. */
    var notice by remember { mutableStateOf<Int?>(null) }

    /** Reordering is a mode, the way it is in an iOS list. */
    var editing by remember { mutableStateOf(false) }

    /** Queued for removal, so a stray tap cannot delete a widget outright. */
    var removing by remember { mutableStateOf<HostedWidget?>(null) }

    fun keep(widget: PendingWidget, message: Int? = null) {
        viewModel.confirmAdded(widget.id, widget.provider)
        notice = message
        pending = null
        picking = false
    }

    fun drop(widget: PendingWidget, message: Int? = null) {
        viewModel.abandon(widget.id)
        notice = message
        pending = null
        picking = false
    }

    // Declared before the bind launcher because binding may hand straight over
    // to it.
    val configureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { _ ->
        // The widget is kept whatever the configuration screen answered.
        //
        // A cancelled result cannot be told apart from a refusal: several One
        // UI configuration activities decline to run for a host that is not
        // the launcher and return CANCELLED at once, without showing the user
        // anything. Dropping the widget then means half the phone's stock
        // widgets cannot be added at all, and what the user sees is a tap that
        // did nothing. Kept, the tile draws whatever the widget draws
        // unconfigured — usually its own invitation to set it up — and it can
        // always be removed.
        pending?.let { keep(it) }
    }

    fun finishAdding(widget: PendingWidget) {
        if (widget.configure == null) {
            keep(widget)
            return
        }
        pending = widget
        val configuring = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
            component = widget.configure
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widget.id)
        }
        // Some providers name a configuration activity that will not start for
        // anyone but the launcher. Keeping the widget beats failing silently.
        runCatching { configureLauncher.launch(configuring) }
            .onFailure { keep(widget, R.string.widgets_notice_no_setup) }
    }

    val bindLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val current = pending ?: return@rememberLauncherForActivityResult
        if (result.resultCode == Activity.RESULT_OK) {
            finishAdding(current)
        } else {
            // Unambiguous: the system asked the user directly and they said
            // no. Hand the id back rather than leaking it.
            drop(current, R.string.widgets_notice_denied)
        }
    }

    fun add(info: AppWidgetProviderInfo) {
        val (id, alreadyBound) = viewModel.prepareBinding(info.provider)
        val widget = PendingWidget(id, info.provider, info.configure)
        if (alreadyBound) {
            finishAdding(widget)
            return
        }
        pending = widget
        runCatching { bindLauncher.launch(viewModel.host.bindIntent(id, info.provider)) }
            .onFailure { drop(widget, R.string.widgets_notice_bind_failed) }
    }

    notice?.let { message ->
        IosAlert(
            title = stringResource(R.string.widgets_notice_title),
            message = stringResource(message),
            confirmLabel = stringResource(R.string.widgets_notice_confirm),
            onConfirm = { notice = null },
            onDismiss = { notice = null },
        )
    }

    removing?.let { widget ->
        IosAlert(
            title = stringResource(R.string.widgets_remove_title),
            message = stringResource(R.string.widgets_remove_message),
            confirmLabel = stringResource(R.string.widgets_remove_confirm),
            cancelLabel = stringResource(R.string.widgets_remove_cancel),
            confirmIsDestructive = true,
            onConfirm = {
                viewModel.remove(widget)
                removing = null
            },
            onDismiss = { removing = null },
        )
    }

    IosScreen(
        title = stringResource(
            if (picking) R.string.widgets_pick_title else R.string.widgets_title,
        ),
        modifier = modifier,
    ) {
        if (picking) {
            sectionHeading(
                key = "availableHeader",
                text = R.string.widgets_available_header,
            )
            widgetGallery(providers = providers, onPick = ::add)
            sectionFooting(
                key = "availableFooter",
                text = R.string.widgets_available_footer,
            )
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
                added.forEachIndexed { index, widget ->
                    val info = viewModel.host.providerInfo(widget.appWidgetId)
                    ListRow(
                        title = info?.loadLabel(context.packageManager)
                            ?: stringResource(R.string.widgets_unavailable),
                        // Which slot it lands in, since they are dealt
                        // alternately and that is not obvious from a list.
                        subtitle = stringResource(R.string.widgets_slot, index % 2 + 1),
                        value = if (editing) {
                            null
                        } else {
                            stringResource(R.string.widgets_remove_action)
                        },
                        trailing = if (!editing) {
                            null
                        } else {
                            {
                                ReorderControls(
                                    canMoveUp = index > 0,
                                    canMoveDown = index < added.lastIndex,
                                    onUp = { viewModel.move(widget, -1) },
                                    onDown = { viewModel.move(widget, 1) },
                                )
                            }
                        },
                        onClick = if (editing) null else { { removing = widget } },
                    )
                }
            }
            if (added.size > 1) {
                ListRow(
                    title = stringResource(
                        if (editing) R.string.widgets_reorder_done else R.string.widgets_reorder,
                    ),
                    onClick = { editing = !editing },
                )
            }
            ListRow(
                title = stringResource(R.string.widgets_add),
                showChevron = true,
                showSeparator = false,
                onClick = { picking = true },
            )
        }

        listSection(
            key = "rotate",
            header = R.string.widgets_rotate_header,
            footer = R.string.widgets_rotate_footer,
        ) {
            SwitchRow(
                title = stringResource(R.string.widgets_rotate_enabled),
                checked = autoRotate,
                onCheckedChange = viewModel::setAutoRotate,
                showSeparator = false,
            )
        }

        // Only worth asking how often once it is going to happen at all.
        if (autoRotate) {
            listSection(
                key = "interval",
                header = R.string.widgets_rotate_interval,
            ) {
                WidgetRotationInterval.entries.forEachIndexed { index, option ->
                    SelectionRow(
                        title = stringResource(option.labelRes()),
                        selected = interval == option,
                        onSelect = { viewModel.setRotationInterval(option) },
                        showSeparator = index != WidgetRotationInterval.entries.lastIndex,
                    )
                }
            }
        }
    }
}

private fun WidgetRotationInterval.labelRes(): Int = when (this) {
    WidgetRotationInterval.TEN_SECONDS -> R.string.widgets_rotate_10s
    WidgetRotationInterval.THIRTY_SECONDS -> R.string.widgets_rotate_30s
    WidgetRotationInterval.ONE_MINUTE -> R.string.widgets_rotate_1m
    WidgetRotationInterval.FIVE_MINUTES -> R.string.widgets_rotate_5m
}

/**
 * Up and down arrows for one row.
 *
 * Arrows rather than a drag handle: dragging inside a `LazyColumn` that also
 * scrolls needs the two gestures told apart, and at three or four widgets the
 * arrows are fewer taps than a drag anyway.
 */
@Composable
private fun ReorderControls(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
) {
    Row {
        ReorderArrow(
            icon = DesignR.drawable.ic_chevron_up,
            description = stringResource(R.string.widgets_move_up),
            enabled = canMoveUp,
            onClick = onUp,
        )
        ReorderArrow(
            icon = DesignR.drawable.ic_chevron_down,
            description = stringResource(R.string.widgets_move_down),
            enabled = canMoveDown,
            onClick = onDown,
        )
    }
}

@Composable
private fun ReorderArrow(
    icon: Int,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val palette = NightstandTheme.palette
    Box(
        // 44dp is the smallest target iOS ships, and two of them side by side
        // still fit the row height.
        modifier = Modifier
            .size(44.dp)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = if (enabled) palette.tint else palette.tertiaryLabel,
            modifier = Modifier.size(width = 16.dp, height = 10.dp),
        )
    }
}

/** A widget partway through being added. */
private data class PendingWidget(
    val id: Int,
    val provider: ComponentName,
    val configure: ComponentName?,
)
