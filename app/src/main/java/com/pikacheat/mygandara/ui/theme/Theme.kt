package com.pikacheat.mygandara.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GandaraGreen,
    onPrimary = OnGandaraGreen,
    primaryContainer = GandaraGreenContainer,
    onPrimaryContainer = OnGandaraGreenContainer,
    secondary = GandaraBlue,
    onSecondary = OnGandaraBlue,
    secondaryContainer = GandaraBlueContainer,
    onSecondaryContainer = OnGandaraBlueContainer,
    tertiary = GandaraGold,
    onTertiary = OnGandaraGold,
    tertiaryContainer = GandaraGoldContainer,
    onTertiaryContainer = OnGandaraGoldContainer,
    background = LightBackground,
    surface = LightBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = GandaraGreenDark,
    onPrimary = OnGandaraGreenDark,
    primaryContainer = GandaraGreenContainerDark,
    onPrimaryContainer = OnGandaraGreenContainerDark,
    secondary = GandaraBlueDark,
    onSecondary = OnGandaraBlueDark,
    secondaryContainer = GandaraBlueContainerDark,
    onSecondaryContainer = OnGandaraBlueContainerDark,
    tertiary = GandaraGoldDark,
    onTertiary = OnGandaraGoldDark,
    tertiaryContainer = GandaraGoldContainerDark,
    onTertiaryContainer = OnGandaraGoldContainerDark,
    background = DarkBackground,
    surface = DarkBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

/** Brand colours only (no Material You wallpaper colours) so the app always looks like Gandara. */
@Composable
fun MyGandaraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
