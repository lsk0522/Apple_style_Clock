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

/**
 * Whether the room is dark enough for the clock to go red.
 *
 * Two thresholds, not one. A single cut-off makes the screen flicker between
 * palettes every time a car goes past the window, because the reading sits
 * right on the line and crosses it repeatedly. Going dark takes a lower
 * reading than coming back does, so once it has settled it stays settled.
 *
 * The sensor is the slowest rate the platform offers: a room's light level is
 * not something that needs sampling faster than that, and this runs all night.
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

    /**
     * True while the room is dark.
     *
     * Emits false immediately and stays there when the phone has no light
     * sensor — a permanently red clock on hardware that cannot tell the
     * difference would be worse than none.
     */
    val isDark: Flow<Boolean> = run {
        val sensors = manager
        val light = sensors?.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (sensors == null || light == null) {
            flowOf(false)
        } else {
            callbackFlow {
                var dark = false
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val lux = event.values.firstOrNull() ?: return
                        dark = when {
                            lux <= DARK_LUX -> true
                            lux >= LIT_LUX -> false
                            // Between the two: hold whatever it already was.
                            else -> dark
                        }
                        trySend(dark)
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }
                sensors.registerListener(listener, light, SensorManager.SENSOR_DELAY_NORMAL)
                trySend(false)
                awaitClose { sensors.unregisterListener(listener) }
            }.distinctUntilChanged()
        }
    }

    private companion object {
        /** Roughly a room lit only by a streetlight through a curtain. */
        const val DARK_LUX = 10f

        /** A lamp switched on. Well clear of [DARK_LUX] so the two do not chatter. */
        const val LIT_LUX = 40f
    }
}
