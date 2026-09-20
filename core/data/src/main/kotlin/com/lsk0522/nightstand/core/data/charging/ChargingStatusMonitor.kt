package com.lsk0522.nightstand.core.data.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Reports how the phone is being charged, from `ACTION_BATTERY_CHANGED`.
 *
 * Two things make this less trivial than reading `EXTRA_PLUGGED`:
 *
 *  - **Manufacturers lie briefly.** Several phones report `AC` for a moment
 *    when a wireless pad engages, only settling on `WIRELESS` a beat later.
 *    Acting on the first value would start StandBy under the wrong rule, so a
 *    reading has to hold still before it counts.
 *  - **Reverse wireless charging looks identical.** On a phone sharing power
 *    (the S25 Ultra does), `EXTRA_PLUGGED` still says `WIRELESS` even though
 *    the battery is going *down*. `EXTRA_STATUS` is what separates them.
 */
@Singleton
class ChargingStatusMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    /**
     * Emits whenever the charging state settles.
     *
     * Values are debounced, so a transient `AC` flicker on the way to
     * `WIRELESS` never reaches a collector.
     */
    val status: Flow<ChargingStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(intent.toChargingStatus())
            }
        }

        // The sticky broadcast gives us the current state without waiting for
        // the battery to change.
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )?.let { trySend(it.toChargingStatus()) }

        awaitClose { context.unregisterReceiver(receiver) }
    }
        .distinctUntilChanged()
        .debounce(SETTLE_MILLIS)

    /** The state right now, read from the sticky broadcast. Not debounced. */
    fun currentStatus(): ChargingStatus =
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            ?.toChargingStatus()
            ?: ChargingStatus.Unknown

    companion object {
        /**
         * How long a reading must hold before it counts. Covers the
         * manufacturer flicker described above without being noticeable.
         */
        const val SETTLE_MILLIS = 1_500L
    }
}

internal fun Intent.toChargingStatus(): ChargingStatus {
    val plugged = getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val status = getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)

    val level = getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percent = if (level >= 0 && scale > 0) level * 100 / scale else null

    return ChargingStatus(
        type = resolveChargeType(plugged, status),
        levelPercent = percent,
        voltageMilliVolts = getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1).takeIf { it > 0 },
        temperatureTenthsCelsius = getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            .takeIf { it != Int.MIN_VALUE },
    )
}

private fun resolveChargeType(plugged: Int, status: Int): ChargeType {
    // Power is flowing *out*, not in — reverse wireless charging. The plug
    // extra still says WIRELESS here, which is exactly the trap.
    val receivingPower = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL
    if (!receivingPower) return ChargeType.NONE

    return when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> ChargeType.WIRED_AC
        BatteryManager.BATTERY_PLUGGED_USB -> ChargeType.WIRED_USB
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargeType.WIRELESS
        DOCK_PLUG -> ChargeType.DOCK
        else -> ChargeType.NONE
    }
}

/**
 * `BatteryManager.BATTERY_PLUGGED_DOCK`, which only exists from API 33. Named
 * here so the value can be matched on older releases too — the system still
 * reports it on some docks before the constant was published.
 */
private const val DOCK_PLUG = 8
