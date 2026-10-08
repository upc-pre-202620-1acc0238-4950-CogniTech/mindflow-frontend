package com.cognitech.mindflow.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.cognitech.mindflow.R
import com.cognitech.mindflow.BuildConfig
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cognitech.mindflow.MindFlowApplication
import com.cognitech.mindflow.ui.analytics.AnalyticsScreen
import com.cognitech.mindflow.ui.analytics.AnalyticsViewModel
import com.cognitech.mindflow.ui.auth.AuthViewModel
import com.cognitech.mindflow.ui.auth.LoginScreen
import com.cognitech.mindflow.ui.auth.RegisterScreen
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.habits.HabitsScreen
import com.cognitech.mindflow.ui.habits.HabitsViewModel
import com.cognitech.mindflow.ui.home.HomeScreen
import com.cognitech.mindflow.ui.home.HomeViewModel
import com.cognitech.mindflow.ui.journal.JournalScreen
import com.cognitech.mindflow.ui.journal.JournalViewModel
import com.cognitech.mindflow.ui.plans.PlansScreen
import com.cognitech.mindflow.ui.plans.PlansViewModel
import com.cognitech.mindflow.ui.settings.SettingsScreen
import com.cognitech.mindflow.ui.settings.SettingsViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
}

@Composable
fun AppNavigation(app: MindFlowApplication, navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = app.authUseCases
    val legacyAuth = app.authRepository
    val onGoogleClick: () -> Unit = {
        val activity = context as? Activity
        if (activity == null) {
            Toast.makeText(context, "No se pudo abrir el acceso con Google", Toast.LENGTH_SHORT).show()
        } else {
            scope.launch {
                try {
                    val option = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        .build()
                    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                    val result = CredentialManager.create(context).getCredential(activity, request)
                    val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    legacyAuth.signInWithGoogle(googleCredential.idToken)
                        .onSuccess { navController.goHome() }
                        .onFailure { error ->
                            Toast.makeText(context, error.message ?: "No se pudo iniciar sesión con Google", Toast.LENGTH_LONG).show()
                        }
                } catch (_: NoCredentialException) {
                    Toast.makeText(context, "No se encontró una cuenta de Google disponible", Toast.LENGTH_LONG).show()
                } catch (_: GetCredentialException) {
                    Toast.makeText(context, "No se pudo completar el acceso con Google", Toast.LENGTH_LONG).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "No se pudo iniciar sesión con Google", Toast.LENGTH_LONG).show()
                }
            }
            Unit
        }
    }
    val authFactory = viewModelFactory { initializer { AuthViewModel(auth) } }
    val start = if (auth.isLoggedIn()) MainDestination.DASHBOARD.route else Routes.LOGIN

    val onNavigate: (MainDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(MainDestination.DASHBOARD.route) { inclusive = destination == MainDestination.DASHBOARD }
            launchSingleTop = true
        }
    }
    val onLogout: () -> Unit = { navController.navigate(Routes.LOGIN) { popUpTo(0) } }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = viewModel(factory = authFactory),
                onLoggedIn = { navController.goHome() },
                onGoToRegister = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } },
                onGoogleClick = onGoogleClick,
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = viewModel(factory = authFactory),
                onRegistered = { navController.goHome() },
                onGoToLogin = {
                    if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
                        navController.navigate(Routes.LOGIN) { popUpTo(0) }
                    }
                },
                onGoogleClick = onGoogleClick,
            )
        }
        composable(MainDestination.DASHBOARD.route) {
            HomeScreen(
                viewModel = viewModel(factory = viewModelFactory {
                    initializer { HomeViewModel(legacyAuth, app.journalRepository, app.habitRepository) }
                }),
                onNavigate = onNavigate,
            )
        }
        composable(MainDestination.JOURNAL.route) {
            JournalScreen(
                viewModel = viewModel(factory = viewModelFactory { initializer { JournalViewModel(legacyAuth, app.journalRepository) } }),
                onNavigate = onNavigate,
            )
        }
        composable(MainDestination.HABITS.route) {
            HabitsScreen(
                viewModel = viewModel(factory = viewModelFactory {
                    initializer { HabitsViewModel(legacyAuth, app.habitRepository, app.journalRepository) }
                }),
                onNavigate = onNavigate,
            )
        }
        composable(MainDestination.ANALYTICS.route) {
            AnalyticsScreen(
                viewModel = viewModel(factory = viewModelFactory {
                    initializer { AnalyticsViewModel(legacyAuth, app.journalRepository, app.habitRepository, app.aiResponder) }
                }),
                onNavigate = onNavigate,
            )
        }
        composable(MainDestination.SETTINGS.route) {
            SettingsScreen(
                viewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(legacyAuth) } }),
                onNavigate = onNavigate,
                onLogout = onLogout,
            )
        }
        composable(MainDestination.PLANS.route) {
            PlansScreen(
                viewModel = viewModel(factory = viewModelFactory { initializer { PlansViewModel(legacyAuth) } }),
                onNavigate = onNavigate,
            )
        }
    }
}

private fun NavHostController.goHome() = navigate(MainDestination.DASHBOARD.route) { popUpTo(0) }
