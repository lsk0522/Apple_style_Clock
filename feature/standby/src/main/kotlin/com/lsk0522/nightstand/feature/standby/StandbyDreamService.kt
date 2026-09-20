package com.lsk0522.nightstand.feature.standby

import android.service.dreams.DreamService
import android.view.WindowManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.lsk0522.nightstand.core.data.diagnostics.StandbySessionLog
import com.lsk0522.nightstand.core.design.theme.StandbyTheme
import com.lsk0522.nightstand.feature.widgets.StandbyWidgetHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The same clock, shown as the system screen saver.
 *
 * This is the fallback path. Starting an activity from the background needs
 * the overlay permission, and that is a permission people reasonably refuse —
 * it is the one malware asks for. A dream needs no permission at all: the user
 * turns it on in the system's own screen-saver settings, and Android starts it
 * while the phone is charging and idle. Slower to reach and it cannot decide
 * *when* to appear, which is why it is second rather than first.
 *
 * A `DreamService` is a Service, so none of the owners Compose expects are
 * there. [dreamLifecycle] supplies them by hand; without them `ComposeView`
 * throws on attach rather than rendering.
 */
@AndroidEntryPoint
class StandbyDreamService : DreamService() {

    @Inject lateinit var stateSource: StandByStateSource

    @Inject lateinit var widgetHost: StandbyWidgetHost

    @Inject lateinit var sessionLog: StandbySessionLog

    private val dreamLifecycle = DreamOwners()

    override fun onCreate() {
        super.onCreate()
        dreamLifecycle.create()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Taps have to reach us: one dims, two exit. A non-interactive dream
        // hands the first touch straight to the system, which wakes the phone.
        isInteractive = true
        isFullscreen = true
        // The clock supplies its own dimming, the same as the activity does.
        isScreenBright = true

        val view = ComposeView(this).apply {
            setContent {
                val state by stateSource.state.collectAsState(initial = StandByUiState())
                var dimmed by remember { mutableStateOf(true) }
                LaunchedEffect(dimmed) { applyBrightness(dimmed) }

                StandbyTheme(nightVision = state.nightVision) {
                    StandByScreen(
                        state = state,
                        host = widgetHost,
                        onSingleTap = { dimmed = !dimmed },
                        onExit = { finish() },
                    )
                }
            }
        }

        view.setViewTreeLifecycleOwner(dreamLifecycle)
        view.setViewTreeSavedStateRegistryOwner(dreamLifecycle)
        setContentView(view)
    }

    /** Same trade as the activity: readable in a dark room without lighting it. */
    private fun applyBrightness(dimmed: Boolean) {
        val target = window ?: return
        target.attributes = target.attributes.apply {
            screenBrightness = if (dimmed) {
                DIM_BRIGHTNESS
            } else {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
        }
    }

    override fun onDreamingStarted() {

        super.onDreamingStarted()
        sessionLog.begin()
        dreamLifecycle.resume()
    }

    override fun onDreamingStopped() {
        sessionLog.end()
        dreamLifecycle.pause()
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        dreamLifecycle.destroy()
        super.onDestroy()
    }

    private companion object {
        const val DIM_BRIGHTNESS = 0.25f
    }
}

/**
 * The lifecycle and saved-state registry an Activity would have provided.
 *
 * Deliberately minimal: the dream is torn down rather than reconfigured, so
 * nothing ever has to be restored. The registry exists only because Compose
 * looks one up before it will attach.
 */
private class DreamOwners : LifecycleOwner, SavedStateRegistryOwner {

    private val registry = LifecycleRegistry(this)
    private val controller = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = registry

    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

    fun create() {
        controller.performRestore(null)
        registry.currentState = Lifecycle.State.CREATED
    }

    fun resume() {
        registry.currentState = Lifecycle.State.RESUMED
    }

    fun pause() {
        registry.currentState = Lifecycle.State.CREATED
    }

    fun destroy() {
        registry.currentState = Lifecycle.State.DESTROYED
    }
}
