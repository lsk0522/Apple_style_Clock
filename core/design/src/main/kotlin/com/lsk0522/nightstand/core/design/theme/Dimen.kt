package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 8pt grid spacing tokens — Design.md §3.1. */
object Spacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp

    /** Safe distance from the bezel when the phone sits in a landscape dock. */
    val screenMargin: Dp = 32.dp
}

/**
 * Corner radius tokens — Design.md §3.2.
 *
 * Nested corners must follow the concentric rule: `inner = outer - padding`.
 * Use [concentric] rather than hand-picking a value, or the corner spacing
 * visibly warps.
 */
object Radius {
    val none: Dp = 0.dp
    val xs: Dp = 6.dp
    val sm: Dp = 10.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp

    fun concentric(outer: Dp, padding: Dp): Dp = (outer - padding).coerceAtLeast(0.dp)
}
