package com.lsk0522.nightstand.feature.standby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.ChargeType
import com.lsk0522.nightstand.core.common.model.ClockColor
import com.lsk0522.nightstand.core.common.model.ClockFace
import com.lsk0522.nightstand.core.common.model.WidgetRotationInterval
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class StandByUiState(
    /**
     * False until the stored settings have actually been read.
     *
     * Without this the screen paints its defaults for a frame or two first, so
     * someone who chose Analog sees a flash of the Digital face before their
     * own choice arrives. The screen stays black until this turns true.
     */
    val loaded: Boolean = false,
    val clockFace: ClockFace = ClockFace.Default,
    val clockColor: ClockColor = ClockColor.Default,
    val showDate: Boolean = true,
    val showBattery: Boolean = true,
    val use24Hour: Boolean = true,
    val showSeconds: Boolean = false,
    val nightMode: Boolean = true,
    /**
     * Whether the clock should be drawing itself in the dark-adapted red.
     *
     * Both halves have to agree: the user allowed it, and the light sensor
     * says the room is actually dark.
     */
    val nightVision: Boolean = false,
    val burnInProtection: Boolean = true,
    val autoBrightness: Boolean = true,
    /** The room's light level, or null where it cannot be measured. */
    val ambientLux: Float? = null,
    /**
     * Null until the first reading lands. The screen must not close on a
     * default value — that would dismiss itself the moment it opened.
     */
    val stillCharging: Boolean? = null,
    val batteryPercent: Int? = null,
    val chargeType: ChargeType = ChargeType.NONE,
    val widgets: List<HostedWidget> = emptyList(),
    val autoRotateWidgets: Boolean = true,
    val widgetRotationInterval: WidgetRotationInterval = WidgetRotationInterval.Default,
)

@HiltViewModel
class StandByViewModel @Inject constructor(
    source: StandByStateSource,
    val widgetHost: StandbyWidgetHost,
) : ViewModel() {

    val uiState: StateFlow<StandByUiState> = source.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = StandByUiState(),
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
