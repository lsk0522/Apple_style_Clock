package com.lsk0522.nightstand.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.ChargingStatus
import com.lsk0522.nightstand.core.common.model.ChargingTrigger
import com.lsk0522.nightstand.core.data.charging.ChargingEvent
import com.lsk0522.nightstand.core.data.charging.ChargingEventStore
import com.lsk0522.nightstand.core.data.charging.ChargingStatusMonitor
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChargingUiState(
    val trigger: ChargingTrigger = ChargingTrigger.WIRELESS_ONLY,
    val status: ChargingStatus = ChargingStatus.Unknown,
    /** The last event the receiver caught, possibly while the app was closed. */
    val lastEvent: ChargingEvent? = null,
) {
    /** Whether the current charging would start StandBy under the chosen rule. */
    val wouldTrigger: Boolean get() = trigger.matches(status.type)
}

@HiltViewModel
class ChargingViewModel @Inject constructor(
    private val repository: SettingsRepository,
    monitor: ChargingStatusMonitor,
    eventStore: ChargingEventStore,
) : ViewModel() {

    val uiState: StateFlow<ChargingUiState> =
        combine(
            repository.settings.map { it.chargingTrigger },
            monitor.status,
            eventStore.lastEvent,
        ) { trigger, status, lastEvent -> ChargingUiState(trigger, status, lastEvent) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                // The monitored flow is debounced, so it stays quiet for a
                // moment after subscribing. Seed with a direct read so the
                // screen is never blank on open.
                initialValue = ChargingUiState(status = monitor.currentStatus()),
            )

    fun setTrigger(trigger: ChargingTrigger) {
        viewModelScope.launch { repository.setChargingTrigger(trigger) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
