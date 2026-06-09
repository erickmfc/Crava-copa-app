package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    secondary = NeonYellow,
    tertiary = NeonLightGreen,
    background = BackgroundMidnight,
    surface = SurfaceDeep,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = TextMuted,
    outline = BorderNavy
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for CravaCopa
    dynamicColor: Boolean = false, // Disable to stick completely to CravaCopa colors
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    // Use our custom dark theme styling for status bar as well
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
