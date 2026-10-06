package com.cognitech.mindflow.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.MindFlowApplication
import com.cognitech.mindflow.R
import com.cognitech.mindflow.ui.chat.ChatWidget
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.LocalDarkMode
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.White
import kotlinx.coroutines.launch

/** Destinos del menú móvil, en el mismo orden que el Figma. */
enum class MainDestination(val route: String, val label: String) {
    DASHBOARD("home", "Dashboard"),
    JOURNAL("journal", "Diario (Journal)"),
    HABITS("habits", "Hábitos"),
    ANALYTICS("analytics", "Analíticas"),
    SETTINGS("settings", "Configuración"),
    PLANS("plans", "Planes"),
}

/**
 * Estructura común de las pantallas internas: menú lateral ("Menú móvil"), fondo #F5F7FA,
 * header blanco y contenido desplazable.
 */
@Composable
fun MainScaffold(
    current: MainDestination,
    onNavigate: (MainDestination) -> Unit,
    onLogout: () -> Unit,
    header: @Composable (openMenu: () -> Unit) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val close: () -> Unit = { scope.launch { drawerState.close() } }
    val context = LocalContext.current
    // Leída directo de SharedPreferences (vía SessionManager) en cada recomposición: no necesita
    // StateFlow porque Ajustes es un destino aparte — al volver a una pantalla principal, esta
    // se recompone y toma el valor actualizado.
    val darkMode = (context.applicationContext as MindFlowApplication).authRepository.session.darkMode

    CompositionLocalProvider(LocalDarkMode provides darkMode) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = White,
                drawerShape = RoundedCornerShape(0.dp),
                modifier = Modifier.width(320.dp),
            ) {
                AppDrawerContent(
                    current = current,
                    onClose = close,
                    onSelect = {
                        close()
                        if (it != current) onNavigate(it)
                    },
                    onLogout = onLogout,
                )
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CatskillWhite)
                .systemBarsPadding()
                .imePadding(),
        ) {
            Column(Modifier.fillMaxSize()) {
                header { scope.launch { drawerState.open() } }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                    // Espacio para que el botón del chat no tape el final del contenido.
                    Spacer(Modifier.height(72.dp))
                }
            }
            ChatWidget()
        }
    }
    }
}

@Composable
private fun AppDrawerContent(
    current: MainDestination,
    onClose: () -> Unit,
    onSelect: (MainDestination) -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .systemBarsPadding()
            .padding(start = 16.dp, end = 17.dp, top = 24.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(bottom = 40.dp), verticalAlignment = Alignment.Top) {
            MindFlowLogo(Modifier.weight(1f))
            Image(
                painter = painterResource(R.drawable.ic_menu_close),
                contentDescription = "Cerrar menú",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onClose),
            )
        }
        MainDestination.entries.forEach { destination ->
            DrawerLink(destination.label, selected = destination == current) { onSelect(destination) }
        }
        Spacer(Modifier.weight(1f))
        HorizontalDivider(color = Mercury, modifier = Modifier.padding(bottom = 8.dp))
        DrawerLink("Cerrar sesión", selected = false, onClick = onLogout)
    }
}

@Composable
private fun DrawerLink(label: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) CatskillWhite else White)
                .then(
                    if (selected) Modifier.drawBehind {
                        drawRect(CornflowerBlue, Offset.Zero, size.copy(width = 4.dp.toPx()))
                    } else Modifier
                )
                .clickable(onClick = onClick)
                .padding(start = if (selected) 20.dp else 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
        ) {
            Text(
                label,
                color = if (selected) CornflowerBlue else Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/** Botón "Abrir menú" (44x44, fondo #F7F7FC) exportado del Figma. */
@Composable
fun MenuButton(onClick: () -> Unit) {
    Image(
        painter = painterResource(R.drawable.ic_menu_open),
        contentDescription = "Abrir menú",
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    )
}

/** Header blanco de 70dp con borde inferior #E5E5E5. */
@Composable
fun ScreenHeader(
    title: String,
    onMenuClick: () -> Unit,
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
            MenuButton(onMenuClick)
            Text(
                title,
                color = com.cognitech.mindflow.ui.theme.MineShaft,
                fontSize = titleSize.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
        HorizontalDivider(color = Mercury)
    }
}
