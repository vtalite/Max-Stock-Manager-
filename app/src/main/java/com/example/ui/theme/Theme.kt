package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode(val label: String) {
    SYSTEM("Automático"),
    LIGHT("Modo Claro"),
    DARK("Modo Escuro")
}

private val LightColorScheme = lightColorScheme(
    primary = WineBurgundyPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = WineBurgundyContainer,
    onPrimaryContainer = WineBurgundyDark,
    secondary = AccentLavender,
    onSecondary = AccentLavenderDark,
    secondaryContainer = AccentLavender,
    onSecondaryContainer = AccentLavenderDark,
    background = LightBackground,
    onBackground = TextPrimary,
    surface = LightSurface,
    onSurface = TextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OutlineBorder,
    error = StockRed,
    onError = androidx.compose.ui.graphics.Color.White,
    errorContainer = StockRedContainer,
    onErrorContainer = StockRed
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkWinePrimary,
    onPrimary = DarkWineOnPrimary,
    primaryContainer = DarkWinePrimaryContainer,
    onPrimaryContainer = DarkWineOnPrimaryContainer,
    secondary = androidx.compose.ui.graphics.Color(0xFFD0BCFF),
    onSecondary = androidx.compose.ui.graphics.Color(0xFF381E72),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF4F378B),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFEADDFF),
    background = DarkSurfaceBackground,
    onBackground = DarkTextHighContrast,
    surface = DarkSurfaceCard,
    onSurface = DarkTextHighContrast,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextMediumContrast,
    outline = androidx.compose.ui.graphics.Color(0xFF8A8590),
    outlineVariant = androidx.compose.ui.graphics.Color(0xFF49454F),
    error = androidx.compose.ui.graphics.Color(0xFFFFB4AB),
    onError = androidx.compose.ui.graphics.Color(0xFF690005),
    errorContainer = androidx.compose.ui.graphics.Color(0xFF93000A),
    onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6)
)

@Composable
fun AdegaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to keep wine brand aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = (if (darkTheme) colorScheme.surface else colorScheme.primary).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
