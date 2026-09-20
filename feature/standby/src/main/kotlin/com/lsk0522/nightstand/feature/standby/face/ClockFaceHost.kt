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
 * No face prints a seconds digit, so the shared clock never needs to wake more
 * than once a minute.
 *
 * The analog second hand is the one thing that moves faster, and it drives
 * itself from the frame clock inside [AnalogFace] — which keeps that cost
 * confined to the one face that asked for it.
 */
fun ClockFace.needsSecondTicks(showSeconds: Boolean): Boolean = false
