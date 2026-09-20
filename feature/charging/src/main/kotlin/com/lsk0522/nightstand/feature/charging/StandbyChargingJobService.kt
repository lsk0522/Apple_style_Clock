package com.lsk0522.nightstand.feature.charging

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Runs the moment the phone starts charging, and hands over to
 * [ChargingMonitorService].
 *
 * Deliberately short-lived. A job cannot sit and watch for the screen to turn
 * off, and it cannot be relied on to still be running minutes later, so its
 * only task is to start something that can — and to get out of the way.
 *
 * The pending job is cancelled while the monitor runs; the monitor schedules
 * it again once charging ends. Without that, rescheduling while still plugged
 * in would satisfy the charging constraint immediately and spin.
 */
@AndroidEntryPoint
class StandbyChargingJobService : JobService() {

    @Inject lateinit var scheduler: ChargingJobScheduler

    override fun onStartJob(params: JobParameters?): Boolean {
        scheduler.cancel()
        ContextCompat.startForegroundService(
            this,
            Intent(this, ChargingMonitorService::class.java),
        )
        // Nothing left to do on this thread.
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        // Ask to be run again; the charge event has not been handled.
        return true
    }
}
