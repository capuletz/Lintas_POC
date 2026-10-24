package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RadioDarkColorScheme = darkColorScheme(
    primary = RadioPttOrange,
    onPrimary = Color.White,
    primaryContainer = RadioSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = RadioLcdText,
    onSecondary = Color.Black,
    secondaryContainer = RadioLcdBg,
    onSecondaryContainer = RadioLcdText,
    tertiary = RadioLcdAmber,
    onTertiary = Color.Black,
    background = RadioDarkBg,
    onBackground = RadioTextPrimary,
    surface = RadioSurface,
    onSurface = RadioTextPrimary,
    surfaceVariant = RadioSurfaceVariant,
    onSurfaceVariant = RadioTextSecondary,
    outline = RadioBorder,
    error = RadioTxRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // For Osaka Q8 and HT walkie-talkie devices, always maintain high-contrast rugged dark theme
    MaterialTheme(
        colorScheme = RadioDarkColorScheme,
        typography = Typography,
        content = content
    )
}
