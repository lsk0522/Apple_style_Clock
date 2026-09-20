package com.lsk0522.nightstand.feature.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hosts widgets belonging to other apps — the Samsung clock, weather and
 * calendar included — the way a launcher does.
 *
 * `BIND_APPWIDGET` is a signature permission no ordinary app can hold, so
 * binding goes through [bindIntent]: the system asks the user once, and after
 * that [AppWidgetManager.bindAppWidgetIdIfAllowed] succeeds on its own.
 *
 * The host must be listening while its views are on screen or the widgets
 * silently stop updating, so [startListening] and [stopListening] are tied to
 * the StandBy screen's lifecycle.
 */
@Singleton
class StandbyWidgetHost @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    private val host = AppWidgetHost(context, HOST_ID)

    /** Every widget installed on the phone, grouped by the app that owns it. */
    fun installedProviders(): List<AppWidgetProviderInfo> =
        manager.installedProviders.sortedWith(
            compareBy({ it.loadLabel(context.packageManager) }, { it.provider.className }),
        )

    fun allocateId(): Int = host.allocateAppWidgetId()

    /** Frees the id. Skipping this leaks widget ids for the life of the install. */
    fun releaseId(appWidgetId: Int) = host.deleteAppWidgetId(appWidgetId)

    fun isBound(appWidgetId: Int, provider: ComponentName): Boolean =
        manager.bindAppWidgetIdIfAllowed(appWidgetId, provider)

    /** The system's own "allow this app to add a widget?" prompt. */
    fun bindIntent(appWidgetId: Int, provider: ComponentName): Intent =
        Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

    fun providerInfo(appWidgetId: Int): AppWidgetProviderInfo? =
        manager.getAppWidgetInfo(appWidgetId)

    /**
     * Builds the view and tells the widget how much room it has.
     *
     * Without the size hint a widget lays itself out for its default cell and
     * looks wrong in a StandBy tile — usually too small, with its content
     * bunched in a corner.
     */
    fun createView(appWidgetId: Int, widthDp: Int, heightDp: Int): AppWidgetHostView? {
        val info = providerInfo(appWidgetId) ?: return null
        val view = host.createView(context, appWidgetId, info)
        view.setAppWidget(appWidgetId, info)
        applySize(view, widthDp, heightDp)
        return view
    }

    fun applySize(view: AppWidgetHostView, widthDp: Int, heightDp: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.updateAppWidgetSize(
                Bundle.EMPTY,
                listOf(SizeF(widthDp.toFloat(), heightDp.toFloat())),
            )
        } else {
            @Suppress("DEPRECATION")
            view.updateAppWidgetSize(Bundle.EMPTY, widthDp, heightDp, widthDp, heightDp)
        }
    }

    fun startListening() = runCatching { host.startListening() }

    fun stopListening() = runCatching { host.stopListening() }

    private companion object {
        /** Any stable non-zero value; it identifies this host to the system. */
        const val HOST_ID = 0x4E53 // "NS"
    }
}
