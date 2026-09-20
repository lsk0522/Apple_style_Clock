package com.lsk0522.nightstand

import android.app.Application
import com.lsk0522.nightstand.core.data.diagnostics.CrashRecorder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NightstandApplication : Application() {

    @Inject lateinit var crashRecorder: CrashRecorder

    override fun onCreate() {
        super.onCreate()
        // Installed first thing: the components most likely to crash are the
        // ones that start without the app being opened, and they can be
        // running before anything else here would have had a chance to.
        crashRecorder.install()
    }
}
