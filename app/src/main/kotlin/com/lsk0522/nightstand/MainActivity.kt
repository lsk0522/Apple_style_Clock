package com.lsk0522.nightstand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.feature.main.MainScreen
import com.lsk0522.nightstand.feature.charging.ChargingJobScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Arming the charging job here means a freshly installed app is ready the
     * first time it is opened, without waiting for a reboot.
     */
    @Inject
    lateinit var chargingJobScheduler: ChargingJobScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        chargingJobScheduler.schedule()
        setContent {
            NightstandTheme {
                MainScreen()
            }
        }
    }
}
