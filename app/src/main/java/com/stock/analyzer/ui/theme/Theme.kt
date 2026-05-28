package com.stock.analyzer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GfRed,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = GfLightGold,
    onPrimaryContainer = GfDarkRed,
    secondary = GfGold,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = GfGold,
    background = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
    onBackground = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color(0xFF1A1C1E),
    error = RedDown,
)

@Composable
fun StockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
