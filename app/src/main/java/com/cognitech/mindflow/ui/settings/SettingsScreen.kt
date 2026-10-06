package com.cognitech.mindflow.ui.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.ui.components.GradientAvatar
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.MindCard
import com.cognitech.mindflow.ui.components.MindFlowInput
import com.cognitech.mindflow.ui.components.MindSwitch
import com.cognitech.mindflow.ui.components.OutlineButton
import com.cognitech.mindflow.ui.components.ScreenHeader
import com.cognitech.mindflow.ui.components.SolidButton
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Portage
import com.cognitech.mindflow.ui.theme.SunsetOrange
import com.cognitech.mindflow.ui.theme.White

private enum class SupportDialog { TICKET, FAQ, DELETE }

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigate: (MainDestination) -> Unit,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state = viewModel.state
    val context = LocalContext.current
    var dialog by remember { mutableStateOf<SupportDialog?>(null) }

    LaunchedEffect(state.saved) {
        if (state.saved) Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
    }

    MainScaffold(
        current = MainDestination.SETTINGS,
        onNavigate = onNavigate,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        header = { openMenu -> ScreenHeader("Ajustes y Privacidad", openMenu) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ProfileCard(state, viewModel) {
                Toast.makeText(context, "Tu avatar usa tu inicial y los colores de MindFlow", Toast.LENGTH_SHORT).show()
            }
            PreferencesCard(state, viewModel)
            SubscriptionCard(isPremium = state.user?.isPremium == true, onUpgrade = { onNavigate(MainDestination.PLANS) })
            MindCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Soporte Técnico", color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                OutlineButton("Generar Ticket de Ayuda", onClick = { dialog = SupportDialog.TICKET }, modifier = Modifier.fillMaxWidth())
                OutlineButton("Centro de Preguntas (FAQ)", onClick = { dialog = SupportDialog.FAQ }, modifier = Modifier.fillMaxWidth())
            }
            MindCard(
                padding = PaddingValues(25.dp),
                border = BorderStroke(1.dp, SunsetOrange),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Zona de Peligro", color = SunsetOrange, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Eliminar tu cuenta purgará permanentemente todos tus registros encriptados.",
                    color = Gray,
                    fontSize = 12.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlineButton(
                    text = "Eliminar mi cuenta",
                    onClick = { dialog = SupportDialog.DELETE },
                    borderColor = SunsetOrange,
                    contentColor = SunsetOrange,
                    background = SunsetOrange.copy(alpha = 0.1f),
                    radius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 13.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(36.dp))
        }
    }

    when (dialog) {
        SupportDialog.TICKET -> TicketDialog(onDismiss = { dialog = null }) { number ->
            dialog = null
            Toast.makeText(context, "Ticket #$number generado. Te contactaremos por correo.", Toast.LENGTH_LONG).show()
        }
        SupportDialog.FAQ -> FaqDialog(onDismiss = { dialog = null })
        SupportDialog.DELETE -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = White,
            title = { Text("¿Eliminar tu cuenta?", color = SunsetOrange, fontWeight = FontWeight.Bold) },
            text = { Text("Se borrarán tu perfil, tus registros del diario y tus hábitos de este dispositivo. Esta acción no se puede deshacer.", color = MineShaft) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    viewModel.deleteAccount(onLogout)
                }) { Text("Eliminar", color = SunsetOrange, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancelar", color = Gray) } },
        )
        null -> Unit
    }
}

