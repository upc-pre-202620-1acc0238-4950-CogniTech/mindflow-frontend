package com.cognitech.mindflow.ui.habits

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognitech.mindflow.data.model.Habit
import com.cognitech.mindflow.data.model.HabitCategories
import com.cognitech.mindflow.data.model.HabitFrequencies
import com.cognitech.mindflow.data.model.HabitLog
import com.cognitech.mindflow.data.model.Sentiment
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.data.repository.HabitRepository
import com.cognitech.mindflow.data.repository.JournalRepository
import com.cognitech.mindflow.data.remote.ApiClient
import com.cognitech.mindflow.data.remote.dto.HabitSuggestionsResponse
import com.cognitech.mindflow.data.remote.dto.StressCheckResponse
import kotlinx.coroutines.launch

enum class HabitsTab(val label: String) { ROUTINES("Mis Rutinas"), SUGGESTIONS("Sugerencias de IA"), HISTORY("Historial") }

enum class HabitStatusFilter(val label: String) { ALL("Todos"), COMPLETED("Completados"), PENDING("Pendientes"), PAUSED("Pausados") }

data class HabitSuggestion(val name: String, val category: String, val reason: String)

data class HabitsState(
    val tab: HabitsTab = HabitsTab.ROUTINES,
    val habits: List<Habit> = emptyList(),
    val history: List<HabitLog> = emptyList(),
    val stressDetected: Boolean = false,
    val newName: String = "",
    val newFrequency: String = HabitFrequencies.first(),
    val filter: String = "",
    val statusFilter: HabitStatusFilter = HabitStatusFilter.ALL,
    val aiSuggestions: List<HabitSuggestion> = emptyList(),
    val aiAdvice: String? = null,
    val suggestionsLoading: Boolean = false,
    val suggestionsLoaded: Boolean = false,
) {
    /** Con estrés alto, la IA pausa las tareas de alta exigencia cognitiva (categoría Estudios). */
    fun isPaused(habit: Habit) = stressDetected && habit.category == HabitCategories.STUDY

    val activeHabits get() = habits.filterNot(::isPaused)
    val completedCount get() = activeHabits.count { it.doneToday }
    val progress get() = if (activeHabits.isEmpty()) 0f else completedCount.toFloat() / activeHabits.size

    val visibleHabits
        get() = habits.filter { h ->
            (filter.isBlank() || h.name.contains(filter, true) || h.category.contains(filter, true)) &&
                when (statusFilter) {
                    HabitStatusFilter.ALL -> true
                    HabitStatusFilter.COMPLETED -> h.doneToday && !isPaused(h)
                    HabitStatusFilter.PENDING -> !h.doneToday && !isPaused(h)
                    HabitStatusFilter.PAUSED -> isPaused(h)
                }
        }

    val suggestions
        get() = aiSuggestions.filter { s -> habits.none { it.name.equals(s.name, ignoreCase = true) } }

    companion object {
        val FALLBACK_SUGGESTIONS = listOf(
            HabitSuggestion("Caminar 15 minutos al aire libre", HabitCategories.PHYSICAL, "Moverte al aire libre reduce el cortisol y mejora tu ánimo."),
            HabitSuggestion("Escribir 3 cosas por las que estás agradecido", HabitCategories.MENTAL, "La gratitud diaria ayuda a equilibrar los pensamientos negativos."),
            HabitSuggestion("Dormir antes de las 11 PM", HabitCategories.SLEEP, "Un descanso regular mejora tu concentración al día siguiente."),
            HabitSuggestion("Respiración 4-7-8 antes de estudiar", HabitCategories.MENTAL, "Calma el sistema nervioso antes de tareas exigentes."),
            HabitSuggestion("Leer 10 páginas antes de dormir", HabitCategories.STUDY, "Reemplaza el uso de pantallas por una rutina relajante."),
        )
    }
}

class HabitsViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
    private val journalRepository: JournalRepository,
) : ViewModel() {

    var state by mutableStateOf(HabitsState())
        private set

    fun load() {
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            val recent = journalRepository.listByUser(user.id).take(3)
            state = state.copy(
                habits = habitRepository.listByUser(user.id),
                history = habitRepository.history(user.id),
                stressDetected = recent.count { it.sentiment == Sentiment.NEGATIVE } >= 2,
            )
        }
    }

    fun onTabChange(tab: HabitsTab) {
        state = state.copy(tab = tab)
        if (tab == HabitsTab.SUGGESTIONS && !state.suggestionsLoaded && !state.suggestionsLoading) {
            loadAiInsights()
        }
    }

    private fun loadAiInsights() {
        viewModelScope.launch {
            state = state.copy(suggestionsLoading = true)
            val suggestions = runCatching {
                val response = ApiClient.aiInsightsApi.habitSuggestions()
                val body = response.body() ?: error("No response")
                check(response.isSuccessful)
                ApiClient.parseBody(HabitSuggestionsResponse.serializer(), body).suggestions.map {
                    HabitSuggestion(it.name, it.category, it.reason)
                }
            }.getOrElse { emptyList() }

            val wellness = runCatching {
                val response = ApiClient.aiInsightsApi.stressCheck()
                val body = response.body() ?: error("No response")
                check(response.isSuccessful)
                ApiClient.parseBody(StressCheckResponse.serializer(), body)
            }.getOrNull()

            state = state.copy(
                aiSuggestions = suggestions.ifEmpty { HabitsState.FALLBACK_SUGGESTIONS },
                aiAdvice = wellness?.advice,
                stressDetected = wellness?.stressLevel == "high" || state.stressDetected,
                suggestionsLoading = false,
                suggestionsLoaded = true,
            )
        }
    }
    fun onNewNameChange(value: String) { state = state.copy(newName = value) }
    fun onNewFrequencyChange(value: String) { state = state.copy(newFrequency = value) }
    fun onFilterChange(value: String) { state = state.copy(filter = value) }
    fun onStatusFilterChange(value: HabitStatusFilter) { state = state.copy(statusFilter = value) }

    fun create() {
        if (state.newName.isBlank()) return
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            habitRepository.create(user.id, state.newName, state.newFrequency)
            state = state.copy(newName = "")
            load()
        }
    }

    fun addSuggestion(suggestion: HabitSuggestion) {
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            habitRepository.create(user.id, suggestion.name, HabitFrequencies.first())
            load()
        }
    }

    fun toggle(habit: Habit) {
        if (state.isPaused(habit)) return
        viewModelScope.launch {
            habitRepository.toggleToday(habit.id)
            load()
        }
    }

    fun logout() = authRepository.logout()
}
