package com.cognitech.mindflow.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.data.model.Habit
import com.cognitech.mindflow.data.model.JournalCategories
import com.cognitech.mindflow.data.model.JournalEntry
import com.cognitech.mindflow.data.model.Sentiment
import com.cognitech.mindflow.ui.common.dominantSentiment
import com.cognitech.mindflow.ui.common.longLabel
import com.cognitech.mindflow.ui.common.relativeLabel
import com.cognitech.mindflow.ui.common.sentimentScore
import com.cognitech.mindflow.ui.common.toLocalDate
import com.cognitech.mindflow.ui.common.weekStart
import com.cognitech.mindflow.ui.components.GradientAvatar
import com.cognitech.mindflow.ui.components.GradientButton
import com.cognitech.mindflow.ui.components.Intervention
import com.cognitech.mindflow.ui.components.InterventionDialog
import com.cognitech.mindflow.ui.components.LeftAccentCard
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.MenuButton
import com.cognitech.mindflow.ui.components.MindCard
import com.cognitech.mindflow.ui.components.MindFlowInput
import com.cognitech.mindflow.ui.components.OutlineButton
import com.cognitech.mindflow.ui.components.SquareCheck
import com.cognitech.mindflow.ui.components.Tag
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gallery
import com.cognitech.mindflow.ui.theme.GoldenTainoi
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MindGradientSoft
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Serenade
import com.cognitech.mindflow.ui.theme.VividTangerine
import com.cognitech.mindflow.ui.theme.White
import com.cognitech.mindflow.ui.theme.Zest
import java.time.LocalDate

private const val RECENT_LIMIT = 3

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (MainDestination) -> Unit,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state = viewModel.state
    var intervention by remember { mutableStateOf<Intervention?>(null) }
    val firstName = state.user?.name?.substringBefore(" ").orEmpty().ifBlank { "Usuario" }

    MainScaffold(
        current = MainDestination.DASHBOARD,
        onNavigate = onNavigate,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        header = { openMenu ->
            HomeHeader(
                firstName = firstName,
                initial = state.user?.initial ?: "U",
                onMenuClick = openMenu,
                onAvatarClick = { onNavigate(MainDestination.SETTINGS) },
            )
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            MoodCard(
                draft = state.draft,
                category = state.category,
                saving = state.saving,
                lastAiResponse = state.lastAiResponse,
                entries = state.entries,
                showAll = state.showAllHistory,
                onDraftChange = viewModel::onDraftChange,
                onCategoryChange = viewModel::onCategoryChange,
                onSave = viewModel::save,
                onToggleHistory = viewModel::toggleHistory,
            )
            WeeklyMoodCard(state.entries, onFullReport = { onNavigate(MainDestination.ANALYTICS) })
            MindCard(
                padding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Intervenciones Rápidas",
                    color = MineShaft,
                    fontSize = 17.6.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                InterventionButton("Respiración 4-7-8") { intervention = Intervention.BREATHING_478 }
                InterventionButton("Micro-meditación (3 min)") { intervention = Intervention.MICRO_MEDITATION }
            }
            DailyHabitsCard(state.habits.take(3), onToggle = viewModel::toggleHabit)
            Spacer(Modifier.height(8.dp))
        }
    }

    intervention?.let { InterventionDialog(it, onDismiss = { intervention = null }) }
}

@Composable
private fun HomeHeader(firstName: String, initial: String, onMenuClick: () -> Unit, onAvatarClick: () -> Unit) {
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
            Column(Modifier.weight(1f)) {
                Text("Hola, $firstName", color = MineShaft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${LocalDate.now().longLabel()} - Tu IA está lista para escucharte.",
                    color = Gray,
                    fontSize = 13.6.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            GradientAvatar(initial, modifier = Modifier.clickable(onClick = onAvatarClick))
        }
        HorizontalDivider(color = Mercury)
    }
}

