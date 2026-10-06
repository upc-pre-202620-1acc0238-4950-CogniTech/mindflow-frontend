package com.cognitech.mindflow.ui.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognitech.mindflow.data.ai.LocalAiResponder
import com.cognitech.mindflow.data.model.JournalEntry
import com.cognitech.mindflow.data.model.Sentiment
import com.cognitech.mindflow.data.model.User
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.data.repository.HabitRepository
import com.cognitech.mindflow.data.repository.JournalRepository
import com.cognitech.mindflow.ui.common.dominantSentiment
import com.cognitech.mindflow.ui.common.sentimentScore
import com.cognitech.mindflow.ui.common.toLocalDate
import com.cognitech.mindflow.ui.common.weekStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

data class AnalyticsState(
    val user: User? = null,
    val entries: List<JournalEntry> = emptyList(),
    val weekStart: LocalDate = weekStart(),
    val positivity: Int? = null,
    val positivityDelta: Int? = null,
    val insight: String = "",
    val monthCount: Int = 0,
    val averageMood: String = "Sin datos",
    val bestStreak: Int = 0,
    /** Puntaje 0..1 por día (Lun..Dom) y su sentimiento predominante; null si no hay registros. */
    val weekScores: List<Float?> = List(7) { null },
    val weekSentiments: List<String?> = List(7) { null },
    val topWords: List<String> = emptyList(),
)

class AnalyticsViewModel(
    private val authRepository: AuthRepository,
    private val journalRepository: JournalRepository,
    private val habitRepository: HabitRepository,
    private val aiResponder: LocalAiResponder,
) : ViewModel() {

    var state by mutableStateOf(AnalyticsState())
        private set

    fun load() {
        viewModelScope.launch {
            val user = authRepository.currentUser() ?: return@launch
            val entries = journalRepository.listByUser(user.id)
            val habits = habitRepository.listByUser(user.id)
            val monday = weekStart()
            val thisWeek = entries.filter { !it.createdAt.toLocalDate().isBefore(monday) }
            val lastWeek = entries.filter {
                val d = it.createdAt.toLocalDate()
                !d.isBefore(monday.minusDays(7)) && d.isBefore(monday)
            }
            val month = YearMonth.now()
            val monthEntries = entries.filter { YearMonth.from(it.createdAt.toLocalDate()) == month }
            val byDay = thisWeek.groupBy { it.createdAt.toLocalDate() }
            val days = (0..6).map { monday.plusDays(it.toLong()) }
            val positivity = thisWeek.positivity()

            state = AnalyticsState(
                user = user,
                entries = entries,
                weekStart = monday,
                positivity = positivity,
                positivityDelta = lastWeek.positivity()?.let { last -> positivity?.minus(last) },
                insight = aiResponder.weeklyInsight(
                    positive = thisWeek.count { it.sentiment == Sentiment.POSITIVE },
                    neutral = thisWeek.count { it.sentiment == Sentiment.NEUTRAL },
                    negative = thisWeek.count { it.sentiment == Sentiment.NEGATIVE },
                    topCategory = thisWeek.groupingBy { it.category }.eachCount().maxByOrNull { it.value }?.key,
                ),
                monthCount = monthEntries.size,
                averageMood = monthEntries.dominantSentiment()?.let(Sentiment::label) ?: "Sin datos",
                bestStreak = habits.maxOfOrNull { it.streak } ?: 0,
                weekScores = days.map { d -> byDay[d]?.map { sentimentScore(it.sentiment) }?.average()?.toFloat() },
                weekSentiments = days.map { d -> byDay[d]?.dominantSentiment() },
                topWords = topWords(entries.filter { it.createdAt.toLocalDate().isAfter(LocalDate.now().minusDays(30)) }),
            )
        }
    }

    fun logout() = authRepository.logout()

    // Promedio ponderado (positivo=100%, neutral=50%, negativo=25%) vía sentimentScore, consistente
    // con el resto de la UI (p. ej. las barras del Resumen Semanal de Ánimo). Antes solo contaba
    // entradas positivas sobre el total, así que las neutrales valían 0% igual que las negativas
    // y el puntaje parecía sesgado hacia lo negativo aunque no hubiera entradas negativas.
    private fun List<JournalEntry>.positivity(): Int? =
        if (isEmpty()) null else (map { sentimentScore(it.sentiment) }.average() * 100).roundToInt()

    private fun topWords(entries: List<JournalEntry>): List<String> =
        entries.flatMap { e ->
            e.content.lowercase()
                .split(Regex("[^\\p{L}]+"))
                .filter { it.length > 3 && it !in STOPWORDS }
        }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key.replaceFirstChar(Char::uppercase) }

    private companion object {
        val STOPWORDS = setOf(
            "para", "pero", "como", "este", "esta", "esto", "estos", "estas", "porque", "cuando", "donde", "desde",
            "hasta", "sobre", "entre", "también", "tambien", "muy", "más", "mas", "todo", "toda", "todos", "todas",
            "algo", "nada", "hoy", "ayer", "mañana", "siento", "sentí", "senti", "estoy", "estaba", "tengo", "tenía",
            "había", "hacer", "hice", "sido", "ser", "fue", "eso", "esa", "ese", "aunque", "mucho", "mucha", "poco",
            "otra", "otro", "cada", "solo", "sólo", "bien", "mismo", "misma", "ahora", "luego", "antes", "después",
            "despues", "que", "con", "los", "las", "del", "una", "uno", "unos", "unas", "sus", "mis", "nos", "les",
        )
    }
}
