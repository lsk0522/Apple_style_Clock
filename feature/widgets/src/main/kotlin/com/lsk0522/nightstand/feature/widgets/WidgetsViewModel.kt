package com.lsk0522.nightstand.feature.widgets

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lsk0522.nightstand.core.data.widget.HostedWidget
import com.lsk0522.nightstand.core.data.widget.HostedWidgetStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetsViewModel @Inject constructor(
    private val store: HostedWidgetStore,
    val host: StandbyWidgetHost,
) : ViewModel() {

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

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
