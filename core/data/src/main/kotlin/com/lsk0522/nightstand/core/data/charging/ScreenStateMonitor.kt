package com.lsk0522.nightstand.core.data.charging

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Whether the phone looks idle enough for the clock to take the screen. */
data class ScreenState(
    val isInteractive: Boolean,
    val isLocked: Boolean,
) {
    /**
     * Taking over the display while someone is reading a message would be
     * hostile. The clock waits for the phone to be put down.
     */
    val isIdle: Boolean get() = !isInteractive || isLocked
}

/**
 * Tracks whether the screen is on and whether the keyguard is up.
 *
 * `ACTION_SCREEN_ON` and `ACTION_SCREEN_OFF` cannot be declared in a manifest
 * at all — they are registered-at-runtime only — which is part of why a
 * component has to be alive to notice the right moment.
 */
@Singleton
class ScreenStateMonitor @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun current(): ScreenState {
        val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return ScreenState(
            isInteractive = power?.isInteractive ?: true,
            isLocked = keyguard?.isKeyguardLocked ?: false,
        )
    }

    val state: Flow<ScreenState> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(current())
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        trySend(current())
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()
}
