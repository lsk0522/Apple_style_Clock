package com.lsk0522.nightstand.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Night mode swaps the palette to dark-adapted red so the screen does not
 * wreck night vision on a bedside table — Design.md §4.
 */
@Immutable
data class NightstandPalette(
    val canvas: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val glass: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val hairline: Color,
    val glassRim: Color,
    /** Translucent bar material sitting over the content behind it. */
    val tabBar: Color,
)

private val StandardPalette = NightstandPalette(
    canvas = NightstandColor.CanvasBlack,
    surface1 = NightstandColor.SurfaceTile1,
    surface2 = NightstandColor.SurfaceTile2,
    surface3 = NightstandColor.SurfaceTile3,
    glass = NightstandColor.SurfaceGlass,
    textPrimary = NightstandColor.TextPrimary,
    textSecondary = NightstandColor.TextSecondary,
    textTertiary = NightstandColor.TextTertiary,
    accent = NightstandColor.AccentOrange,
    hairline = NightstandColor.BorderHairline,
    glassRim = NightstandColor.BorderGlassRim,
    tabBar = NightstandColor.TabBarGlass,
)

private val NightVisionPalette = StandardPalette.copy(
    textPrimary = NightstandColor.NightRed,
    textSecondary = NightstandColor.NightRedDim,
    textTertiary = NightstandColor.NightRedDim.copy(alpha = 0.5f),
    accent = NightstandColor.NightRed,
)

val LocalNightstandPalette = staticCompositionLocalOf { StandardPalette }

/** Ambient-light driven night mode. Set by the StandBy screen, off elsewhere. */
val LocalNightVision = staticCompositionLocalOf { false }

object NightstandTheme {
    val palette: NightstandPalette
        @Composable @ReadOnlyComposable get() = LocalNightstandPalette.current
}

@Composable
fun NightstandTheme(
    nightVision: Boolean = false,
    content: @Composable () -> Unit,
) {
    val palette = if (nightVision) NightVisionPalette else StandardPalette

    // Material3 is only here so that stock components (ripples, text defaults)
    // do not fall back to purple; the real tokens live in NightstandPalette.
    val material = darkColorScheme(
        primary = NightstandColor.PrimaryOnDark,
        background = palette.canvas,
        surface = palette.surface1,
        onBackground = palette.textPrimary,
        onSurface = palette.textPrimary,
    )

    CompositionLocalProvider(
        LocalNightstandPalette provides palette,
        LocalNightVision provides nightVision,
    ) {
        MaterialTheme(colorScheme = material, content = content)
    }
}

/** True when the OS is in dark mode. The StandBy surface ignores this — it is always black. */
@Composable
fun systemIsDark(): Boolean = isSystemInDarkTheme()
