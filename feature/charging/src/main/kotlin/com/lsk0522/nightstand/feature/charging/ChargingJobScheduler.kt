package com.lsk0522.nightstand.feature.charging

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arranges to be woken when the phone starts charging.
 *
 * This replaces a manifest receiver for `ACTION_POWER_CONNECTED`, which
 * **never fires on Android 8 or later** — that action is not on the implicit
 * broadcast exemption list, so a statically registered receiver for it is
 * silently dead. A job with a charging constraint is the supported way to hear
 * about it while the app is closed.
 */
@Singleton
class ChargingJobScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val scheduler: JobScheduler?
        get() = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler

    /** Idempotent: rescheduling replaces the pending job rather than stacking. */
    fun schedule() {
        val job = JobInfo.Builder(
            JOB_ID,
            ComponentName(context, StandbyChargingJobService::class.java),
        )
            .setRequiresCharging(true)
            // Survives a reboot, so the clock still works on a phone that was
            // restarted and never reopened.
            .setPersisted(true)
            .build()
        scheduler?.schedule(job)
    }

    fun cancel() {
        scheduler?.cancel(JOB_ID)
    }

    private companion object {
        const val JOB_ID = 1001
    }
}
