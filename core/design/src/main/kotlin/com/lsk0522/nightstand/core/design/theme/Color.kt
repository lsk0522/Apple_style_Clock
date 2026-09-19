package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens from Design.md §4.
 *
 * The StandBy canvas is pure black on purpose: on the S25 Ultra's AMOLED panel
 * those pixels are fully off, which both saves power and prevents burn-in.
 */
object NightstandColor {
    // Primary & accents
    val Primary = Color(0xFF0066CC)
    val PrimaryFocus = Color(0xFF0071E3)
    val PrimaryOnDark = Color(0xFF2997FF)
    val AccentOrange = Color(0xFFFF9500)

    // Night-vision mode
    val NightRed = Color(0xFFFF453A)
    val NightRedDim = Color(0xFF801B17)

    // Canvas & surfaces
    val CanvasBlack = Color(0xFF000000)
    val SurfaceTile1 = Color(0xFF1C1C1E)
    val SurfaceTile2 = Color(0xFF2C2C2E)
    val SurfaceTile3 = Color(0xFF3A3A3C)
    val SurfaceGlass = Color(0xB31E1E23) // rgba(30,30,35,0.70)
    val TabBarGlass = Color(0xD91C1C1E) // rgba(28,28,30,0.85)

    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0x99FFFFFF) // 60%
    val TextTertiary = Color(0x4DFFFFFF) // 30%
    val TextQuaternary = Color(0x2EFFFFFF) // 18%

    // Hairlines
    val BorderGlassRim = Color(0x1FFFFFFF) // 12%
    val BorderHairline = Color(0x14FFFFFF) // 8%
}
