package com.cognitech.mindflow.ui.plans

import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognitech.mindflow.R
import com.cognitech.mindflow.data.model.User
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.ui.components.GradientButton
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.ScreenHeader
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Portage
import com.cognitech.mindflow.ui.theme.White
import kotlinx.coroutines.launch

class PlansViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var user by mutableStateOf<User?>(null)
        private set
    var checkoutUrl by mutableStateOf<String?>(null)
        private set
    var isCheckoutLoading by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    fun load() {
        viewModelScope.launch {
            user = authRepository.currentUser()
            refreshSubscription(silent = true)
        }
    }

    fun startCheckout() {
        viewModelScope.launch {
            isCheckoutLoading = true
            authRepository.createCheckout()
                .onSuccess { checkoutUrl = it }
                .onFailure { message = it.message ?: "No se pudo iniciar Checkout" }
            isCheckoutLoading = false
        }
    }

    fun refreshSubscription(silent: Boolean = false) {
        viewModelScope.launch {
            authRepository.refreshSubscription()
                .onSuccess { refreshed -> if (refreshed != null) user = refreshed }
                .onFailure { if (!silent) message = it.message ?: "No se pudo actualizar el plan" }
        }
    }

    fun checkoutOpened() { checkoutUrl = null }
    fun consumeMessage() { message = null }

    fun logout() = authRepository.logout()
}

@Composable
fun PlansScreen(
    viewModel: PlansViewModel,
    onNavigate: (MainDestination) -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val isPremium = viewModel.user?.isPremium == true
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var confirmUpgrade by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSubscription(silent = true)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(viewModel.checkoutUrl) {
        viewModel.checkoutUrl?.let { url ->
            CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
            viewModel.checkoutOpened()
        }
    }
    LaunchedEffect(viewModel.message) {
        viewModel.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    MainScaffold(
        current = MainDestination.PLANS,
        onNavigate = onNavigate,
        header = { ScreenHeader(stringResource(R.string.plans_title)) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.plans_headline),
                color = MineShaft,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.plans_subheadline),
                color = Gray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))
            val downgradeMessage = stringResource(R.string.plans_downgrade_toast)
            Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                FreemiumCard(
                    isCurrent = !isPremium,
                    onDowngrade = {
                        Toast.makeText(context, downgradeMessage, Toast.LENGTH_LONG).show()
                    },
                )
                PremiumCard(isCurrent = isPremium, onUpgrade = { confirmUpgrade = true })
            }
            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.ic_lock), contentDescription = null, modifier = Modifier.size(16.dp))
                Text(
                    stringResource(R.string.plans_stripe_disclosure),
                    color = Gray,
                    fontSize = 12.8.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (confirmUpgrade) {
        AlertDialog(
            onDismissRequest = { confirmUpgrade = false },
            containerColor = White,
            title = { Text("MindFlow Premium", color = Portage, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.plans_checkout_disclaimer), color = MineShaft) },
            confirmButton = {
                TextButton(onClick = {
                    confirmUpgrade = false
                    viewModel.startCheckout()
                }, enabled = !viewModel.isCheckoutLoading) { Text(stringResource(if (viewModel.isCheckoutLoading) R.string.common_preparing else R.string.common_continue), color = Portage, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { confirmUpgrade = false }) { Text(stringResource(R.string.common_cancel), color = Gray) } },
        )
    }
}

@Composable
private fun FreemiumCard(isCurrent: Boolean, onDowngrade: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.03f), spotColor = Color.Black.copy(alpha = 0.05f))
            .background(White, shape)
            .border(1.dp, Mercury, shape)
            .padding(horizontal = 33.dp, vertical = 41.dp),
    ) {
        Text("Freemium", color = MineShaft, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        Price("\$0")
        Text(
            stringResource(R.string.plans_freemium_tagline),
            color = Gray,
            fontSize = 14.4.sp,
            modifier = Modifier.padding(bottom = 32.dp),
        )
        Column(Modifier.padding(bottom = 56.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Feature("AI Mood Journal:", stringResource(R.string.plans_feature_mood_journal))
            Feature("Dynamic Habit Tracker:", stringResource(R.string.plans_feature_habit_tracker))
            Feature("Smart Interventions:", stringResource(R.string.plans_feature_interventions))
            Feature(stringResource(R.string.plans_feature_basic_analytics_bold), stringResource(R.string.plans_feature_basic_analytics_rest))
            MissingFeature(stringResource(R.string.plans_feature_pdf_export))
            MissingFeature(stringResource(R.string.plans_feature_csv_export))
        }
        val buttonShape = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(buttonShape)
                .background(CatskillWhite, buttonShape)
                .border(1.dp, Mercury, buttonShape)
                .clickable(enabled = !isCurrent, onClick = onDowngrade)
                .padding(13.8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(if (isCurrent) R.string.plans_current_plan else R.string.plans_back_to_freemium), color = Gray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PremiumCard(isCurrent: Boolean, onUpgrade: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = Portage.copy(alpha = 0.15f), spotColor = Portage.copy(alpha = 0.25f))
            .background(White, shape)
            .border(BorderStroke(2.dp, Portage), shape)
            .clip(shape),
    ) {
        Column(Modifier.padding(horizontal = 34.dp, vertical = 42.dp)) {
            Text("MindFlow Premium", color = Portage, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
            Price("\$4.99")
            Text(
                stringResource(R.string.plans_premium_tagline),
                color = Gray,
                fontSize = 14.4.sp,
                modifier = Modifier.padding(bottom = 32.dp),
            )
            Column(Modifier.padding(bottom = 57.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Feature(stringResource(R.string.plans_feature_all_freemium), "")
                Feature(stringResource(R.string.plans_feature_pdf_bold), stringResource(R.string.plans_feature_pdf_detail))
                Feature(stringResource(R.string.plans_feature_csv_bold), stringResource(R.string.plans_feature_csv_detail))
                Feature(stringResource(R.string.plans_feature_advanced_analytics_bold), stringResource(R.string.plans_feature_advanced_analytics_rest))
                Feature(stringResource(R.string.plans_feature_priority_support_bold), stringResource(R.string.plans_feature_priority_support_rest))
            }
            GradientButton(
                text = stringResource(if (isCurrent) R.string.plans_current_plan else R.string.plans_upgrade_to_premium),
                onClick = onUpgrade,
                enabled = !isCurrent,
                contentPadding = PaddingValues(12.8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(8.dp), ambientColor = CornflowerBlue.copy(alpha = 0.3f), spotColor = CornflowerBlue.copy(alpha = 0.3f)),
            )
        }
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .background(Portage, RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .padding(horizontal = 24.dp, vertical = 4.8.dp)
        ) {
            Text(stringResource(R.string.plans_recommended), color = Color.White, fontSize = 12.8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun Price(amount: String) {
    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 24.dp)) {
        Text(amount, color = MineShaft, fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.plans_per_month), color = Gray, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 9.dp))
    }
}

@Composable
private fun Feature(bold: String, rest: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✓", color = Downy, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(bold) }
                append(rest)
            },
            color = MineShaft,
            fontSize = 15.2.sp,
        )
    }
}

@Composable
private fun MissingFeature(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✕", color = Mercury, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
        Text(text, color = Gray, fontSize = 15.2.sp, textDecoration = TextDecoration.LineThrough)
    }
}
