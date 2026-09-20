package com.lsk0522.nightstand.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Everything the user has chosen, persisted across process death. */
data class UserSettings(
    val chargingTrigger: ChargingTrigger = ChargingTrigger.WIRELESS_ONLY,
    val use24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val nightMode: Boolean = true,
    val burnInProtection: Boolean = true,
    val autoRotateWidgets: Boolean = true,
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
            use24Hour = prefs[Keys.USE_24_HOUR] ?: true,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: false,
            nightMode = prefs[Keys.NIGHT_MODE] ?: true,
            burnInProtection = prefs[Keys.BURN_IN_PROTECTION] ?: true,
            autoRotateWidgets = prefs[Keys.AUTO_ROTATE_WIDGETS] ?: true,
        )
    }

    suspend fun setChargingTrigger(trigger: ChargingTrigger) =
        edit { it[Keys.CHARGING_TRIGGER] = trigger.name }

    suspend fun setUse24Hour(value: Boolean) = edit { it[Keys.USE_24_HOUR] = value }

    suspend fun setShowSeconds(value: Boolean) = edit { it[Keys.SHOW_SECONDS] = value }

    suspend fun setNightMode(value: Boolean) = edit { it[Keys.NIGHT_MODE] = value }

    suspend fun setBurnInProtection(value: Boolean) =
        edit { it[Keys.BURN_IN_PROTECTION] = value }

    suspend fun setAutoRotateWidgets(value: Boolean) =
        edit { it[Keys.AUTO_ROTATE_WIDGETS] = value }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.nightstandDataStore.edit(block)
    }

    private object Keys {
        val CHARGING_TRIGGER = stringPreferencesKey("charging_trigger")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val NIGHT_MODE = booleanPreferencesKey("night_mode")
        val BURN_IN_PROTECTION = booleanPreferencesKey("burn_in_protection")
        val AUTO_ROTATE_WIDGETS = booleanPreferencesKey("auto_rotate_widgets")
    }
}
