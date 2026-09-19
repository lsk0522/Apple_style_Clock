package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

val NightstandFontFamily: FontFamily = Pretendard

/**
 * Tabular figures. Without this the clock jitters every second, because `1`
 * is narrower than `8` in a proportional face — Design.md §2.2 treats that as
 * a defect, not a nicety.
 */
private const val TABULAR = "tnum"

private val TightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * The iOS text styles, with Apple's own tracking table.
 *
 * The sign flip is the part that matters and the part everyone gets wrong:
 * SF Pro *loosens* at display sizes (+0.37pt at 34pt) and *tightens* at reading
 * sizes (-0.41pt at 17pt). Applying one blanket negative tracking is what makes
 * an imitation read as "almost, but not quite".
 */
object NightstandType {

    // ── Display / titles ────────────────────────────────────────────────
    /** Navigation large title. */
    val LargeTitle = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 41.sp,
        letterSpacing = 0.0109.em, // +0.37pt
    )

    val Title1 = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.sp,
        letterSpacing = 0.0129.em,
    )

    val Title2 = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp,
        letterSpacing = 0.0159.em,
    )

    val Title3 = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 25.sp,
        letterSpacing = 0.019.em,
    )

    // ── Text ────────────────────────────────────────────────────────────
    /** Collapsed navigation-bar title, emphasised row labels. */
    val Headline = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        letterSpacing = (-0.024).em, // -0.41pt
    )

    /** The default. 17sp, never 16 — Design.md §7. */
    val Body = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 22.sp,
        letterSpacing = (-0.024).em,
    )

    val Callout = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.sp,
        letterSpacing = (-0.019).em,
    )

    val Subheadline = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = (-0.016).em,
    )

    /** Grouped-list section headers and footers. */
    val Footnote = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
        letterSpacing = (-0.006).em,
    )

    val Caption1 = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
    )

    /** Tab-bar labels. */
    val Caption2 = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 13.sp,
        letterSpacing = 0.006.em,
    )

    // ── Clock faces — Design.md §2.3 ────────────────────────────────────
    /** Two-row stacked hero clock. */
    val HeroClockDisplay = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 140.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 133.sp,
        letterSpacing = (-0.04).em,
        fontFeatureSettings = TABULAR,
        lineHeightStyle = TightLineHeight,
    )

    /** Clock inside a split two-panel layout. */
    val HeroClockMedium = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 88.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 88.sp,
        letterSpacing = (-0.03).em,
        fontFeatureSettings = TABULAR,
        lineHeightStyle = TightLineHeight,
    )

    /** Clock rendered inside a widget tile. */
    val WidgetClock = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 52.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 55.sp,
        letterSpacing = (-0.02).em,
        fontFeatureSettings = TABULAR,
        lineHeightStyle = TightLineHeight,
    )
}
