package com.lsk0522.nightstand.core.data.charging

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The last power event the system woke us for. */
data class ChargingEvent(
    val type: ChargeType,
    val atEpochMillis: Long,
)

/**
 * Records power-connected events so the app can show that detection is working
 * **even when it was not running** — the receiver fires with the app closed,
 * and the charging screen reads the result back later.
 *
 * Until Phase 3 gives the detector something to launch, this is how the engine
 * proves itself on a real device.
 */
@Singleton
class ChargingEventStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val lastEvent: Flow<ChargingEvent?> = context.nightstandDataStore.data.map { prefs ->
        val type = prefs[Keys.TYPE]
            ?.let { runCatching { ChargeType.valueOf(it) }.getOrNull() }
            ?: return@map null
        val at = prefs[Keys.AT] ?: return@map null
        ChargingEvent(type, at)
    }

    suspend fun record(type: ChargeType, atEpochMillis: Long) {
        context.nightstandDataStore.edit { prefs ->
            prefs[Keys.TYPE] = type.name
            prefs[Keys.AT] = atEpochMillis
        }
    }

    private object Keys {
        val TYPE = stringPreferencesKey("last_event_type")
        val AT = longPreferencesKey("last_event_at")
    }
}
