package com.cognitech.mindflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MindFlowTheme(content: @Composable () -> Unit) {
    // Se arma dentro de la función @Composable (no como val de nivel superior) porque White,
    // MineShaft, Gray y Mercury ahora son propiedades @Composable (reaccionan a LocalDarkMode).
    val colors = lightColorScheme(
        primary = CornflowerBlue,
        onPrimary = Color.White, // fijo: texto sobre primary (CornflowerBlue), un color sólido de marca
        secondary = Downy,
        onSecondary = Color.White, // fijo: texto sobre secondary (Downy)
        background = White,
        onBackground = MineShaft,
        surface = White,
        onSurface = MineShaft,
        onSurfaceVariant = Gray,
        outline = Mercury,
        error = SunsetOrange,
    )
    MaterialTheme(colorScheme = colors, typography = MindFlowTypography, content = content)
}
