package com.aicompanion.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Pink40,
    onPrimary = Neutral100,
    primaryContainer = Pink80,
    onPrimaryContainer = Neutral10,
    secondary = Purple40,
    onSecondary = Neutral100,
    secondaryContainer = Purple80,
    onSecondaryContainer = Neutral10,
    tertiary = Coral40,
    onTertiary = Neutral100,
    tertiaryContainer = Coral80,
    onTertiaryContainer = Neutral10,
    error = ErrorRed,
    onError = Neutral100,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral30,
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink60,
    onPrimary = Neutral20,
    primaryContainer = Pink20,
    onPrimaryContainer = Pink80,
    secondary = Purple60,
    onSecondary = Neutral20,
    secondaryContainer = Purple20,
    onSecondaryContainer = Purple80,
    tertiary = Coral60,
    onTertiary = Neutral20,
    tertiaryContainer = Coral20,
    onTertiaryContainer = Coral80,
    error = ErrorRedDark,
    onError = Neutral20,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral90,
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF48454E),
)

@Composable
fun AiCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            // Dynamic color not used for this companion app — warm tones are part of the brand
            if (darkTheme) DarkColorScheme else LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
