package com.lsk0522.nightstand.feature.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Re-arms the charging job after a restart.
 *
 * Unlike the power broadcasts, `ACTION_BOOT_COMPLETED` **is** on the implicit
 * broadcast exemption list, so this one genuinely does reach a manifest
 * receiver. The job is persisted as well, so this is a belt-and-braces measure
 * for phones that drop persisted jobs.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: ChargingJobScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        scheduler.schedule()
    }
}
