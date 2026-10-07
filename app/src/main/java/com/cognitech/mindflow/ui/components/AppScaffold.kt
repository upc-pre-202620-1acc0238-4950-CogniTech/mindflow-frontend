package com.cognitech.mindflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R
import com.cognitech.mindflow.ui.chat.ChatWidget
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Gray400
import com.cognitech.mindflow.ui.theme.LocalDarkMode
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.ThemeState
import com.cognitech.mindflow.ui.theme.White

/** Destinos del menú móvil, en el mismo orden que el Figma. */
enum class MainDestination(val route: String) {
    DASHBOARD("home"),
    JOURNAL("journal"),
    HABITS("habits"),
    ANALYTICS("analytics"),
    SETTINGS("settings"),
    PLANS("plans"),
}

/** Tabs visibles en la barra inferior, en este orden. Planes vive dentro de Configuración. */
private val BottomNavItems = listOf(
    MainDestination.DASHBOARD to Icons.Filled.Home,
    MainDestination.JOURNAL to Icons.Filled.EditNote,
    MainDestination.HABITS to Icons.Filled.Checklist,
    MainDestination.ANALYTICS to Icons.Filled.Insights,
    MainDestination.SETTINGS to Icons.Filled.Person,
)

/**
 * Estructura común de las pantallas internas: header superior, contenido desplazable y una barra
 * de navegación inferior de 5 iconos (estilo Twitter/Instagram), fondo #F5F7FA.
 */
@Composable
fun MainScaffold(
    current: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    header: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    // State de Compose (no una relectura de SharedPreferences): togglear el switch en Ajustes
    // recompone esto al instante, sin tener que navegar a otra pantalla primero.
    val darkMode = ThemeState.isDark

    CompositionLocalProvider(LocalDarkMode provides darkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CatskillWhite)
                .systemBarsPadding()
                .imePadding(),
        ) {
            Column(Modifier.fillMaxSize()) {
                header()
                Box(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        content()
                        // Espacio para que el botón del chat no tape el final del contenido.
                        Spacer(Modifier.height(72.dp))
                    }
                    ChatWidget()
                }
                BottomNavBar(current = current, onNavigate = onNavigate)
            }
        }
    }
}

/** Barra inferior fija de 5 iconos (sin etiquetas, igual que Twitter/Instagram). */
@Composable
private fun BottomNavBar(current: MainDestination, onNavigate: (MainDestination) -> Unit) {
    // Planes no tiene tab propio: si el usuario está ahí, resaltamos Perfil (Configuración),
    // de donde se accede a Planes.
    val highlighted = if (current == MainDestination.PLANS) MainDestination.SETTINGS else current
    Column(Modifier.fillMaxWidth().background(White)) {
        HorizontalDivider(color = Mercury)
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            BottomNavItems.forEach { (destination, icon) ->
                val selected = destination == highlighted
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .clickable(onClick = { if (destination != current) onNavigate(destination) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = stringResource(destination.labelRes()),
                        tint = if (selected) CornflowerBlue else Gray400,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

private fun MainDestination.labelRes(): Int = when (this) {
    MainDestination.DASHBOARD -> R.string.nav_home
    MainDestination.JOURNAL -> R.string.nav_journal
    MainDestination.HABITS -> R.string.nav_habits
    MainDestination.ANALYTICS -> R.string.nav_analytics
    MainDestination.SETTINGS -> R.string.nav_profile
    MainDestination.PLANS -> R.string.nav_plans
}

/** Header blanco de 70dp con borde inferior #E5E5E5. */
@Composable
fun ScreenHeader(
    title: String,
    titleSize: Int = 24,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().background(White)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                title,
                color = MineShaft,
                fontSize = titleSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
        HorizontalDivider(color = Mercury)
    }
}
