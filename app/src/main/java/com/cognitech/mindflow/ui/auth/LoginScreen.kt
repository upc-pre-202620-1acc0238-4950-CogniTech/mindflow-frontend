package com.cognitech.mindflow.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.MindGradient
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.SunsetOrange
import com.cognitech.mindflow.ui.theme.White

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoogleClick: () -> Unit,
) {
    val state = viewModel.state
    var showForgotDialog by remember { mutableStateOf(false) }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
    ) {
        // Formulario (alto de pantalla completa, centrado verticalmente como en el Figma)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight - 48.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            MindFlowLogo()
            Spacer(Modifier.height(32.dp))
            Text(stringResource(R.string.login_welcome_back), color = MineShaft, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.login_subtitle),
                color = Gray,
                fontSize = 15.2.sp,
            )
            Spacer(Modifier.height(32.dp))
            GoogleButton(stringResource(R.string.login_google), onClick = onGoogleClick)
            Spacer(Modifier.height(24.dp))
            DividerWithText(stringResource(R.string.login_or_email))
            Spacer(Modifier.height(24.dp))
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
                label = stringResource(R.string.login_password),
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "••••••••",
                isPassword = true,
                error = state.passwordError,
            )
            Spacer(Modifier.height(11.2.dp))
            Text(
                stringResource(R.string.login_forgot_password),
                color = CornflowerBlue,
                fontSize = 13.6.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { showForgotDialog = true },
            )
            if (state.generalError != null) {
                Text(state.generalError, color = SunsetOrange, fontSize = 13.6.sp, modifier = Modifier.padding(top = 12.dp))
            }
            Spacer(Modifier.height(24.dp))
            GradientButton(
                text = stringResource(R.string.login_submit),
                onClick = { viewModel.signIn(onLoggedIn) },
                loading = state.loading,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.login_no_account))
                    withStyle(SpanStyle(color = CornflowerBlue, fontWeight = FontWeight.SemiBold)) {
                        append(stringResource(R.string.login_register_free))
                    }
                },
                color = Gray,
                fontSize = 14.4.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable(onClick = onGoToRegister),
            )
        }

        LoginTestimonialPanel()
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            confirmButton = { TextButton(onClick = { showForgotDialog = false }) { Text(stringResource(R.string.common_understood)) } },
            title = { Text(stringResource(R.string.login_recover_password)) },
            text = { Text(stringResource(R.string.login_recover_password_body)) },
        )
    }
}

/** Bloque inferior del Login: fondo degradado, "Retoma el control de tu calma interior." y testimonio. */
@Composable
private fun LoginTestimonialPanel() {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .background(MindGradient)
            .clipToBounds(),
    ) {
        Box(
            Modifier
                .offset(x = maxWidth * 0.52f, y = (-60).dp)
                .size(width = maxWidth * 0.58f, height = 330.dp)
                .blur(20.dp)
                .background(White.copy(alpha = 0.1f), RoundedCornerShape(200.dp))
        )
        Box(
            Modifier
                .offset(x = -maxWidth * 0.1f, y = 460.dp)
                .size(width = maxWidth * 0.43f, height = 200.dp)
                .blur(25.dp)
                .background(White.copy(alpha = 0.15f), RoundedCornerShape(150.dp))
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                stringResource(R.string.login_testimonial_headline),
                color = White,
                fontSize = 40.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Bold,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(1.dp, White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(33.dp),
            ) {
                Text(
                    stringResource(R.string.login_testimonial_quote),
                    color = White,
                    fontSize = 19.2.sp,
                    lineHeight = 30.72.sp,
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}
