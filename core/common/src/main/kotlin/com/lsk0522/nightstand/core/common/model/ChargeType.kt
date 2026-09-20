package com.lsk0522.nightstand.core.common.model

/** How the phone is currently receiving power. */
enum class ChargeType {
    /** Not charging, or charging could not be determined. */
    NONE,

    /** A mains charger over the cable. */
    WIRED_AC,

    /** A computer's USB port. Slower, but still wired. */
    WIRED_USB,

    /** A Qi pad. */
    WIRELESS,

    /** A dock. Only reported from API 33; inferred below that. */
    DOCK,
    ;

    val isWired: Boolean get() = this == WIRED_AC || this == WIRED_USB
    val isCharging: Boolean get() = this != NONE
}

/** Which kind of charging the user wants to wake the clock. */
enum class ChargingTrigger {
    WIRELESS_ONLY,
    WIRED_ONLY,
    ANY,
    DOCK_ONLY,
    ;

    /** Whether [type] should start StandBy under this setting. */
    fun matches(type: ChargeType): Boolean = when (this) {
        WIRELESS_ONLY -> type == ChargeType.WIRELESS
        WIRED_ONLY -> type.isWired
        ANY -> type.isCharging
        DOCK_ONLY -> type == ChargeType.DOCK
    }
}

/**
 * A snapshot of the battery, as the system last reported it.
 *
 * @param temperatureTenthsCelsius the unit `BatteryManager` uses — 253 means 25.3°C.
 */
data class ChargingStatus(
    val type: ChargeType,
    val levelPercent: Int?,
    val voltageMilliVolts: Int?,
    val temperatureTenthsCelsius: Int?,
) {
    val temperatureCelsius: Float?
        get() = temperatureTenthsCelsius?.let { it / 10f }

    companion object {
        val Unknown = ChargingStatus(
            type = ChargeType.NONE,
            levelPercent = null,
            voltageMilliVolts = null,
            temperatureTenthsCelsius = null,
        )
    }
}
