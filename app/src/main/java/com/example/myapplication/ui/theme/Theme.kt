package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.preferences.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = AccentGreen,
    onSecondary = Color.Black,
    tertiary = AccentGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = ElevatedDark,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    surfaceContainer = CardDark,
    surfaceContainerHigh = ElevatedDark,
)

private val LightColorScheme = lightColorScheme(
    primary = AccentGreen,
    onPrimary = Color.White,
    secondary = AccentGreen,
    onSecondary = Color.White,
    tertiary = AccentGreen,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
)

private val AmoledColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = AccentGreen,
    onSecondary = Color.Black,
    tertiary = AccentGreen,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    surfaceContainer = AmoledCardColor,
    surfaceContainerHigh = AmoledSurfaceVariant,
)

private val AuroraColorScheme = darkColorScheme(
    primary = AuroraMagenta,
    onPrimary = Color.White,
    secondary = AuroraCoral,
    onSecondary = Color.White,
    tertiary = AuroraAmber,
    background = AuroraVoid,
    surface = AuroraDeepPurple,
    surfaceVariant = AuroraUltraviolet,
    onBackground = PureWhite,
    onSurface = PureWhite,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    surfaceContainer = AuroraDeepPurple,
    surfaceContainerHigh = AuroraUltraviolet,
)

@Composable
fun DVibessTheme(
    appTheme: AppTheme = AppTheme.DARK,
    useDynamicColor: Boolean = false,
    accentColor: Color? = null,
    useAuroraTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        useAuroraTheme -> AuroraColorScheme
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            when (appTheme) {
                AppTheme.LIGHT -> dynamicLightColorScheme(context)
                AppTheme.DARK -> dynamicDarkColorScheme(context)
                AppTheme.AMOLED -> dynamicDarkColorScheme(context).copy(
                    background = AmoledBackground,
                    surface = AmoledSurface,
                )
            }
        }
        else -> {
            val baseScheme = when (appTheme) {
                AppTheme.LIGHT -> LightColorScheme
                AppTheme.DARK -> DarkColorScheme
                AppTheme.AMOLED -> AmoledColorScheme
            }
            if (accentColor != null) {
                baseScheme.copy(primary = accentColor, secondary = accentColor, tertiary = accentColor)
            } else {
                baseScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
