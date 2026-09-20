package com.lsk0522.nightstand.core.design.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
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

    /**
     * Apple's standard ease, cubic-bezier(0.42, 0, 0.58, 1).
     *
     * For the transitions where a spring would overshoot into something that
     * has to stay legible -- type swapping under the reader, a whole pane
     * sliding -- this is the curve UIKit uses.
     */
    val StandardEase: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

    /** Cross-fade from one clock face to another. */
    const val FACE_SWAP_MS = 450

    fun <T> faceSwap(): FiniteAnimationSpec<T> =
        tween(durationMillis = FACE_SWAP_MS, easing = StandardEase)

    /** Switching sections in the app shell. */
    const val TAB_SWAP_MS = 260

    fun <T> tabSwap(): FiniteAnimationSpec<T> =
        tween(durationMillis = TAB_SWAP_MS, easing = StandardEase)

    /**
     * How far a pane travels while it fades, as a fraction of its width.
     *
     * Small on purpose: enough to say which way the section moved, not so far
     * that it reads as a push navigation, which means something else.
     */
    const val TAB_SLIDE_FRACTION = 0.07f

    /** Burn-in protection: shift the whole canvas a couple of px, every 10 min. */
    val PIXEL_SHIFT_INTERVAL = 10.minutes
    const val PIXEL_SHIFT_MAX_DP = 2f
}