@Composable
private fun ProfileCard(state: SettingsState, viewModel: SettingsViewModel, onChangeAvatar: () -> Unit) {
    MindCard(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            GradientAvatar(state.user?.initial ?: "U", size = 80.dp, fontSize = 32.sp)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(state.user?.name.orEmpty(), color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
                OutlineButton("Cambiar Avatar", onClick = onChangeAvatar)
            }
        }
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            ProfileField("Nombre Completo", state.name, viewModel::onNameChange)
            ProfileField("Correo Electrónico", state.user?.email.orEmpty(), {}, enabled = false)
            ProfileField("Ocupación", state.occupation, viewModel::onOccupationChange, placeholder = "Ej. Estudiante Universitario")
            ProfileField("Zona Horaria", state.timezone, viewModel::onTimezoneChange)
        }
        SolidButton(
            text = "Guardar Cambios",
            onClick = viewModel::save,
            background = CornflowerBlue,
            enabled = state.name.isNotBlank(),
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    enabled: Boolean = true,
    placeholder: String = "",
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Gray, fontSize = 13.6.sp, fontWeight = FontWeight.SemiBold)
        MindFlowInput(
            value = value,
            onValueChange = onChange,
            placeholder = placeholder,
            enabled = enabled,
            fontSize = 13.3.sp,
            fontWeight = FontWeight.Medium,
            contentPadding = PaddingValues(13.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PreferencesCard(state: SettingsState, viewModel: SettingsViewModel) {
    MindCard {
        Text("Privacidad y Experiencia", color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        PreferenceRow("Bloqueo por PIN (AES-256)", "Solicitar código de 4 dígitos al abrir la app.", state.pinLock, viewModel::onPinLockChange)
        HorizontalDivider(color = CatskillWhite)
        PreferenceRow("Modo Oscuro", "Ideal para registrar emociones en la noche.", state.darkMode, viewModel::onDarkModeChange)
        HorizontalDivider(color = CatskillWhite)
        PreferenceRow("Recordatorios de Hábitos", "Notificaciones push para hidratación y pausas.", state.reminders, viewModel::onRemindersChange, last = true)
    }
}

@Composable
private fun PreferenceRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit, last: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 19.2.dp, bottom = if (last) 0.dp else 20.2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Gray, fontSize = 12.8.sp)
        }
        MindSwitch(checked, onChange)
    }
}

// Tarjeta de marca con fondo oscuro fijo (no reactivo a Modo Oscuro): es un acento de diseño,
// no una superficie de la app, igual que el panel oscuro de Registro.
private val SubscriptionCardBg = Color(0xFF2F2F2F)
private val SubscriptionCardText = Color(0xFFCCCCCC)

@Composable
private fun SubscriptionCard(isPremium: Boolean, onUpgrade: () -> Unit) {
    MindCard(background = SubscriptionCardBg, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Suscripción", color = Downy, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                append("Plan Actual: ")
                withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append(if (isPremium) "Premium" else "Freemium") }
            },
            color = SubscriptionCardText,
            fontSize = 13.6.sp,
        )
        Column(Modifier.padding(top = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✔️ IA Mood Journal", color = SubscriptionCardText, fontSize = 13.6.sp)
            Text("✔️ Gestor de Hábitos", color = SubscriptionCardText, fontSize = 13.6.sp)
            Text(if (isPremium) "✔️ Exportación a PDF" else "❌ Exportación a PDF", color = SubscriptionCardText.copy(alpha = if (isPremium) 1f else 0.5f), fontSize = 13.6.sp)
            Text(if (isPremium) "✔️ Reportes Clínicos" else "❌ Reportes Clínicos", color = SubscriptionCardText.copy(alpha = if (isPremium) 1f else 0.5f), fontSize = 13.6.sp)
        }
        SolidButton(
            text = if (isPremium) "Gestionar Plan" else "Mejorar a Premium",
            onClick = onUpgrade,
            background = Portage,
            radius = 8.dp,
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TicketDialog(onDismiss: () -> Unit, onSent: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { Text("Generar Ticket de Ayuda", color = MineShaft, fontWeight = FontWeight.Bold) },
        text = {
            MindFlowInput(
                value = text,
                onValueChange = { text = it },
                placeholder = "Describe el problema que tienes...",
                singleLine = false,
                minHeight = 120.dp,
                fontSize = 14.4.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSent((1000..9999).random()) }, enabled = text.isNotBlank()) {
                Text("Enviar", color = CornflowerBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Gray) } },
    )
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    val faqs = listOf(
        "¿Mis registros son privados?" to "Sí. Tus registros se guardan cifrados y solo tú puedes verlos.",
        "¿Cómo funciona MindFlow AI?" to "Analiza el tono de lo que escribes para darte una respuesta empática y sugerencias de bienestar.",
        "¿Por qué se pausaron mis hábitos?" to "Cuando la IA detecta estrés alto, pausa las tareas de alta exigencia para que priorices tu descanso.",
        "¿Qué incluye Premium?" to "Exportación de reportes clínicos en PDF y CSV, analíticas avanzadas y soporte prioritario.",
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { Text("Centro de Preguntas (FAQ)", color = MineShaft, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                faqs.forEach { (q, a) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(q, color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold)
                        Text(a, color = Gray, fontSize = 13.6.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = CornflowerBlue) } },
    )
}
