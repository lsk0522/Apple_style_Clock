package com.lsk0522.nightstand.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The palette the app UI draws from — semantic, not literal, so a screen never
 * names a hex value and light/dark comes out consistent.
 */
@Immutable
data class NightstandPalette(
    /** The ground a grouped list sits on. */
    val groupedBackground: Color,
    /** The card a list section is drawn on. */
    val groupedCard: Color,
    val rowPressed: Color,
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val quaternaryLabel: Color,
    val separator: Color,
    /** Navigation and tab bar material. */
    val bar: Color,
    val tint: Color,
    val positive: Color,
    val warning: Color,
    val destructive: Color,
    val switchTrackOff: Color,
    val fill: Color,
    val isDark: Boolean,
)

private val LightPalette = NightstandPalette(
    groupedBackground = NightstandColor.Ios.GroupedBgLight,
    groupedCard = NightstandColor.Ios.GroupedCardLight,
    rowPressed = NightstandColor.Ios.RowPressedLight,
    label = NightstandColor.Ios.LabelLight,
    secondaryLabel = NightstandColor.Ios.SecondaryLabelLight,
    tertiaryLabel = NightstandColor.Ios.TertiaryLabelLight,
    quaternaryLabel = NightstandColor.Ios.QuaternaryLabelLight,
    separator = NightstandColor.Ios.SeparatorLight,
    bar = NightstandColor.Ios.BarLight,
    tint = NightstandColor.Ios.BlueLight,
    positive = NightstandColor.Ios.GreenLight,
    warning = NightstandColor.Ios.OrangeLight,
    destructive = NightstandColor.Ios.RedLight,
    switchTrackOff = NightstandColor.Ios.SwitchTrackOffLight,
    fill = NightstandColor.Ios.FillLight,
    isDark = false,
)

private val DarkPalette = NightstandPalette(
    groupedBackground = NightstandColor.Ios.GroupedBgDark,
    groupedCard = NightstandColor.Ios.GroupedCardDark,
    rowPressed = NightstandColor.Ios.RowPressedDark,
    label = NightstandColor.Ios.LabelDark,
    secondaryLabel = NightstandColor.Ios.SecondaryLabelDark,
    tertiaryLabel = NightstandColor.Ios.TertiaryLabelDark,
    quaternaryLabel = NightstandColor.Ios.QuaternaryLabelDark,
    separator = NightstandColor.Ios.SeparatorDark,
    bar = NightstandColor.Ios.BarDark,
    tint = NightstandColor.Ios.BlueDark,
    positive = NightstandColor.Ios.GreenDark,
    warning = NightstandColor.Ios.OrangeDark,
    destructive = NightstandColor.Ios.RedDark,
    switchTrackOff = NightstandColor.Ios.SwitchTrackOffDark,
    fill = NightstandColor.Ios.FillDark,
    isDark = true,
)

/**
 * The StandBy clock surface — Design.md §4. Separate from the app palette on
 * purpose: this one never follows the system appearance.
 */
@Immutable
data class StandbyPalette(
    val canvas: Color,
    val tile: Color,
    val tileRaised: Color,
    val glass: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val hairline: Color,
    val glassRim: Color,
    val accent: Color,
)

private val StandbyStandard = StandbyPalette(
    canvas = NightstandColor.Standby.Canvas,
    tile = NightstandColor.Standby.Tile,
    tileRaised = NightstandColor.Standby.TileRaised,
    glass = NightstandColor.Standby.Glass,
    textPrimary = NightstandColor.Standby.TextPrimary,
    textSecondary = NightstandColor.Standby.TextSecondary,
    textTertiary = NightstandColor.Standby.TextTertiary,
    hairline = NightstandColor.Standby.Hairline,
    glassRim = NightstandColor.Standby.GlassRim,
    accent = NightstandColor.Standby.Accent,
)

private val StandbyNightVision = StandbyStandard.copy(
    textPrimary = NightstandColor.Standby.NightRed,
    textSecondary = NightstandColor.Standby.NightRedDim,
    textTertiary = NightstandColor.Standby.NightRedDim.copy(alpha = 0.5f),
    accent = NightstandColor.Standby.NightRed,
)

val LocalNightstandPalette = staticCompositionLocalOf { LightPalette }
val LocalStandbyPalette = staticCompositionLocalOf { StandbyStandard }

object NightstandTheme {
    /** App UI colours. */
    val palette: NightstandPalette
        @Composable @ReadOnlyComposable get() = LocalNightstandPalette.current

    /** Clock-surface colours. */
    val standby: StandbyPalette
        @Composable @ReadOnlyComposable get() = LocalStandbyPalette.current
}

/**
 * Wraps the app UI. Material3 is configured underneath only so that stock
 * components do not fall back to purple; screens read [NightstandTheme.palette].
 */
@Composable
fun NightstandTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    val material = if (darkTheme) {
        darkColorScheme(
            primary = palette.tint,
            background = palette.groupedBackground,
            surface = palette.groupedCard,
            onBackground = palette.label,
            onSurface = palette.label,
        )
    } else {
        lightColorScheme(
            primary = palette.tint,
            background = palette.groupedBackground,
            surface = palette.groupedCard,
            onBackground = palette.label,
            onSurface = palette.label,
        )
    }

    CompositionLocalProvider(
        LocalNightstandPalette provides palette,
        LocalStandbyPalette provides StandbyStandard,
    ) {
        MaterialTheme(colorScheme = material, content = content)
    }
}

/**
 * Wraps the StandBy clock surface. Always dark; [nightVision] swaps it to the
 * dark-adapted red palette when the light sensor says the room is dark.
 */
@Composable
fun StandbyTheme(
    nightVision: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalStandbyPalette provides if (nightVision) StandbyNightVision else StandbyStandard,
        content = content,
    )
}
