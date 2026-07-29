package com.example.proyectwin.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

private val DarkColorScheme = darkColorScheme(
    primary = SenaGreen,
    secondary = SenaAccent,
    tertiary = Color(0xFF34D399),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0),
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = SenaGreen,
    secondary = SenaAccent,
    tertiary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    error = Color(0xFFEF4444)
)

// Local for manual dark mode control
val LocalThemeIsDark = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No ThemeIsDark provided")
}

// SenaColorScheme mirrors the frontend's CSS custom properties approach:
// All UI code reads via senaColors() and automatically adapts to dark/light.
val LocalSenaColorScheme = staticCompositionLocalOf<SenaColorScheme> {
    error("No SenaColorScheme provided")
}

@Composable
fun senaColors(): SenaColorScheme = LocalSenaColorScheme.current

@Composable
fun ProyecTwinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkState = remember { mutableStateOf(darkTheme) }

    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalSenaColorScheme provides if (isDarkState.value) darkSenaColors() else lightSenaColors()
    ) {
        val currentDark = LocalThemeIsDark.current.value
        val colorScheme = when {
            (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
                val context = LocalContext.current
                if (currentDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            currentDark -> DarkColorScheme
            else -> LightColorScheme
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
