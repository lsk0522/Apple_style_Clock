package com.lsk0522.nightstand.core.data.standby

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Why the last power event did or did not bring up the clock. */
enum class StandbyAttemptOutcome {
    /** The clock was started. */
    LAUNCHED,

    /** Charging did not match the rule the user chose. */
    TRIGGER_MISMATCH,

    /** Overlay permission missing, so a background start would be dropped. */
    NO_OVERLAY_PERMISSION,

    /** The start was attempted and the system refused it. */
    LAUNCH_FAILED,

    /** Power was pulled during the wait. */
    CANCELLED_EARLY,

    /** Someone is using the phone; the clock waits for the screen to go off. */
    SCREEN_IN_USE,

    /** The charger was unplugged. */
    DISCONNECTED,
}

data class StandbyAttempt(
    val outcome: StandbyAttemptOutcome,
    val detectedType: ChargeType,
    val trigger: ChargingTrigger,
    val atEpochMillis: Long,
)

/**
 * Records the outcome of every power event.
 *
 * The receiver runs with the app closed and the screen off, so when the clock
 * fails to appear there is otherwise nothing to look at — the failure is
 * silent and every possible cause looks identical from the outside. This turns
 * it into something the developer tab can read back.
 */
@Singleton
class StandbyAttemptLog @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val lastAttempt: Flow<StandbyAttempt?> = context.nightstandDataStore.data.map { prefs ->
        val outcome = prefs[Keys.OUTCOME]
            ?.let { runCatching { StandbyAttemptOutcome.valueOf(it) }.getOrNull() }
            ?: return@map null
        StandbyAttempt(
            outcome = outcome,
            detectedType = prefs[Keys.TYPE]
                ?.let { runCatching { ChargeType.valueOf(it) }.getOrNull() }
                ?: ChargeType.NONE,
            trigger = prefs[Keys.TRIGGER]
                ?.let { runCatching { ChargingTrigger.valueOf(it) }.getOrNull() }
                ?: ChargingTrigger.WIRELESS_ONLY,
            atEpochMillis = prefs[Keys.AT] ?: 0L,
        )
    }

    suspend fun record(
        outcome: StandbyAttemptOutcome,
        detectedType: ChargeType,
        trigger: ChargingTrigger,
    ) {
        context.nightstandDataStore.edit { prefs ->
            prefs[Keys.OUTCOME] = outcome.name
            prefs[Keys.TYPE] = detectedType.name
            prefs[Keys.TRIGGER] = trigger.name
            prefs[Keys.AT] = System.currentTimeMillis()
        }
    }

    private object Keys {
        val OUTCOME = stringPreferencesKey("last_attempt_outcome")
        val TYPE = stringPreferencesKey("last_attempt_type")
        val TRIGGER = stringPreferencesKey("last_attempt_trigger")
        val AT = longPreferencesKey("last_attempt_at")
    }
}
