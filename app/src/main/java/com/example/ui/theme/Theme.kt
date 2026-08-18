package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ImmersiveColorScheme =
  darkColorScheme(
    primary = Amber400,
    onPrimary = ObsidianBlack,
    primaryContainer = GlassCardBgLit,
    onPrimaryContainer = TextPrimary,
    secondary = NeonCyan,
    onSecondary = ObsidianBlack,
    secondaryContainer = GlassCardBg,
    onSecondaryContainer = TextPrimary,
    tertiary = Amber500,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = ObsidianElevated,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassFrostBorder,
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ImmersiveColorScheme,
    typography = Typography,
    content = content
  )
}

