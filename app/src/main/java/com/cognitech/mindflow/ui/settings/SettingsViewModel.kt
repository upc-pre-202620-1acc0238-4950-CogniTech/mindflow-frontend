package com.cognitech.mindflow.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognitech.mindflow.data.model.User
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.ui.theme.ThemeState
import kotlinx.coroutines.launch

data class SettingsState(
    val user: User? = null,
    val name: String = "",
    val occupation: String = "",
    val timezone: String = "",
    val pinLock: Boolean = true,
    val darkMode: Boolean = false,
    val reminders: Boolean = true,
    val saved: Boolean = false,
)

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val session = authRepository.session

    var state by mutableStateOf(SettingsState())
        private set

    fun load() {
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            state = SettingsState(
                user = user,
                name = user.name,
                occupation = user.occupation,
                timezone = user.timezone,
                pinLock = session.pinLock,
                darkMode = session.darkMode,
                reminders = session.habitReminders,
            )
        }
    }

    fun onNameChange(value: String) { state = state.copy(name = value, saved = false) }
    fun onOccupationChange(value: String) { state = state.copy(occupation = value, saved = false) }
    fun onTimezoneChange(value: String) { state = state.copy(timezone = value, saved = false) }

    fun onPinLockChange(value: Boolean) {
        session.pinLock = value
        state = state.copy(pinLock = value)
    }

    fun onDarkModeChange(value: Boolean) {
        session.darkMode = value
        ThemeState.isDark = value
        state = state.copy(darkMode = value)
    }

    fun onRemindersChange(value: Boolean) {
        session.habitReminders = value
        state = state.copy(reminders = value)
    }

    fun save() {
        val user = state.user ?: return
        if (state.name.isBlank()) return
        viewModelScope.launch {
            authRepository.updateProfile(user.id, state.name, state.occupation, state.timezone)
            state = state.copy(user = authRepository.currentUser(), saved = true)
        }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val user = state.user ?: return
        viewModelScope.launch {
            authRepository.deleteAccount(user.id)
            onDeleted()
        }
    }

    fun logout() = authRepository.logout()
}
