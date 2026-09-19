package com.lsk0522.nightstand.core.design.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Motion tokens — Design.md §6.
 *
 * Nothing moves linearly: every transition runs on a damped spring so the UI
 * carries a sense of mass, the way a mechanical movement does.
 */
object NightstandMotion {

    /** Default spring for widget stacks, sheets and card transitions. */
    fun <T> standard(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

    /** Snappier spring for press feedback. */
    fun <T> press(): AnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
    )

    /** Scale a card or button shrinks to while held — Design.md §6. */
    const val PRESS_SCALE = 0.96f

    /** StandBy wake-up: wait, then ease the screen up from black. */
    val STANDBY_ENTER_DELAY = 2.seconds
    const val STANDBY_FADE_IN_MS = 800
    const val STANDBY_FADE_OUT_MS = 400

    fun <T> standbyFadeIn(): AnimationSpec<T> = tween(durationMillis = STANDBY_FADE_IN_MS)
    fun <T> standbyFadeOut(): AnimationSpec<T> = tween(durationMillis = STANDBY_FADE_OUT_MS)

    /** Burn-in protection: shift the whole canvas a couple of px, every 10 min. */
    val PIXEL_SHIFT_INTERVAL = 10.minutes
    const val PIXEL_SHIFT_MAX_DP = 2f
}
