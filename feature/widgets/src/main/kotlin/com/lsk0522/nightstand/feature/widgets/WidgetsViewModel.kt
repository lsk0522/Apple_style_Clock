package com.lsk0522.nightstand.feature.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.common.model.WidgetRotationInterval
import com.lsk0522.nightstand.core.data.settings.SettingsRepository
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.data.widget.HostedWidgetStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetsViewModel @Inject constructor(
    private val store: HostedWidgetStore,
    private val settings: SettingsRepository,
    val host: StandbyWidgetHost,
) : ViewModel() {

    val autoRotate: StateFlow<Boolean> = settings.settings
        .map { it.autoRotateWidgets }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = true,
        )

    fun setAutoRotate(value: Boolean) {
        viewModelScope.launch { settings.setAutoRotateWidgets(value) }
    }

    val rotationInterval: StateFlow<WidgetRotationInterval> = settings.settings
        .map { it.widgetRotationInterval }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = WidgetRotationInterval.Default,
        )

    fun setRotationInterval(value: WidgetRotationInterval) {
        viewModelScope.launch { settings.setWidgetRotationInterval(value) }
    }

    val widgets: StateFlow<List<HostedWidget>> = store.widgets.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList(),
    )

    fun installedProviders(): List<AppWidgetProviderInfo> = host.installedProviders()

    /**
     * Reserves an id for [provider] and reports whether it bound without
     * asking the user.
     *
     * The id is allocated first either way, because the system's consent
     * prompt needs one to attach the answer to.
     */
    fun prepareBinding(provider: ComponentName): Pair<Int, Boolean> {
        val id = host.allocateId()
        return id to host.isBound(id, provider)
    }

    fun confirmAdded(appWidgetId: Int, provider: ComponentName) {
        viewModelScope.launch {
            val current = store.widgets.first()
            if (current.any { it.appWidgetId == appWidgetId }) return@launch
            store.save(current + HostedWidget(appWidgetId, provider.flattenToString()))
        }
    }

    /** Releases the id as well; without that they leak for the life of the install. */
    fun remove(widget: HostedWidget) {
        viewModelScope.launch {
            store.save(store.widgets.first().filterNot { it.appWidgetId == widget.appWidgetId })
            host.releaseId(widget.appWidgetId)
        }
    }

    fun abandon(appWidgetId: Int) = host.releaseId(appWidgetId)

    /**
     * Moves one widget [by] places in the list.
     *
     * The stored order is what decides which slot a widget lands in on the
     * clock — they are dealt alternately into the two columns — so this is the
     * only way to say "put the weather on the right".
     */
    fun move(widget: HostedWidget, by: Int) {
        viewModelScope.launch {
            val current = store.widgets.first()
            val from = current.indexOfFirst { it.appWidgetId == widget.appWidgetId }
            if (from < 0) return@launch
            val to = (from + by).coerceIn(0, current.lastIndex)
            if (to == from) return@launch
            store.save(current.toMutableList().apply { add(to, removeAt(from)) })
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
