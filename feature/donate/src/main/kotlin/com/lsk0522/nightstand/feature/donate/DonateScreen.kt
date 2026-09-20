package com.lsk0522.nightstand.feature.donate

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lsk0522.nightstand.core.design.component.IosAlert
import com.lsk0522.nightstand.core.design.component.IosScreen
import com.lsk0522.nightstand.core.design.component.ListRow
import com.lsk0522.nightstand.core.design.component.listSection
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType

/**
 * Tips, through Play's in-app products.
 *
 * The screen has three honest shapes: still asking the store, a list of real
 * prices, or a sentence saying why there is nothing to tap. It never shows a
 * price the store did not give us — a hard-coded amount would be wrong in
 * every currency but one.
 */
@Composable
fun DonateScreen(
    modifier: Modifier = Modifier,
    viewModel: DonateViewModel = hiltViewModel(),
) {
    val palette = NightstandTheme.palette
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    var thanking by remember { mutableStateOf(false) }

    // The billing service can be dropped while the app is in the background.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.connect() }

    LaunchedEffect(Unit) {
        viewModel.thanks.collect { thanking = true }
    }

    if (thanking) {
        IosAlert(
            title = stringResource(R.string.donate_thanks_title),
            message = stringResource(R.string.donate_thanks_message),
            confirmLabel = stringResource(R.string.donate_thanks_confirm),
            onConfirm = { thanking = false },
            onDismiss = { thanking = false },
        )
    }

    IosScreen(title = stringResource(R.string.donate_title), modifier = modifier) {
        listSection(key = "intro") {
            Text(
                text = stringResource(R.string.donate_intro),
                style = NightstandType.Body,
                color = palette.label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            )
        }

        listSection(
            key = "items",
            header = R.string.donate_items_header,
            footer = R.string.donate_footer,
        ) {
            when (val current = state) {
                DonationState.Connecting -> ListRow(
                    title = stringResource(R.string.donate_connecting),
                    enabled = false,
                    showSeparator = false,
                    onClick = null,
                )

                is DonationState.Blocked -> ListRow(
                    title = stringResource(current.reason.labelRes()),
                    subtitle = stringResource(current.reason.detailRes()),
                    enabled = false,
                    showSeparator = false,
                    onClick = null,
                )

                is DonationState.Ready -> current.offers.forEachIndexed { index, offer ->
                    ListRow(
                        title = stringResource(offer.tier.labelRes()),
                        subtitle = stringResource(offer.tier.captionRes()),
                        value = offer.formattedPrice,
                        showChevron = true,
                        showSeparator = index != current.offers.lastIndex,
                        onClick = {
                            context.findActivity()?.let { viewModel.donate(it, offer) }
                        },
                    )
                }
            }
        }
    }
}

/**
 * The Activity behind a Composable's context.
 *
 * `launchBillingFlow` needs a real Activity to put the sheet over; the context
 * a Composable sees is usually a wrapper around it.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun DonationTier.labelRes(): Int = when (this) {
    DonationTier.COFFEE -> R.string.donate_coffee
    DonationTier.MEAL -> R.string.donate_meal
    DonationTier.GENEROUS -> R.string.donate_generous
}

private fun DonationTier.captionRes(): Int = when (this) {
    DonationTier.COFFEE -> R.string.donate_coffee_why
    DonationTier.MEAL -> R.string.donate_meal_why
    DonationTier.GENEROUS -> R.string.donate_generous_why
}

private fun DonationBlocker.labelRes(): Int = when (this) {
    DonationBlocker.NO_PLAY_STORE -> R.string.donate_no_play
    DonationBlocker.NETWORK -> R.string.donate_network
    DonationBlocker.NOT_AVAILABLE_HERE -> R.string.donate_not_here
}

private fun DonationBlocker.detailRes(): Int = when (this) {
    DonationBlocker.NO_PLAY_STORE -> R.string.donate_no_play_why
    DonationBlocker.NETWORK -> R.string.donate_network_why
    DonationBlocker.NOT_AVAILABLE_HERE -> R.string.donate_not_here_why
}
