package com.lsk0522.nightstand.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.common.model.ClockColor
import com.lsk0522.nightstand.core.common.model.ClockFace
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the user has chosen, persisted across process death. */
data class UserSettings(
    val chargingTrigger: ChargingTrigger = ChargingTrigger.WIRELESS_ONLY,
    val clockFace: ClockFace = ClockFace.Default,
    val clockColor: ClockColor = ClockColor.Default,
    val persistence: StandbyPersistence = StandbyPersistence.ONCE_PER_CHARGE,
    val showDateOnClock: Boolean = true,
    val showBatteryOnClock: Boolean = true,
    val use24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val nightMode: Boolean = true,
    val burnInProtection: Boolean = true,
    val autoRotateWidgets: Boolean = true,
    /** The first-run setup screen has been dismissed. */
    val setupSeen: Boolean = false,
)

@Singleton
class SettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val settings: Flow<UserSettings> = context.nightstandDataStore.data.map { prefs ->
        UserSettings(
            chargingTrigger = prefs[Keys.CHARGING_TRIGGER]
                ?.let { runCatching { ChargingTrigger.valueOf(it) }.getOrNull() }
                ?: ChargingTrigger.WIRELESS_ONLY,
            clockFace = prefs[Keys.CLOCK_FACE]
                ?.let { runCatching { ClockFace.valueOf(it) }.getOrNull() }
                ?: ClockFace.Default,
            clockColor = prefs[Keys.CLOCK_COLOR]
                ?.let { runCatching { ClockColor.valueOf(it) }.getOrNull() }
                ?: ClockColor.Default,
            persistence = prefs[Keys.PERSISTENCE]
                ?.let { runCatching { StandbyPersistence.valueOf(it) }.getOrNull() }
                ?: StandbyPersistence.ONCE_PER_CHARGE,
            showDateOnClock = prefs[Keys.SHOW_DATE] ?: true,
            showBatteryOnClock = prefs[Keys.SHOW_BATTERY] ?: true,
            use24Hour = prefs[Keys.USE_24_HOUR] ?: true,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: false,
            nightMode = prefs[Keys.NIGHT_MODE] ?: true,
            burnInProtection = prefs[Keys.BURN_IN_PROTECTION] ?: true,
            autoRotateWidgets = prefs[Keys.AUTO_ROTATE_WIDGETS] ?: true,
            setupSeen = prefs[Keys.SETUP_SEEN] ?: false,
        )
    }

    suspend fun setChargingTrigger(trigger: ChargingTrigger) =
        edit { it[Keys.CHARGING_TRIGGER] = trigger.name }

    suspend fun setClockFace(face: ClockFace) = edit { it[Keys.CLOCK_FACE] = face.name }

    suspend fun setClockColor(color: ClockColor) = edit { it[Keys.CLOCK_COLOR] = color.name }

    suspend fun setPersistence(value: StandbyPersistence) =
        edit { it[Keys.PERSISTENCE] = value.name }

    suspend fun setShowDateOnClock(value: Boolean) = edit { it[Keys.SHOW_DATE] = value }

    suspend fun setShowBatteryOnClock(value: Boolean) = edit { it[Keys.SHOW_BATTERY] = value }

    suspend fun setUse24Hour(value: Boolean) = edit { it[Keys.USE_24_HOUR] = value }

    suspend fun setShowSeconds(value: Boolean) = edit { it[Keys.SHOW_SECONDS] = value }

    suspend fun setNightMode(value: Boolean) = edit { it[Keys.NIGHT_MODE] = value }

    suspend fun setBurnInProtection(value: Boolean) =
        edit { it[Keys.BURN_IN_PROTECTION] = value }

    suspend fun setAutoRotateWidgets(value: Boolean) =
        edit { it[Keys.AUTO_ROTATE_WIDGETS] = value }

    suspend fun setSetupSeen(value: Boolean) = edit { it[Keys.SETUP_SEEN] = value }


    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.nightstandDataStore.edit(block)
    }

    private object Keys {
        val CHARGING_TRIGGER = stringPreferencesKey("charging_trigger")
        val CLOCK_FACE = stringPreferencesKey("clock_face")
        val CLOCK_COLOR = stringPreferencesKey("clock_color")
        val PERSISTENCE = stringPreferencesKey("standby_persistence")
        val SHOW_DATE = booleanPreferencesKey("show_date_on_clock")
        val SHOW_BATTERY = booleanPreferencesKey("show_battery_on_clock")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val NIGHT_MODE = booleanPreferencesKey("night_mode")
        val BURN_IN_PROTECTION = booleanPreferencesKey("burn_in_protection")
        val AUTO_ROTATE_WIDGETS = booleanPreferencesKey("auto_rotate_widgets")
        val SETUP_SEEN = booleanPreferencesKey("setup_seen")
    }
}
