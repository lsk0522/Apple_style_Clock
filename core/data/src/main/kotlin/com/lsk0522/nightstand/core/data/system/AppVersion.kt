package com.lsk0522.nightstand.core.data.system

import android.content.Context
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The build actually running on the device.
 *
 * Read from the package manager rather than `BuildConfig` so any module can
 * ask, and so the answer is the installed APK's own metadata — which is the
 * whole point when the question is "am I looking at the build I just
 * downloaded?".
 */
@Singleton
class AppVersion @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /** e.g. `0.1.0-debug.42 (42)`. Empty only if the lookup somehow fails. */
    val display: String by lazy {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val code = PackageInfoCompat.getLongVersionCode(info)
            "${info.versionName} ($code)"
        }.getOrDefault("—")
    }

    val androidRelease: String get() = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"

    val deviceModel: String get() = "${Build.MANUFACTURER} ${Build.MODEL}"
}
