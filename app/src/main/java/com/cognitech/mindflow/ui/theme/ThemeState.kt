package com.cognitech.mindflow.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Fuente de verdad en memoria para la preferencia de Modo Oscuro. [MindFlowApplication] la
 * inicializa desde [com.cognitech.mindflow.data.local.SessionManager.darkMode] al arrancar, y
 * [com.cognitech.mindflow.ui.settings.SettingsViewModel] la actualiza al tocar el switch.
 *
 * Es un `mutableStateOf` (no una simple lectura de SharedPreferences en cada recomposición) para
 * que el cambio de tema se vea al instante en toda pantalla que lo esté observando, sin depender
 * de navegar a otro destino para que [com.cognitech.mindflow.ui.components.MainScaffold] vuelva a
 * leer el valor.
 */
object ThemeState {
    var isDark by mutableStateOf(false)
}
