package com.reznick.spitepine.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainerLight,
    onPrimaryContainer = Color(0xFF002113),

    secondary = WarmBrown,
    onSecondary = Color.White,
    secondaryContainer = WarmBrownContainerLight,
    onSecondaryContainer = Color(0xFF2C1700),

    tertiary = Amber,
    onTertiary = Color.White,

    background = WarmWhite,
    onBackground = WarmCharcoal,
    surface = WarmWhite,
    onSurface = WarmCharcoal,
    surfaceVariant = Color(0xFFEEE9DF),
    onSurfaceVariant = WarmGreyLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenLight,
    onPrimary = Color(0xFF002113),
    primaryContainer = ForestGreenContainerDark,
    onPrimaryContainer = ForestGreenContainerLight,

    secondary = WarmBrownLight,
    onSecondary = Color(0xFF2C1700),
    secondaryContainer = WarmBrownContainerDark,
    onSecondaryContainer = WarmBrownContainerLight,

    tertiary = AmberLight,
    onTertiary = Color(0xFF2A1900),

    background = WarmCharcoal,
    onBackground = WarmGreyDark,
    surface = WarmCharcoal,
    onSurface = WarmGreyDark,
    surfaceVariant = Color(0xFF2D2A26),
    onSurfaceVariant = WarmGreyDark,
)

// Dynamic color is intentionally OFF by default so the app stays on-brand
// across the Spite line (spec §14). Pass dynamicColor = true at the call
// site to opt back into wallpaper-derived theming on Android 12+.
@Composable
fun SpitePineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
