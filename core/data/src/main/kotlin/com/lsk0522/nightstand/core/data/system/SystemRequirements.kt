package com.lsk0522.nightstand.core.data.system

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** A system-level setting the app needs the user to turn on itself. */
enum class SystemRequirementId {
    /** Without this the clock cannot appear while another app is in front. */
    OVERLAY,

    /** Samsung in particular will put the app to sleep and never wake it. */
    BATTERY_OPTIMIZATION,

    /** Needed once the charging service posts its ongoing notification. */
    NOTIFICATIONS,
}

data class SystemRequirement(
    val id: SystemRequirementId,
    val isSatisfied: Boolean,
    /** Required items block the app from working at all; others degrade it. */
    val isRequired: Boolean,
)

/**
 * Reads whether the permissions and system settings the app depends on are in
 * place, and builds the intents that take the user straight to each one.
 *
 * None of these can be granted from inside the app — they all live in system
 * settings — so the job here is to check honestly and then get the user to the
 * exact screen rather than describing a path they have to hunt for.
 */
@Singleton
class SystemRequirements @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun canDrawOverlays(): Boolean = Settings.canDrawOverlays(context)

    fun isIgnoringBatteryOptimizations(): Boolean {
        val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return power?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // Before Android 13 notifications needed no runtime grant.
            true
        }

    fun check(): List<SystemRequirement> = buildList {
        add(
            SystemRequirement(
                id = SystemRequirementId.OVERLAY,
                isSatisfied = canDrawOverlays(),
                isRequired = true,
            ),
        )
        add(
            SystemRequirement(
                id = SystemRequirementId.BATTERY_OPTIMIZATION,
                isSatisfied = isIgnoringBatteryOptimizations(),
                isRequired = true,
            ),
        )
        add(
            SystemRequirement(
                id = SystemRequirementId.NOTIFICATIONS,
                isSatisfied = hasNotificationPermission(),
                isRequired = false,
            ),
        )
    }

    /**
     * The settings screen for [id], or null when it has to be handled as a
     * runtime permission request instead.
     *
     * Some of these actions are missing on some builds, so callers must be
     * ready for the start to fail and fall back.
     */
    fun settingsIntent(id: SystemRequirementId): Intent? = when (id) {
        SystemRequirementId.OVERLAY -> Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:${context.packageName}".toUri(),
        )

        SystemRequirementId.BATTERY_OPTIMIZATION -> Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            "package:${context.packageName}".toUri(),
        )

        SystemRequirementId.NOTIFICATIONS -> null // runtime permission
    }

    /** Used when [settingsIntent] is refused by the device. */
    fun fallbackIntent(id: SystemRequirementId): Intent = when (id) {
        SystemRequirementId.BATTERY_OPTIMIZATION ->
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

        else -> Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        )
    }
}
