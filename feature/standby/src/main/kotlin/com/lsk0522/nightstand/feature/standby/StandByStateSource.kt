package com.lsk0522.nightstand.feature.standby

import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.sensor.AmbientLightMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.widget.HostedWidgetStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Everything the StandBy surface draws, in one flow.
 *
 * It lives outside the ViewModel because the screen has two hosts. The
 * activity is the main one; the dream is the fallback for anyone who would not
 * grant the overlay permission, and a `DreamService` is a Service — it has no
 * ViewModelStore to hang a ViewModel off. Keeping the assembly here means the
 * two hosts cannot drift into showing different things.
 */
@Singleton
class StandByStateSource @Inject constructor(
    settings: SettingsRepository,
    monitor: ChargingStatusMonitor,
    widgetStore: HostedWidgetStore,
    ambientLight: AmbientLightMonitor,
) {
    val state: Flow<StandByUiState> = combine(
        settings.settings,
        monitor.status,
        widgetStore.widgets,
        ambientLight.isDark,
        ambientLight.lux,
    ) { prefs, status, widgets, dark, lux ->
        StandByUiState(
            loaded = true,
            clockFace = prefs.clockFace,
            clockColor = prefs.clockColor,
            showDate = prefs.showDateOnClock,
            showBattery = prefs.showBatteryOnClock,
            use24Hour = prefs.use24Hour,
            showSeconds = prefs.showSeconds,
            nightMode = prefs.nightMode,
            nightVision = prefs.nightMode && dark,
            burnInProtection = prefs.burnInProtection,
            autoBrightness = prefs.autoBrightness,
            ambientLux = lux,
            stillCharging = prefs.chargingTrigger.matches(status.type),
            batteryPercent = status.levelPercent,
            chargeType = status.type,
            widgets = widgets,
            autoRotateWidgets = prefs.autoRotateWidgets,
            widgetRotationInterval = prefs.widgetRotationInterval,
        )
    }
}
