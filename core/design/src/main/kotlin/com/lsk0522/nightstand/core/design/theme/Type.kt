package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Typography tokens - Design.md section 2.
 */
val NightstandFontFamily: FontFamily = Pretendard

/**
 * Tabular figures. Without this the clock jitters every second, because `1`
 * is narrower than `8` in a proportional face — Design.md §2.2 treats that as
 * a hard defect, not a nicety.
 */
private const val TABULAR = "tnum"

private val TightLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

object NightstandType {
    /** Two-row stacked hero clock. */
    val HeroClockDisplay = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 140.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 133.sp, // 140 * 0.95
        letterSpacing = (-0.04).em,
        fontFeatureSettings = TABULAR,
        lineHeightStyle = TightLineHeight,
    )

    /** Clock inside a split (two-panel) layout. */
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

    val DisplayTitle = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 34.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 41.sp,
        letterSpacing = (-0.02).em,
    )

    val Headline = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).em,
    )

    /** Body is 17sp, not 16sp — Design.md §7 calls this out explicitly. */
    val Body = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 25.sp,
        letterSpacing = (-0.02).em,
    )

    val BodyStrong = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        letterSpacing = (-0.02).em,
    )

    val Caption = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.sp,
    )

    /** Tab-bar labels, AM/PM. */
    val Footnote = TextStyle(
        fontFamily = NightstandFontFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 14.sp,
        letterSpacing = 0.02.em,
    )
}
