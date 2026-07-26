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
    tertiary = SenaSuccess,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = SenaDanger
)

private val LightColorScheme = lightColorScheme(
    primary = SenaGreen,
    secondary = SenaAccent,
    tertiary = SenaSuccess,
    background = SenaBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = SenaText,
    onSurface = SenaText,
    error = SenaDanger
)

// Local for manual dark mode control
val LocalThemeIsDark = staticCompositionLocalOf<MutableState<Boolean>> {
    error("No ThemeIsDark provided")
}

@Composable
fun ProyecTwinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkState = remember { mutableStateOf(darkTheme) }

    CompositionLocalProvider(LocalThemeIsDark provides isDarkState) {
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
