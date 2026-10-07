package com.cognitech.mindflow.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R
import com.cognitech.mindflow.ui.components.FilterPill
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

    val savedMessage = stringResource(R.string.settings_saved_toast)
    LaunchedEffect(state.saved) {
        if (state.saved) Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
    }

    val logout: () -> Unit = {
        viewModel.logout()
        onLogout()
    }
    val avatarHint = stringResource(R.string.settings_avatar_hint)

    MainScaffold(
        current = MainDestination.SETTINGS,
        onNavigate = onNavigate,
        header = { ScreenHeader(stringResource(R.string.settings_title)) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ProfileCard(state, viewModel) {
                Toast.makeText(context, avatarHint, Toast.LENGTH_SHORT).show()
            }
            PreferencesCard(state, viewModel)
            SubscriptionCard(isPremium = state.user?.isPremium == true, onUpgrade = { onNavigate(MainDestination.PLANS) })
            OutlineButton(stringResource(R.string.settings_logout), onClick = logout, modifier = Modifier.fillMaxWidth())
            MindCard(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_support_title), color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                OutlineButton(stringResource(R.string.settings_generate_ticket), onClick = { dialog = SupportDialog.TICKET }, modifier = Modifier.fillMaxWidth())
                OutlineButton(stringResource(R.string.settings_faq), onClick = { dialog = SupportDialog.FAQ }, modifier = Modifier.fillMaxWidth())
            }
            MindCard(
                padding = PaddingValues(25.dp),
                border = BorderStroke(1.dp, SunsetOrange),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.settings_danger_zone), color = SunsetOrange, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.settings_delete_account_body),
                    color = Gray,
                    fontSize = 12.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlineButton(
                    text = stringResource(R.string.settings_delete_account),
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
        SupportDialog.TICKET -> {
            val ticketGenerated = stringResource(R.string.settings_ticket_generated)
            TicketDialog(onDismiss = { dialog = null }) { number ->
                dialog = null
                Toast.makeText(context, ticketGenerated.format(number), Toast.LENGTH_LONG).show()
            }
        }
        SupportDialog.FAQ -> FaqDialog(onDismiss = { dialog = null })
        SupportDialog.DELETE -> AlertDialog(
            onDismissRequest = { dialog = null },
            containerColor = White,
            title = { Text(stringResource(R.string.settings_delete_confirm_title), color = SunsetOrange, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.settings_delete_confirm_body), color = MineShaft) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = null
                    viewModel.deleteAccount(onLogout)
                }) { Text(stringResource(R.string.common_delete), color = SunsetOrange, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.common_cancel), color = Gray) } },
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
                OutlineButton(stringResource(R.string.settings_change_avatar), onClick = onChangeAvatar)
            }
        }
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            ProfileField(stringResource(R.string.settings_full_name), state.name, viewModel::onNameChange)
            ProfileField(stringResource(R.string.settings_email), state.user?.email.orEmpty(), {}, enabled = false)
            ProfileField(stringResource(R.string.settings_occupation), state.occupation, viewModel::onOccupationChange, placeholder = stringResource(R.string.settings_occupation_placeholder))
            ProfileField(stringResource(R.string.settings_timezone), state.timezone, viewModel::onTimezoneChange)
        }
        SolidButton(
            text = stringResource(R.string.settings_save_changes),
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
        Text(stringResource(R.string.settings_privacy_title), color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        PreferenceRow(stringResource(R.string.settings_pin_lock), stringResource(R.string.settings_pin_lock_desc), state.pinLock, viewModel::onPinLockChange)
        HorizontalDivider(color = CatskillWhite)
        PreferenceRow(stringResource(R.string.settings_dark_mode), stringResource(R.string.settings_dark_mode_desc), state.darkMode, viewModel::onDarkModeChange)
        HorizontalDivider(color = CatskillWhite)
        PreferenceRow(stringResource(R.string.settings_reminders), stringResource(R.string.settings_reminders_desc), state.reminders, viewModel::onRemindersChange)
        HorizontalDivider(color = CatskillWhite)
        LanguageRow()
    }
}

@Composable
private fun LanguageRow() {
    val current = AppCompatDelegate.getApplicationLocales()
    val isEnglish = current.toLanguageTags().startsWith("en")
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 19.2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(stringResource(R.string.settings_language), color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.settings_language_desc), color = Gray, fontSize = 12.8.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterPill("ES", selected = !isEnglish) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("es"))
            }
            FilterPill("EN", selected = isEnglish) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            }
        }
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
        Text(stringResource(R.string.settings_subscription_title), color = Downy, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                append(stringResource(R.string.settings_current_plan))
                withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append(if (isPremium) "Premium" else "Freemium") }
            },
            color = SubscriptionCardText,
            fontSize = 13.6.sp,
        )
        Column(Modifier.padding(top = 16.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("✔️ " + stringResource(R.string.settings_feature_mood_journal), color = SubscriptionCardText, fontSize = 13.6.sp)
            Text("✔️ " + stringResource(R.string.settings_feature_habit_manager), color = SubscriptionCardText, fontSize = 13.6.sp)
            Text((if (isPremium) "✔️ " else "❌ ") + stringResource(R.string.settings_feature_pdf_export), color = SubscriptionCardText.copy(alpha = if (isPremium) 1f else 0.5f), fontSize = 13.6.sp)
            Text((if (isPremium) "✔️ " else "❌ ") + stringResource(R.string.settings_feature_clinical_reports), color = SubscriptionCardText.copy(alpha = if (isPremium) 1f else 0.5f), fontSize = 13.6.sp)
        }
        SolidButton(
            text = stringResource(if (isPremium) R.string.settings_manage_plan else R.string.settings_upgrade_premium),
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
        title = { Text(stringResource(R.string.settings_generate_ticket), color = MineShaft, fontWeight = FontWeight.Bold) },
        text = {
            MindFlowInput(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(R.string.settings_ticket_placeholder),
                singleLine = false,
                minHeight = 120.dp,
                fontSize = 14.4.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSent((1000..9999).random()) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.common_send), color = CornflowerBlue, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel), color = Gray) } },
    )
}

@Composable
private fun FaqDialog(onDismiss: () -> Unit) {
    val faqs = listOf(
        R.string.settings_faq_q1 to R.string.settings_faq_a1,
        R.string.settings_faq_q2 to R.string.settings_faq_a2,
        R.string.settings_faq_q3 to R.string.settings_faq_a3,
        R.string.settings_faq_q4 to R.string.settings_faq_a4,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { Text(stringResource(R.string.settings_faq), color = MineShaft, fontWeight = FontWeight.Bold) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                faqs.forEach { (q, a) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(q), color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(a), color = Gray, fontSize = 13.6.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_close), color = CornflowerBlue) } },
    )
}
