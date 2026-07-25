package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TealDarkPrimary,
    onPrimary = TealDarkOnPrimary,
    primaryContainer = TealDarkPrimaryContainer,
    onPrimaryContainer = TealDarkOnPrimaryContainer,
    secondary = TealDarkSecondary,
    onSecondary = TealDarkOnSecondary,
    secondaryContainer = TealDarkSecondaryContainer,
    onSecondaryContainer = TealDarkOnSecondaryContainer,
    background = TealDarkBackground,
    onBackground = TealDarkOnBackground,
    surface = TealDarkSurface,
    onSurface = TealDarkOnSurface,
    surfaceVariant = TealDarkSurfaceVariant,
    onSurfaceVariant = TealDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = TealLightPrimary,
    onPrimary = TealLightOnPrimary,
    primaryContainer = TealLightPrimaryContainer,
    onPrimaryContainer = TealLightOnPrimaryContainer,
    secondary = TealLightSecondary,
    onSecondary = TealLightOnSecondary,
    secondaryContainer = TealLightSecondaryContainer,
    onSecondaryContainer = TealLightOnSecondaryContainer,
    background = TealLightBackground,
    onBackground = TealLightOnBackground,
    surface = TealLightSurface,
    onSurface = TealLightOnSurface,
    surfaceVariant = TealLightSurfaceVariant,
    onSurfaceVariant = TealLightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "CLARO",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isDark = themeMode.equals("OSCURO", ignoreCase = true)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
