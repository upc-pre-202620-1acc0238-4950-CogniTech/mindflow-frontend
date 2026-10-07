package com.cognitech.mindflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cognitech.mindflow.ui.navigation.AppNavigation
import com.cognitech.mindflow.ui.theme.MindFlowTheme

/**
 * AppCompatActivity (no ComponentActivity) a propósito: es lo que hace que
 * AppCompatDelegate.setApplicationLocales() (botón de idioma en Ajustes) realmente recree la
 * pantalla con el idioma nuevo. Con ComponentActivity el cambio se guarda pero nada le avisa a
 * Compose que debe redibujarse, por lo que el botón parece no hacer nada (bug que estaba pasando).
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MindFlowTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(application as MindFlowApplication)
                }
            }
        }
    }
}
