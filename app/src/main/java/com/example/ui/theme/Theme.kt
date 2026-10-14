package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FuturisticPurpleColorScheme = darkColorScheme(
    primary = NeonPurpleBright,
    onPrimary = Color(0xFF1E0038),
    primaryContainer = NeonPurpleContainer,
    onPrimaryContainer = NeonPurpleOnContainer,
    secondary = NeonCyanSecondary,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = NeonCyanContainer,
    onSecondaryContainer = NeonCyanOnContainer,
    tertiary = NeonPinkTertiary,
    onTertiary = Color(0xFF4C0519),
    tertiaryContainer = NeonPinkContainer,
    onTertiaryContainer = NeonPinkOnContainer,
    background = FuturisticDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = FuturisticDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = FuturisticDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFE2E8F0),
    outline = FuturisticDarkOutline,
    outlineVariant = FuturisticDarkOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = FuturisticPurpleColorScheme,
        typography = Typography,
        content = content
    )
}
