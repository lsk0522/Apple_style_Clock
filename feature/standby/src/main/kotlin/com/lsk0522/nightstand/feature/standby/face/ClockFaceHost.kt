package com.lsk0522.nightstand.feature.standby.face

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lsk0522.nightstand.core.common.model.ClockFace

/** Draws whichever face the user chose. */
@Composable
fun ClockFaceHost(
    face: ClockFace,
    data: ClockFaceData,
    modifier: Modifier = Modifier,
) {
    when (face) {
        ClockFace.DIGITAL -> DigitalFace(data, modifier)
        ClockFace.ANALOG -> AnalogFace(data, modifier)
        ClockFace.WORLD -> WorldFace(data, modifier)
        ClockFace.SOLAR -> SolarFace(data, modifier)
        ClockFace.FLOAT -> FloatFace(data, modifier)
        ClockFace.MINIMAL_MONO -> MinimalMonoFace(data, modifier)
    }
}

/**
 * Whether this face needs a tick every second.
 *
 * Only the analog sweep second hand does. Waking the others every second would
 * keep the panel from idling for no visible gain.
 */
fun ClockFace.needsSecondTicks(showSeconds: Boolean): Boolean =
    showSeconds && this == ClockFace.ANALOG
