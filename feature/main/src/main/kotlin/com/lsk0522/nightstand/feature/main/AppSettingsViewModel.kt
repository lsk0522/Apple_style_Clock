package com.lsk0522.nightstand.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.ClockColor
import com.lsk0522.nightstand.core.common.model.ClockFace
import com.lsk0522.nightstand.core.common.model.StandbyPersistence
import com.lsk0522.nightstand.core.data.sensor.AmbientLightMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.settings.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Backs the switches on the settings and widgets tabs. */
@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    ambientLight: AmbientLightMonitor,
) : ViewModel() {

    /**
     * Whether this phone can tell a dark room from a lit one.
     *
     * Offering the night-mode switch on hardware with no light sensor would
     * be offering a switch that can never fire.
     */
    val lightSensorAvailable: Boolean = ambientLight.isAvailable

    val settings: StateFlow<UserSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UserSettings(),
    )

    fun setClockFace(face: ClockFace) = update { repository.setClockFace(face) }
    fun setClockColor(color: ClockColor) = update { repository.setClockColor(color) }
    fun setPersistence(value: StandbyPersistence) = update { repository.setPersistence(value) }
    fun setShowDateOnClock(value: Boolean) = update { repository.setShowDateOnClock(value) }

    fun setShowBatteryOnClock(value: Boolean) =
        update { repository.setShowBatteryOnClock(value) }

    fun setUse24Hour(value: Boolean) = update { repository.setUse24Hour(value) }
    fun setShowSeconds(value: Boolean) = update { repository.setShowSeconds(value) }
    fun setNightMode(value: Boolean) = update { repository.setNightMode(value) }
    fun setBurnInProtection(value: Boolean) = update { repository.setBurnInProtection(value) }
    fun setAutoRotateWidgets(value: Boolean) = update { repository.setAutoRotateWidgets(value) }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
