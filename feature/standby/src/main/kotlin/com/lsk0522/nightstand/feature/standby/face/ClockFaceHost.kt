package com.lsk0522.nightstand.feature.standby.face

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.lsk0522.nightstand.core.common.model.ClockFace

/**
 * Draws whichever face the user chose.
 *
 * The whole face is collapsed into one spoken line. Most of it is painted on a
 * canvas, so a screen reader would otherwise find either nothing at all or a
 * scatter of stray numerals from the dial — and what someone wants from a
 * clock is the time, said once.
 */
@Composable
fun ClockFaceHost(
    face: ClockFace,
    data: ClockFaceData,
    modifier: Modifier = Modifier,
) {
    val spoken = data.spokenSummary()
    val described = modifier.clearAndSetSemantics { contentDescription = spoken }

    when (face) {
        ClockFace.DIGITAL -> DigitalFace(data, described)
        ClockFace.ANALOG -> AnalogFace(data, described)
        ClockFace.WORLD -> WorldFace(data, described)
        ClockFace.SOLAR -> SolarFace(data, described)
        ClockFace.FLOAT -> FloatFace(data, described)
        ClockFace.MINIMAL_MONO -> MinimalMonoFace(data, described)
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
