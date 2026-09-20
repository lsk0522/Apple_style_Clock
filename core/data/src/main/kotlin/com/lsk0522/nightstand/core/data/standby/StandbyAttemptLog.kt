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

    /**
     * The recent attempts, newest first.
     *
     * One entry was enough to find a dead receiver, but not to see a pattern —
     * "it works on the charger at my desk and not the one by the bed" is a
     * question about the last ten events, not the last one.
     */
    val history: Flow<List<StandbyAttempt>> = context.nightstandDataStore.data.map { prefs ->
        prefs[Keys.HISTORY]
            ?.split(RECORD_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull(::decode)
            .orEmpty()
            .asReversed()
    }

    suspend fun record(
        outcome: StandbyAttemptOutcome,
        detectedType: ChargeType,
        trigger: ChargingTrigger,
    ) {
        val attempt = StandbyAttempt(
            outcome = outcome,
            detectedType = detectedType,
            trigger = trigger,
            atEpochMillis = System.currentTimeMillis(),
        )
        context.nightstandDataStore.edit { prefs ->
            prefs[Keys.OUTCOME] = attempt.outcome.name
            prefs[Keys.TYPE] = attempt.detectedType.name
            prefs[Keys.TRIGGER] = attempt.trigger.name
            prefs[Keys.AT] = attempt.atEpochMillis

            // Oldest last in storage so appending is a string concat; the read
            // reverses it. Trimmed here rather than on read so the value
            // cannot grow without bound while the app is never opened.
            val kept = (prefs[Keys.HISTORY]?.split(RECORD_SEPARATOR)?.filter { it.isNotBlank() }
                .orEmpty() + encode(attempt))
                .takeLast(HISTORY_LIMIT)
            prefs[Keys.HISTORY] = kept.joinToString(RECORD_SEPARATOR)
        }
    }

    suspend fun clearHistory() {
        context.nightstandDataStore.edit { it.remove(Keys.HISTORY) }
    }

    private fun encode(attempt: StandbyAttempt): String = listOf(
        attempt.outcome.name,
        attempt.detectedType.name,
        attempt.trigger.name,
        attempt.atEpochMillis.toString(),
    ).joinToString(FIELD_SEPARATOR)

    private fun decode(record: String): StandbyAttempt? {
        val fields = record.split(FIELD_SEPARATOR)
        if (fields.size != 4) return null
        return StandbyAttempt(
            outcome = runCatching { StandbyAttemptOutcome.valueOf(fields[0]) }
                .getOrNull() ?: return null,
            detectedType = runCatching { ChargeType.valueOf(fields[1]) }
                .getOrNull() ?: return null,
            trigger = runCatching { ChargingTrigger.valueOf(fields[2]) }
                .getOrNull() ?: return null,
            atEpochMillis = fields[3].toLongOrNull() ?: return null,
        )
    }

    private object Keys {
        val OUTCOME = stringPreferencesKey("last_attempt_outcome")
        val TYPE = stringPreferencesKey("last_attempt_type")
        val TRIGGER = stringPreferencesKey("last_attempt_trigger")
        val AT = longPreferencesKey("last_attempt_at")
        val HISTORY = stringPreferencesKey("attempt_history")
    }

    private companion object {
        /** Enough to see a pattern; short enough to stay a preference value. */
        const val HISTORY_LIMIT = 30
        const val RECORD_SEPARATOR = "|"
        const val FIELD_SEPARATOR = ";"
    }
}
