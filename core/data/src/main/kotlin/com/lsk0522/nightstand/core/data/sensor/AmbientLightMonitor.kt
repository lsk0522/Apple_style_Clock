package com.lsk0522.nightstand.core.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold

/**
 * How light the room is, and whether that counts as dark.
 *
 * The sensor is read at the slowest rate the platform offers: a room's light
 * level does not need sampling faster than that, and this runs all night.
 */
@Singleton
class AmbientLightMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val manager: SensorManager?
        get() = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    /** False on a phone with no light sensor, so callers can say so. */
    val isAvailable: Boolean
        get() = manager?.getDefaultSensor(Sensor.TYPE_LIGHT) != null

    /** The raw reading in lux, or null on hardware that cannot measure it. */
    val lux: Flow<Float?> = run {
        val sensors = manager
        val light = sensors?.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensors == null || light == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        event.values.firstOrNull()?.let { trySend(it) }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                sensors.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL)
                awaitClose { sensors.unregisterListener(listener) }
            }.map { it }
        }
    }

    /**
     * True while the room is dark.
     *
     * Two thresholds, not one. A single cut-off makes the screen flicker
     * between palettes every time a car goes past the window, because the
     * reading sits right on the line and crosses it repeatedly. Going dark
     * takes a lower reading than coming back does, so once it has settled it
     * stays settled.
     *
     * Starts at false and stays there without a sensor — a permanently red
     * clock on hardware that cannot tell the difference would be worse than
     * none.
     */
    val isDark: Flow<Boolean> = lux
        .runningFold(false) { wasDark, reading ->
            when {
                reading == null -> false
                reading <= DARK_LUX -> true
                reading >= LIT_LUX -> false
                // Between the two: hold whatever it already was.
                else -> wasDark
            }
        }
        .distinctUntilChanged()

    companion object {
        /** Roughly a room lit only by a streetlight through a curtain. */
        const val DARK_LUX = 10f

        /** A lamp switched on. Well clear of [DARK_LUX] so the two do not chatter. */
        const val LIT_LUX = 40f
    }
}
