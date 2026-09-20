package com.lsk0522.nightstand.feature.donate

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/** What can be bought. The ids must match the products set up in Play Console. */
enum class DonationTier(val productId: String) {
    COFFEE("donate_coffee"),
    MEAL("donate_meal"),
    GENEROUS("donate_generous"),
}

/** One tier as the store describes it, with the price in the user's currency. */
data class DonationOffer(
    val tier: DonationTier,
    val formattedPrice: String,
    internal val details: ProductDetails,
)

/** Why nothing can be bought right now. Each one needs a different sentence. */
enum class DonationBlocker {
    /** No Play Store on the device, or it is too old. */
    NO_PLAY_STORE,

    /** Could not reach the store. Worth retrying. */
    NETWORK,

    /**
     * The store answered, but knows none of these products.
     *
     * The ordinary case for any build that did not come from Play: products
     * only resolve for an app installed by the Play Store under a package name
     * that has them registered.
     */
    NOT_AVAILABLE_HERE,
}

sealed interface DonationState {
    data object Connecting : DonationState

    data class Ready(val offers: List<DonationOffer>) : DonationState

    data class Blocked(val reason: DonationBlocker) : DonationState
}

/**
 * Buying the developer a coffee, through Play's in-app products.
 *
 * Donations are consumable and consumed as soon as they land, so the same one
 * can be given again. Treating them as one-off entitlements would let someone
 * donate once and then be told they already own it.
 *
 * Nothing here works in a build that Play did not install: the store resolves
 * products against the installing package, so a sideloaded debug APK gets an
 * empty list. That is [DonationBlocker.NOT_AVAILABLE_HERE] rather than an
 * error, and the screen says so instead of showing a spinner forever.
 */
@Singleton
class DonationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val _state = MutableStateFlow<DonationState>(DonationState.Connecting)
    val state: StateFlow<DonationState> = _state.asStateFlow()

    /** Emits once per completed donation, for the thank-you. */
    private val _thanks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val thanks: SharedFlow<Unit> = _thanks.asSharedFlow()

    private val purchasesUpdated = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach(::settle)
        }
        // Everything else — cancelled, already owned, error — leaves the
        // screen as it was. A donation that did not happen is not a failure
        // worth putting in front of someone.
    }

    private val client: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdated)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    private var connecting = false

    /** Safe to call repeatedly; the screen calls it every time it opens. */
    fun start() {
        if (client.isReady) {
            loadOffers()
            return
        }
        if (connecting) return
        connecting = true
        client.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    connecting = false
                    when (result.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            recoverUnconsumed()
                            loadOffers()
                        }

                        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
                        ->
                            _state.value = DonationState.Blocked(DonationBlocker.NO_PLAY_STORE)

                        else ->
                            _state.value = DonationState.Blocked(DonationBlocker.NETWORK)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    connecting = false
                    // Left disconnected on purpose: the screen calls start()
                    // again when it next opens, and a retry loop behind a
                    // screen nobody is looking at is just battery.
                }
            },
        )
    }

    private fun loadOffers() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                DonationTier.entries.map { tier ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(tier.productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                },
            )
            .build()

        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                _state.value = DonationState.Blocked(DonationBlocker.NETWORK)
                return@queryProductDetailsAsync
            }

            val offers = details.productDetailsList.mapNotNull { product ->
                val tier = DonationTier.entries
                    .firstOrNull { it.productId == product.productId }
                    ?: return@mapNotNull null
                val price = product.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: return@mapNotNull null
                DonationOffer(tier, price, product)
            }.sortedBy { it.tier.ordinal }

            _state.value = if (offers.isEmpty()) {
                DonationState.Blocked(DonationBlocker.NOT_AVAILABLE_HERE)
            } else {
                DonationState.Ready(offers)
            }
        }
    }

    /**
     * Consumes anything left over from a previous run.
     *
     * A donation can complete while the app is being killed. Without this the
     * token stays owned and every later attempt at the same tier is refused as
     * already purchased.
     */
    private fun recoverUnconsumed() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach(::settle)
            }
        }
    }

    fun donate(activity: Activity, offer: DonationOffer) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(offer.details)
                        .build(),
                ),
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    /** Consume a completed purchase so the same tier can be given again. */
    private fun settle(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.consumeAsync(params) { result, _ ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _thanks.tryEmit(Unit)
            }
        }
    }
}
