package com.lsk0522.nightstand.core.data.diagnostics

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** One stretch of the clock being on screen, and what the battery did meanwhile. */
data class StandbySession(
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long,
    val startPercent: Int,
    val endPercent: Int,
    /** True if a charger was attached for any of it. */
    val charging: Boolean,
) {
    val durationMillis: Long get() = endedAtEpochMillis - startedAtEpochMillis

    /**
     * Percent used per hour, or null when the answer would be noise.
     *
     * Two things make it noise. A session shorter than a few minutes cannot
     * resolve a battery reading that only moves in whole percent. And a
     * session on a charger measures the charger, not the clock — the number is
     * still shown, because "it charged anyway" is worth knowing, but it does
     * not belong in an average of what the clock costs.
     */
    val drainPerHour: Float?
        get() {
            if (charging) return null
            if (durationMillis < MINIMUM_USEFUL_MILLIS) return null
            val hours = durationMillis / 3_600_000f
            return (startPercent - endPercent) / hours
        }

    private companion object {
        const val MINIMUM_USEFUL_MILLIS = 5 * 60 * 1000L
    }
}

/**
 * Measures what StandBy actually costs.
 *
 * The plan set a target — under 5% an hour — and nothing was ever measured
 * against it, because measuring needs a phone and a stopwatch. This lets the
 * phone keep the stopwatch: every time the clock opens and closes, the
 * duration and the battery either side are written down.
 *
 * Reading it back is then just arithmetic, and the number is from the user's
 * own device rather than from a lab.
 */
@Singleton
class StandbySessionLog @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val monitor: ChargingStatusMonitor,
) {
    // Ends are reported from `onDestroy`, which cannot suspend, so the write
    // has to outlive the caller.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var openedAt: Long? = null
    private var openedAtPercent: Int? = null
    private var sawCharger = false

    val sessions: Flow<List<StandbySession>> = context.nightstandDataStore.data.map { prefs ->
        prefs[Key]
            ?.split(RECORD_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull(::decode)
            .orEmpty()
            .asReversed()
    }

    fun begin() {
        val status = monitor.currentStatus()
        openedAt = System.currentTimeMillis()
        openedAtPercent = status.levelPercent
        sawCharger = status.type.isCharging
    }

    fun end() {
        val startedAt = openedAt ?: return
        val startPercent = openedAtPercent
        openedAt = null
        openedAtPercent = null

        val status = monitor.currentStatus()
        val endPercent = status.levelPercent
        // Without both readings there is nothing to record; a session with a
        // made-up endpoint would be worse than a missing one.
        if (startPercent == null || endPercent == null) return

        val session = StandbySession(
            startedAtEpochMillis = startedAt,
            endedAtEpochMillis = System.currentTimeMillis(),
            startPercent = startPercent,
            endPercent = endPercent,
            charging = sawCharger || status.type.isCharging,
        )

        scope.launch {
            context.nightstandDataStore.edit { prefs ->
                val kept = (
                    prefs[Key]?.split(RECORD_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty() +
                        encode(session)
                    ).takeLast(LIMIT)
                prefs[Key] = kept.joinToString(RECORD_SEPARATOR)
            }
        }
    }

    suspend fun clear() {
        context.nightstandDataStore.edit { it.remove(Key) }
    }

    private fun encode(session: StandbySession): String = listOf(
        session.startedAtEpochMillis,
        session.endedAtEpochMillis,
        session.startPercent,
        session.endPercent,
        if (session.charging) 1 else 0,
    ).joinToString(FIELD_SEPARATOR)

    private fun decode(record: String): StandbySession? {
        val fields = record.split(FIELD_SEPARATOR)
        if (fields.size != 5) return null
        return StandbySession(
            startedAtEpochMillis = fields[0].toLongOrNull() ?: return null,
            endedAtEpochMillis = fields[1].toLongOrNull() ?: return null,
            startPercent = fields[2].toIntOrNull() ?: return null,
            endPercent = fields[3].toIntOrNull() ?: return null,
            charging = fields[4] == "1",
        )
    }

    private companion object {
        val Key = stringPreferencesKey("standby_sessions")
        const val LIMIT = 30
        const val RECORD_SEPARATOR = "|"
        const val FIELD_SEPARATOR = ";"
    }
}
