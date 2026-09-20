package com.lsk0522.nightstand.core.data.diagnostics

import android.content.Context
import com.lsk0522.nightstand.core.data.system.AppVersion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the last crash where it can be read back and sent on.
 *
 * Most of this app runs with nothing on screen — a foreground service, a
 * screen saver, an activity started from the background while the phone is
 * face down on a charger. A crash in any of those is invisible: the clock
 * simply does not appear, and by the time anyone looks, logcat has rotated
 * away.
 *
 * Deliberately local and account-free. A hosted crash reporter would be more
 * capable, but it needs a project set up, a dependency, and a line in the
 * privacy policy about sending data off the device. This needs none of those
 * and answers the only question that matters early on: what was the stack
 * trace? The user shares it if and when they choose.
 */
@Singleton
class CrashRecorder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appVersion: AppVersion,
) {
    private val file: File get() = File(context.filesDir, FILE_NAME)

    /**
     * Chains onto whatever handler is already installed.
     *
     * Replacing it outright would stop the process dying the way the system
     * expects, which breaks both the "app keeps stopping" dialog and any
     * reporter a future build adds.
     */
    fun install() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            // Anything thrown while recording must not replace the real crash.
            runCatching { record(thread, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    private fun record(thread: Thread, error: Throwable) {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        file.writeText(
            buildString {
                appendLine(stamp)
                appendLine(appVersion.display)
                appendLine(appVersion.deviceModel + " · " + appVersion.androidRelease)
                appendLine("thread: " + thread.name)
                appendLine()
                append(error.stackTraceToString())
            },
        )
    }

    /** The stored trace, or null when nothing has crashed since it was cleared. */
    fun lastCrash(): String? = runCatching {
        file.takeIf { it.exists() && it.length() > 0 }?.readText()
    }.getOrNull()

    fun clear() {
        runCatching { file.delete() }
    }

    private companion object {
        const val FILE_NAME = "last-crash.txt"
    }
}
