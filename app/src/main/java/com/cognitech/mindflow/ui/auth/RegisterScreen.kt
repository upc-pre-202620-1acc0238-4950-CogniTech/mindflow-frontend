package com.cognitech.mindflow.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R
import com.cognitech.mindflow.ui.components.DividerWithText
import com.cognitech.mindflow.ui.components.GoogleButton
import com.cognitech.mindflow.ui.components.GradientButton
import com.cognitech.mindflow.ui.components.LabeledInput
import com.cognitech.mindflow.ui.components.MindFlowLogo
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Silver
import com.cognitech.mindflow.ui.theme.SunsetOrange
import com.cognitech.mindflow.ui.theme.White

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
    onGoogleClick: () -> Unit,
) {
    val state = viewModel.state
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight - 48.dp)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            MindFlowLogo()
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.register_title), color = MineShaft, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.register_subtitle),
                color = Gray,
                fontSize = 15.2.sp,
            )
            Spacer(Modifier.height(32.dp))
            GoogleButton(stringResource(R.string.register_google), onClick = onGoogleClick)
            Spacer(Modifier.height(24.dp))
            DividerWithText(stringResource(R.string.register_or_email))
            Spacer(Modifier.height(24.dp))
            LabeledInput(
                label = stringResource(R.string.settings_full_name),
                value = state.name,
                onValueChange = viewModel::onNameChange,
                placeholder = stringResource(R.string.register_name_placeholder),
                error = state.nameError,
            )
            Spacer(Modifier.height(19.2.dp))
            LabeledInput(
                label = stringResource(R.string.settings_email),
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "ejemplo@correo.com",
                keyboardType = KeyboardType.Email,
                error = state.emailError,
            )
            Spacer(Modifier.height(19.2.dp))
            LabeledInput(
                label = stringResource(R.string.register_password_label),
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = stringResource(R.string.register_password_placeholder),
                isPassword = true,
                error = state.passwordError,
            )
            Spacer(Modifier.height(19.2.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.register_terms_prefix))
                    withStyle(SpanStyle(color = CornflowerBlue, textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.register_terms_of_service))
                    }
                    append(stringResource(R.string.register_terms_and))
                    withStyle(SpanStyle(color = CornflowerBlue, textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.register_privacy_policy))
                    }
                    append(stringResource(R.string.register_terms_suffix))
                },
                color = Gray,
                fontSize = 12.sp,
                lineHeight = 16.8.sp,
            )
            if (state.generalError != null) {
                Text(state.generalError, color = SunsetOrange, fontSize = 13.6.sp, modifier = Modifier.padding(top = 12.dp))
            }
            Spacer(Modifier.height(19.2.dp))
            GradientButton(
                text = stringResource(R.string.register_submit),
                onClick = { viewModel.signUp(onRegistered) },
                loading = state.loading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.register_has_account))
                    withStyle(SpanStyle(color = CornflowerBlue, fontWeight = FontWeight.SemiBold)) {
                        append(stringResource(R.string.register_login_here))
                    }
                },
                color = Gray,
                fontSize = 14.4.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onGoToLogin),
            )
        }

        RegisterFeaturesPanel()
    }
}

/** Bloque inferior del Registro: fondo #2F2F2F con las 3 funcionalidades principales. */
@Composable
private fun RegisterFeaturesPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 600.dp)
            .background(MineShaft)
            .background(
                Brush.radialGradient(
                    0f to CornflowerBlue.copy(alpha = 0.1f),
                    0.7f to CornflowerBlue.copy(alpha = 0f),
                    center = Offset(150f, 170f),
                    radius = 600f,
                )
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 64.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Text(
                stringResource(R.string.register_features_headline),
                color = White,
                fontSize = 35.2.sp,
                lineHeight = 42.24.sp,
                fontWeight = FontWeight.Bold,
            )
            Feature("🧠", "AI Mood Journal", stringResource(R.string.register_feature_mood_journal))
            Feature("📊", "Dynamic Habit Tracker", stringResource(R.string.register_feature_habit_tracker))
            Feature("🌬️", "Smart Interventions", stringResource(R.string.register_feature_interventions))
        }
    }
}

@Composable
private fun Feature(emoji: String, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
        Box(
            Modifier
                .size(40.dp)
                .background(White.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 19.2.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.3.dp)) {
            Text(title, color = Downy, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
            Text(description, color = Silver, fontSize = 14.4.sp, lineHeight = 21.6.sp)
        }
        Spacer(Modifier.width(0.dp))
    }
}
