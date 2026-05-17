package com.reznick.spitecards.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SpiteDarkColorScheme = darkColorScheme(
    primary = SpiteGreen,
    onPrimary = SpiteOnSurface,
    primaryContainer = SpiteGreenDark,
    secondary = SpiteAccent,
    background = SpiteSurface,
    surface = SpiteSurface,
    onSurface = SpiteOnSurface,
    error = SpiteError
)

@Composable
fun SpiteCardsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpiteDarkColorScheme,
        typography = SpiteTypography,
        content = content
    )
}
