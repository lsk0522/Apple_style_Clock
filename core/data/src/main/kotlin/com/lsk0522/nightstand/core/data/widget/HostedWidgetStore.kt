package com.lsk0522.nightstand.core.data.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.lsk0522.nightstand.core.data.nightstandDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A widget the user added, as it needs to survive a restart.
 *
 * The provider is kept alongside the id because an id alone is meaningless
 * after a reboot — it has to be re-bound to the same provider to come back.
 */
data class HostedWidget(
    val appWidgetId: Int,
    val providerFlattened: String,
)

/**
 * Remembers which widgets are on the StandBy screen and in what order.
 *
 * Stored as a flat string rather than a typed structure because the list is
 * short and the alternative is a Proto schema for two fields.
 */
@Singleton
class HostedWidgetStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    val widgets: Flow<List<HostedWidget>> = context.nightstandDataStore.data.map { prefs ->
        prefs[Key]?.split(RECORD_SEPARATOR)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { record ->
                val (id, provider) = record.split(FIELD_SEPARATOR, limit = 2)
                    .takeIf { it.size == 2 } ?: return@mapNotNull null
                id.toIntOrNull()?.let { HostedWidget(it, provider) }
            }
            .orEmpty()
    }

    suspend fun save(widgets: List<HostedWidget>) {
        context.nightstandDataStore.edit { prefs ->
            prefs[Key] = widgets.joinToString(RECORD_SEPARATOR) {
                "${it.appWidgetId}$FIELD_SEPARATOR${it.providerFlattened}"
            }
        }
    }

    private companion object {
        val Key = stringPreferencesKey("hosted_widgets")
        const val RECORD_SEPARATOR = "|"
        const val FIELD_SEPARATOR = ";"
    }
}
