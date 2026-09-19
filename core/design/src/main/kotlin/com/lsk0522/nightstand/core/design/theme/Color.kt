package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Colour tokens.
 *
 * Two distinct systems live here, and mixing them is what makes an app stop
 * looking like iOS:
 *
 *  - [Ios] — the semantic palette the *app UI* uses (settings lists, tab bar,
 *    navigation). It follows the system light/dark appearance, exactly as
 *    UIKit's semantic colours do.
 *  - [Standby] — the *clock screen* palette from Design.md §4. Always pure
 *    black, never light, because those pixels are switched off on AMOLED.
 */
object NightstandColor {

    /** UIKit semantic colours, light and dark. */
    object Ios {
        // ── Tints ────────────────────────────────────────────────────────
        val BlueLight = Color(0xFF007AFF)
        val BlueDark = Color(0xFF0A84FF)
        val GreenLight = Color(0xFF34C759)
        val GreenDark = Color(0xFF30D158)
        val OrangeLight = Color(0xFFFF9500)
        val OrangeDark = Color(0xFFFF9F0A)
        val RedLight = Color(0xFFFF3B30)
        val RedDark = Color(0xFFFF453A)

        // ── Grouped backgrounds (the ground a settings list sits on) ─────
        val GroupedBgLight = Color(0xFFF2F2F7)
        val GroupedBgDark = Color(0xFF000000)

        /** The card a list section is drawn on. */
        val GroupedCardLight = Color(0xFFFFFFFF)
        val GroupedCardDark = Color(0xFF1C1C1E)

        /** A row being pressed. */
        val RowPressedLight = Color(0xFFD1D1D6)
        val RowPressedDark = Color(0xFF2C2C2E)

        // ── Labels ───────────────────────────────────────────────────────
        val LabelLight = Color(0xFF000000)
        val LabelDark = Color(0xFFFFFFFF)

        // iOS tints its secondary labels toward the ground rather than using
        // plain grey, which is why they read warm on white and cool on black.
        val SecondaryLabelLight = Color(0x993C3C43) // rgba(60,60,67,.60)
        val SecondaryLabelDark = Color(0x99EBEBF5) // rgba(235,235,245,.60)
        val TertiaryLabelLight = Color(0x4D3C3C43) // .30
        val TertiaryLabelDark = Color(0x4DEBEBF5)
        val QuaternaryLabelLight = Color(0x2E3C3C43) // .18
        val QuaternaryLabelDark = Color(0x2EEBEBF5)

        // ── Separators ───────────────────────────────────────────────────
        val SeparatorLight = Color(0x4A3C3C43) // rgba(60,60,67,.29)
        val SeparatorDark = Color(0xA6545458) // rgba(84,84,88,.65)

        // ── Bar materials (nav bar, tab bar) ─────────────────────────────
        val BarLight = Color(0xF0F9F9F9)
        val BarDark = Color(0xF0161618)

        /** Off state of a switch. */
        val SwitchTrackOffLight = Color(0xFFE9E9EA)
        val SwitchTrackOffDark = Color(0xFF39393D)

        /** Fill used for inline controls such as segmented backgrounds. */
        val FillLight = Color(0x1F787880)
        val FillDark = Color(0x2E787880)

        // ── Liquid Glass (iOS 26, tuned to the iOS 27 revision) ──────────
        //
        // iOS 27 pulled the default transparency back from iOS 26 because the
        // original was hard to read over busy content, so these fills are
        // closer to opaque than the first Liquid Glass release.
        val GlassLight = Color(0xB8FFFFFF) // ~72% white
        val GlassDark = Color(0xB81E1E20) // ~72% near-black

        /** Bright rim along the lit (top) edge — the specular highlight. */
        val GlassSpecularLight = Color(0xA6FFFFFF)
        val GlassSpecularDark = Color(0x47FFFFFF)

        /** The darkened outer edge iOS 27 added to separate glass from content. */
        val GlassEdgeLight = Color(0x2E000000)
        val GlassEdgeDark = Color(0x73000000)
    }

    /** The StandBy clock canvas — Design.md §4. Dark only, by design. */
    object Standby {
        val Canvas = Color(0xFF000000)
        val Tile = Color(0xFF1C1C1E)
        val TileRaised = Color(0xFF2C2C2E)
        val Glass = Color(0xB31E1E23)

        val TextPrimary = Color(0xFFFFFFFF)
        val TextSecondary = Color(0x99FFFFFF)
        val TextTertiary = Color(0x4DFFFFFF)

        val Hairline = Color(0x14FFFFFF)
        val GlassRim = Color(0x1FFFFFFF)

        val Accent = Color(0xFFFF9F0A)

        /** Night-vision mode — dark-adapted red. */
        val NightRed = Color(0xFFFF453A)
        val NightRedDim = Color(0xFF801B17)
    }
}
