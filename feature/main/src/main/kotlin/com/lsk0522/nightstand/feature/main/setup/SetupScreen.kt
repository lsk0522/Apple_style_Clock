package com.lsk0522.nightstand.feature.main.setup

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.data.system.SystemRequirement
import com.lsk0522.nightstand.core.data.system.SystemRequirementId
import com.lsk0522.nightstand.core.design.component.IosButton
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.SwitchRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.feature.main.R

/**
 * The screen that stands in front of the app on first run.
 *
 * Every item here lives in system settings and cannot be granted from inside
 * the app. So rather than printing a path for the user to hunt down, each row
 * opens the exact screen. Status is re-read whenever the app returns to the
 * foreground, because none of these settings notify anyone when they change.
 *
 * It guides rather than blocks: the user can continue with items still off,
 * and the same list stays reachable from the main tab afterwards.
 */
@Composable
fun SetupScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val palette = NightstandTheme.palette

    // Coming back from a system settings screen is the only signal we get.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { viewModel.refresh() }

    fun open(id: SystemRequirementId) {
        val intent = viewModel.settingsIntent(id) ?: return
        context.startSettings(intent) { context.startSettings(viewModel.fallbackIntent(id)) }
    }

    IosScreen(
        title = stringResource(R.string.setup_title),
        modifier = modifier,
        bottomClearance = 0.dp,
    ) {
        item(key = "intro") {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            ) {
                Text(
                    text = stringResource(R.string.setup_intro),
                    style = NightstandType.Body,
                    color = palette.secondaryLabel,
                )
                Text(
                    text = stringResource(R.string.setup_intro_hint),
                    style = NightstandType.Body,
                    color = palette.secondaryLabel,
                )
            }
        }

        listSection(
            key = "required",
            header = R.string.setup_required_header,
            footer = R.string.setup_required_footer,
        ) {
            RequirementRow(
                requirement = state.requirement(SystemRequirementId.OVERLAY),
                title = stringResource(R.string.setup_overlay),
                subtitle = stringResource(R.string.setup_overlay_why),
                onOpen = { open(SystemRequirementId.OVERLAY) },
            )
            RequirementRow(
                requirement = state.requirement(SystemRequirementId.BATTERY_OPTIMIZATION),
                title = stringResource(R.string.setup_battery),
                subtitle = stringResource(R.string.setup_battery_why),
                showSeparator = false,
                onOpen = { open(SystemRequirementId.BATTERY_OPTIMIZATION) },
            )
        }

        listSection(
            key = "optional",
            header = R.string.setup_optional_header,
            footer = R.string.setup_optional_footer,
        ) {
            val hasDailyBoard = state.has(SystemRequirementId.SAMSUNG_DAILY_BOARD)

            RequirementRow(
                requirement = state.requirement(SystemRequirementId.NOTIFICATIONS),
                title = stringResource(R.string.setup_notifications),
                subtitle = stringResource(R.string.setup_notifications_why),
                showSeparator = hasDailyBoard,
                onOpen = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )

            if (hasDailyBoard) {
                ListRow(
                    title = stringResource(R.string.setup_daily_board),
                    subtitle = stringResource(R.string.setup_daily_board_why),
                    showChevron = true,
                    onClick = { open(SystemRequirementId.SAMSUNG_DAILY_BOARD) },
                )
                SwitchRow(
                    title = stringResource(R.string.setup_daily_board_done),
                    checked = state.isSatisfied(SystemRequirementId.SAMSUNG_DAILY_BOARD),
                    onCheckedChange = viewModel::setDailyBoardHandled,
                    showSeparator = false,
                )
            }
        }

        item(key = "continue") {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                IosButton(
                    label = stringResource(
                        if (state.isReady) R.string.setup_start else R.string.setup_continue_anyway,
                    ),
                    prominent = state.isReady,
                    onClick = {
                        viewModel.dismissSetup()
                        onDone()
                    },
                )
                if (!state.isReady) {
                    Text(
                        text = stringResource(R.string.setup_remaining, state.blockingCount),
                        style = NightstandType.Footnote,
                        color = palette.secondaryLabel,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RequirementRow(
    requirement: SystemRequirement?,
    title: String,
    subtitle: String,
    onOpen: () -> Unit,
    showSeparator: Boolean = true,
) {
    val palette = NightstandTheme.palette
    val satisfied = requirement?.isSatisfied == true

    ListRow(
        title = title,
        subtitle = subtitle,
        showSeparator = showSeparator,
        showChevron = !satisfied,
        onClick = if (satisfied) null else onOpen,
        trailing = {
            Text(
                text = stringResource(
                    if (satisfied) R.string.setup_state_on else R.string.setup_state_needed,
                ),
                style = NightstandType.Body,
                color = if (satisfied) palette.positive else palette.warning,
            )
        },
    )
}

/** Starts a settings screen, falling back when the device does not have it. */
private inline fun Context.startSettings(intent: Intent, onMissing: () -> Unit = {}) {
    try {
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        onMissing()
    }
}

private fun SetupUiState.requirement(id: SystemRequirementId): SystemRequirement? =
    requirements.firstOrNull { it.id == id }
