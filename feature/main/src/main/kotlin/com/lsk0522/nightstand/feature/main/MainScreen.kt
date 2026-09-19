package com.lsk0522.nightstand.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lsk0522.nightstand.core.design.theme.NightstandTheme
import com.lsk0522.nightstand.core.design.theme.NightstandType
import com.lsk0522.nightstand.core.design.theme.Spacing

/**
 * Placeholder shell for the main screen.
 *
 * TODO(next): Phase 1 — replace with the five-section tab bar
 * (widgets / charging / main / developer / donate) and its navigation graph.
 */
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val palette = NightstandTheme.palette
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(palette.canvas),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            modifier = Modifier.padding(Spacing.screenMargin),
        ) {
            Text(
                text = "Nightstand",
                style = NightstandType.DisplayTitle,
                color = palette.textPrimary,
            )
            Text(
                text = "Phase 0 · 기초 공사 완료",
                style = NightstandType.Body,
                color = palette.textSecondary,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MainScreenPreview() {
    NightstandTheme { MainScreen() }
}
