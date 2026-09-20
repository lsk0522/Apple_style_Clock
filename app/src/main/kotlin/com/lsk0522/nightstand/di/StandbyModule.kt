package com.lsk0522.nightstand.di

import android.content.Context
import android.content.Intent
import com.lsk0522.nightstand.core.data.standby.StandbyLauncher
import com.lsk0522.nightstand.feature.standby.StandByActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The app module is the only place that knows about both the charging feature
 * and the StandBy feature, so it is where the two are joined.
 */
@Singleton
class ActivityStandbyLauncher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : StandbyLauncher {

    override fun launch() {
        val intent = Intent(context, StandByActivity::class.java)
            // Launching from a receiver means there is no task to join.
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StandbyModule {
    @Binds
    abstract fun bindStandbyLauncher(impl: ActivityStandbyLauncher): StandbyLauncher
}
