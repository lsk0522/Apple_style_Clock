package com.lsk0522.nightstand.core.design.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.lsk0522.nightstand.core.design.R

/**
 * Pretendard — the Android stand-in for SF Pro.
 *
 * SF Pro itself cannot ship here: its licence covers Apple platforms only.
 * Pretendard is OFL-licensed and was drawn to SF Pro's neo-grotesque
 * proportions, so the metrics and the Korean coverage both hold up
 * (Design.md §2.1).
 *
 * One variable file carries every weight, which keeps the APK to a single
 * ~6.7 MB asset instead of four static cuts.
 *
 * TODO(next): Phase 10 — subset the font to the glyphs actually used; the
 * full Korean range is most of that size.
 */
@OptIn(ExperimentalTextApi::class)
private fun pretendard(weight: FontWeight) = Font(
    resId = R.font.pretendard_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Pretendard: FontFamily = FontFamily(
    pretendard(FontWeight.Normal),
    pretendard(FontWeight.Medium),
    pretendard(FontWeight.SemiBold),
    pretendard(FontWeight.Bold),
)
