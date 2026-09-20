package com.lsk0522.nightstand.core.data.standby

/**
 * Starts the StandBy screen.
 *
 * Declared here as an interface so the charging receiver can trigger StandBy
 * without `:feature:charging` having to depend on `:feature:standby`. The app
 * module, which knows about both, supplies the implementation.
 */
interface StandbyLauncher {
    /**
     * Brings up the clock.
     *
     * Only succeeds from the background while the overlay permission is held —
     * that permission is what exempts the app from Android 10's restriction on
     * background activity starts.
     */
    fun launch()
}
