package com.pikacheat.mygandara.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.pikacheat.mygandara.util.ThemeMode

/** Current light/dark choice and a setter, provided by MainActivity. */
val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }
val LocalSetThemeMode = staticCompositionLocalOf<(ThemeMode) -> Unit> { {} }