@Composable
private fun MoodCard(
    draft: String,
    category: String,
    saving: Boolean,
    lastAiResponse: String?,
    entries: List<JournalEntry>,
    showAll: Boolean,
    onDraftChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSave: () -> Unit,
    onToggleHistory: () -> Unit,
) {
    MindCard(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("¿Cómo te sientes en este momento?", color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.SemiBold)
            Row(
                modifier = Modifier
                    .background(CatskillWhite, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.8.dp, vertical = 4.8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(Modifier.size(6.dp).background(Downy, CircleShape))
                Text("MindFlow AI Activa", color = CornflowerBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        MindFlowInput(
            value = draft,
            onValueChange = onDraftChange,
            placeholder = "Escribe aquí tus pensamientos. Este es un espacio seguro y encriptado...",
            singleLine = false,
            minHeight = 100.dp,
            contentPadding = PaddingValues(17.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategorySelector(category, onCategoryChange, Modifier.weight(1f).padding(end = 8.dp))
            GradientButton(
                text = "Guardar Registro",
                onClick = onSave,
                enabled = draft.isNotBlank(),
                loading = saving,
                fontSize = 13.3.sp,
                contentPadding = PaddingValues(horizontal = 19.2.dp, vertical = 9.6.dp),
            )
        }
        LeftAccentCard(
            accent = Downy,
            accentWidth = 3.dp,
            background = MindGradientSoft,
            radius = 8.dp,
            padding = PaddingValues(start = 22.2.dp, end = 19.2.dp, top = 27.2.dp, bottom = 19.2.dp),
        ) {
            Text("✨ MindFlow AI", color = CornflowerBlue, fontSize = 14.4.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                lastAiResponse
                    ?: "Hola, estoy aquí para escucharte. Cuéntame cómo te sientes hoy y te daré una respuesta personalizada.",
                color = MineShaft,
                fontSize = 14.4.sp,
                lineHeight = 21.6.sp,
            )
        }
        HorizontalDivider(color = Mercury)
        Column(
            modifier = Modifier.padding(top = 17.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Conversaciones Recientes", color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (showAll) "Ver solo las recientes" else "Ver historial completo de conversaciones",
                    color = CornflowerBlue,
                    fontSize = 12.8.sp,
                    modifier = Modifier.clickable(enabled = entries.size > RECENT_LIMIT, onClick = onToggleHistory),
                )
            }
            if (entries.isEmpty()) {
                Text(
                    "Aún no tienes registros. Escribe cómo te sientes para empezar.",
                    color = Gray,
                    fontSize = 13.6.sp,
                    modifier = Modifier.padding(horizontal = 13.8.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                (if (showAll) entries else entries.take(RECENT_LIMIT)).forEach { RecentItem(it) }
            }
        }
    }
}

@Composable
private fun CategorySelector(category: String, onCategoryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gallery, shape)
                .border(1.dp, Mercury, shape)
                .clickable { expanded = true }
                .padding(start = 13.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(category, color = MineShaft, fontSize = 13.6.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f), maxLines = 1)
            Text("▾", color = MineShaft, fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = White) {
            JournalCategories.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 13.6.sp, color = MineShaft) },
                    onClick = {
                        onCategoryChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentItem(entry: JournalEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(13.8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.createdAt.relativeLabel(), color = Gray, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Tag(entry.category)
        }
        Text(
            entry.content,
            color = MineShaft,
            fontSize = 13.6.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WeeklyMoodCard(entries: List<JournalEntry>, onFullReport: () -> Unit) {
    val monday = weekStart()
    val today = LocalDate.now()
    val byDay = entries.groupBy { it.createdAt.toLocalDate() }
    val labels = listOf("L", "M", "X", "J", "V", "S", "D")
    val maxBar = 78f

    MindCard(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Resumen Semanal de Ánimo", color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.SemiBold)
            Text("Ver Reporte Completo", color = CornflowerBlue, fontSize = 13.6.sp, modifier = Modifier.clickable(onClick = onFullReport))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            labels.forEachIndexed { index, label ->
                val day = monday.plusDays(index.toLong())
                val dayEntries = byDay[day].orEmpty()
                val (height, color) = when {
                    dayEntries.isNotEmpty() -> {
                        val avg = dayEntries.map { sentimentScore(it.sentiment) }.average().toFloat()
                        maxBar * avg to sentimentColor(dayEntries.dominantSentiment())
                    }
                    !day.isAfter(today) -> maxBar * 0.4f to CatskillWhite
                    else -> 0f to Color.Transparent
                }
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (height > 0f) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(height.dp)
                                .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Text(label, color = Gray, fontSize = 11.2.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
                }
            }
        }
    }
}

// No-reactivo: son colores fijos de acento por sentimiento (como un badge), no superficies de la app.
fun sentimentColor(sentiment: String?): Color = when (sentiment) {
    Sentiment.POSITIVE -> Downy
    Sentiment.NEGATIVE -> VividTangerine
    Sentiment.NEUTRAL -> GoldenTainoi
    else -> Color(0xFFF5F7FA)
}

@Composable
private fun InterventionButton(text: String, onClick: () -> Unit) {
    OutlineButton(
        text = text,
        onClick = onClick,
        borderColor = CornflowerBlue,
        contentColor = CornflowerBlue,
        fontSize = 14.4.sp,
        fontWeight = FontWeight.Medium,
        radius = 8.dp,
        contentPadding = PaddingValues(horizontal = 17.dp, vertical = 13.8.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DailyHabitsCard(habits: List<Habit>, onToggle: (Habit) -> Unit) {
    MindCard(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Hábitos Diarios", color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.SemiBold)
        Column {
            habits.forEachIndexed { index, habit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.8.dp, bottom = 13.8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SquareCheck(habit.doneToday) { onToggle(habit) }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        habit.name,
                        color = if (habit.doneToday) Gray else MineShaft,
                        fontSize = 14.4.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (habit.doneToday) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f),
                    )
                    StreakBadge(habit)
                }
                if (index < habits.lastIndex) HorizontalDivider(color = CatskillWhite)
            }
        }
    }
}

@Composable
private fun StreakBadge(habit: Habit) {
    when {
        habit.streak > 0 && habit.doneToday -> Tag(
            text = "🔥 ${habit.streak} ${if (habit.streak == 1) "día" else "días"}",
            background = Serenade,
            contentColor = Zest,
            fontWeight = FontWeight.SemiBold,
            radius = 20.dp,
            contentPadding = PaddingValues(horizontal = 9.6.dp, vertical = 3.2.dp),
        )
        habit.streak > 0 -> Tag(
            text = "🔥 ${habit.streak} ${if (habit.streak == 1) "día" else "días"}",
            background = White,
            contentColor = Gray,
            fontWeight = FontWeight.SemiBold,
            radius = 20.dp,
            border = Mercury,
            contentPadding = PaddingValues(horizontal = 10.6.dp, vertical = 4.2.dp),
        )
    }
}
