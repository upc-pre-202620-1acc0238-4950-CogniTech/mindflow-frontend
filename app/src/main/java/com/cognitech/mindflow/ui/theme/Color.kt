package com.cognitech.mindflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Preferencia de Modo Oscuro (Ajustes > Privacidad y Experiencia), provista por [com.cognitech.mindflow.ui.components.MainScaffold]
 * hacia las pantallas autenticadas. Login/Registro no la leen (quedan siempre en el estilo claro del Figma),
 * ya que sus paneles decorativos (gradiente/fondo oscuro fijo) no están pensados para invertirse.
 */
val LocalDarkMode = staticCompositionLocalOf { false }

// Tokens del Figma "Web Application" (página 2 - mockups móviles).
// Son propiedades @Composable (no constantes) para reaccionar a [LocalDarkMode]: la superficie
// que en claro es blanca pasa a gris oscuro, y el texto que era casi negro pasa a casi blanco.
val MineShaft: Color        // color/grey/18 - texto principal
    @Composable get() = if (LocalDarkMode.current) Color(0xFFF2F2F2) else Color(0xFF2F2F2F)
val Gray: Color             // color/grey/51 - texto secundario
    @Composable get() = if (LocalDarkMode.current) Color(0xFFB0B0B0) else Color(0xFF828282)
val Boulder: Color          // color/grey/46 - placeholders
    @Composable get() = if (LocalDarkMode.current) Color(0xFF9E9E9E) else Color(0xFF757575)
val DoveGray: Color         // color/grey/40
    @Composable get() = if (LocalDarkMode.current) Color(0xFFBDBDBD) else Color(0xFF666666)
val Silver: Color           // color/grey/80
    @Composable get() = if (LocalDarkMode.current) Color(0xFF3F3F3F) else Color(0xFFCCCCCC)
val Mercury: Color          // color/grey/90 - bordes
    @Composable get() = if (LocalDarkMode.current) Color(0xFF3A3A3C) else Color(0xFFE5E5E5)
val Gallery: Color          // color/grey/94
    @Composable get() = if (LocalDarkMode.current) Color(0xFF2C2C2E) else Color(0xFFEFEFEF)
val CatskillWhite: Color    // color/grey/97 - fondos de campos
    @Composable get() = if (LocalDarkMode.current) Color(0xFF1C1C1E) else Color(0xFFF5F7FA)
val MenuButtonBg: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF2C2C2E) else Color(0xFFF7F7FC)
/** Superficie "blanca" (tarjetas, headers, diálogos): se invierte a gris casi negro en oscuro. */
val White: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF121212) else Color(0xFFFFFFFF)

val CornflowerBlue = Color(0xFF4F8DF5)  // color/azure/64
val Downy = Color(0xFF6ED3A3)           // color/spring green/63
val GoldenTainoi = Color(0xFFFFD166)    // color/orange/70
val VividTangerine = Color(0xFFFF8A8A)  // color/red/77
val Zest = Color(0xFFE28F22)            // color/orange/51
val Serenade = Color(0xFFFFF4E5)        // color/grey/95
val Portage = Color(0xFF8A7CF6)         // color/blue/73
val SunsetOrange = Color(0xFFFF4A4A)    // color/red/65

// Colores de la pantalla de Analíticas (también reactivos a LocalDarkMode, mismo criterio que arriba)
val Gray800: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFFF3F4F6) else Color(0xFF1F2937)
val Gray500: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF9CA3AF) else Color(0xFF6B7280)
val Gray400: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF6B7280) else Color(0xFF9CA3AF)
val Gray50: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF1F2937) else Color(0xFFF9FAFB)
val Gray100: Color
    @Composable get() = if (LocalDarkMode.current) Color(0xFF374151) else Color(0xFFF3F4F6)
val Purple500 = Color(0xFFA855F7)
val Blue400 = Color(0xFF60A5FA)
val Orange400 = Color(0xFFFB923C)

val MindGradient = Brush.linearGradient(listOf(CornflowerBlue, Downy))
val MindGradientSoft = Brush.linearGradient(
    listOf(CornflowerBlue.copy(alpha = 0.05f), Downy.copy(alpha = 0.05f))
)
