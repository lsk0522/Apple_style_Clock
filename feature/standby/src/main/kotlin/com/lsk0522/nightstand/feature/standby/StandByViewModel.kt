package com.lsk0522.nightstand.feature.standby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StandByUiState(
    val use24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val nightMode: Boolean = true,
    val burnInProtection: Boolean = true,
    /**
     * Null until the first reading lands. The screen must not close on a
     * default value — that would dismiss itself the moment it opened.
     */
    val stillCharging: Boolean? = null,
    val batteryPercent: Int? = null,
    val chargeType: ChargeType = ChargeType.NONE,
)

@HiltViewModel
class StandByViewModel @Inject constructor(
    settings: SettingsRepository,
    monitor: ChargingStatusMonitor,
) : ViewModel() {

    val uiState: StateFlow<StandByUiState> =
        combine(settings.settings, monitor.status) { prefs, status ->
            StandByUiState(
                use24Hour = prefs.use24Hour,
                showSeconds = prefs.showSeconds,
                nightMode = prefs.nightMode,
                burnInProtection = prefs.burnInProtection,
                stillCharging = prefs.chargingTrigger.matches(status.type),
                batteryPercent = status.levelPercent,
                chargeType = status.type,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StandByUiState(),
        )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
