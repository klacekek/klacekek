package com.example.heatertracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OrangePrimary,
    secondary = OrangePrimary,
    tertiary = ProfitGreen,
    background = Color(0xFF121212)
)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    secondary = OrangePrimary,
    tertiary = ProfitGreen,
    background = Color(0xFFFFFBFE)
)

@Composable
fun HeaterTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
