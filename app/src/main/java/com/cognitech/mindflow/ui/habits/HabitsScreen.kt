package com.cognitech.mindflow.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.data.model.Habit
import com.cognitech.mindflow.data.model.HabitFrequencies
import com.cognitech.mindflow.data.model.HabitLog
import com.cognitech.mindflow.ui.common.longLabel
import com.cognitech.mindflow.ui.components.LeftAccentCard
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.MindCard
import com.cognitech.mindflow.ui.components.MindFlowInput
import com.cognitech.mindflow.ui.components.RoundCheck
import com.cognitech.mindflow.ui.components.ScreenHeader
import com.cognitech.mindflow.ui.components.SolidButton
import com.cognitech.mindflow.ui.components.Tag
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.DoveGray
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Serenade
import com.cognitech.mindflow.ui.theme.White
import com.cognitech.mindflow.ui.theme.Zest
import java.time.LocalDate

private val ColStatus = 80.dp
private val ColName = 240.dp
private val ColCategory = 160.dp
private val ColStreak = 150.dp

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel,
    onNavigate: (MainDestination) -> Unit,
    onLogout: () -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state = viewModel.state

    MainScaffold(
        current = MainDestination.HABITS,
        onNavigate = onNavigate,
        onLogout = {
            viewModel.logout()
            onLogout()
        },
        header = { openMenu -> ScreenHeader("Hábitos", openMenu, titleSize = 20) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Tabs(state.tab, viewModel::onTabChange)
            if (state.stressDetected) StressAlert()
            when (state.tab) {
                HabitsTab.ROUTINES -> RoutinesCard(state, viewModel)
                HabitsTab.SUGGESTIONS -> SuggestionsCard(state, viewModel::addSuggestion)
                HabitsTab.HISTORY -> HistoryCard(state.history)
            }
        }
    }
}

@Composable
private fun Tabs(selected: HabitsTab, onSelect: (HabitsTab) -> Unit) {
    val underlineColor = Mercury
    Box(
        Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(underlineColor, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HabitsTab.entries.forEach { tab ->
                val active = tab == selected
                Text(
                    tab.label,
                    color = if (active) CornflowerBlue else Gray,
                    fontSize = 14.4.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onSelect(tab) }
                        .drawBehind {
                            if (active) {
                                val stroke = 3.dp.toPx()
                                drawLine(CornflowerBlue, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
                            }
                        }
                        .padding(start = 16.dp, end = 16.dp, top = 12.8.dp, bottom = 15.8.dp),
                )
            }
        }
    }
}

@Composable
private fun StressAlert() {
    LeftAccentCard(
        accent = Zest,
        background = SolidColor(Serenade),
        radius = 8.dp,
        padding = PaddingValues(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("⚠️", fontSize = 19.2.sp, color = MineShaft)
            Column {
                Text(
                    "MindFlow AI detecta niveles de estrés por encima del promedio.",
                    color = MineShaft,
                    fontSize = 15.2.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    "Hemos pausado tus tareas de alta exigencia cognitiva automáticamente. Prioriza hoy tu bienestar y descanso.",
                    color = DoveGray,
                    fontSize = 13.6.sp,
                )
            }
        }
    }
}

@Composable
private fun RoutinesCard(state: HabitsState, viewModel: HabitsViewModel) {
    MindCard(padding = PaddingValues(32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progreso Diario", color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(
                "${(state.progress * 100).toInt()}% Completado (${state.completedCount}/${state.activeHabits.size})",
                color = CornflowerBlue,
                fontSize = 15.2.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(CatskillWhite, RoundedCornerShape(10.dp))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(state.progress)
                    .fillMaxHeight()
                    .background(Downy, RoundedCornerShape(10.dp))
            )
        }
        Text(
            "Añadir Nuevo Hábito",
            color = MineShaft,
            fontSize = 17.6.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 22.dp),
        )
        Column(
            modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MindFlowInput(
                value = state.newName,
                onValueChange = viewModel::onNewNameChange,
                placeholder = "Ej. Leer 10 páginas antes de dormir...",
                background = White,
                fontSize = 14.4.sp,
                contentPadding = PaddingValues(horizontal = 17.dp, vertical = 13.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Dropdown(
                value = state.newFrequency,
                options = HabitFrequencies,
                onSelect = viewModel::onNewFrequencyChange,
                label = { it },
                radius = 8.dp,
                padding = PaddingValues(start = 21.dp, end = 16.dp, top = 13.dp, bottom = 13.dp),
                textColor = Color.Black,
                fontSize = 14.4f,
                minWidth = 112.dp,
            )
            SolidButton(
                text = "+ Crear",
                onClick = viewModel::create,
                background = Downy,
                fontSize = 14.4.sp,
                radius = 8.dp,
                enabled = state.newName.isNotBlank(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            )
        }
        HorizontalDivider(color = CatskillWhite)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MindFlowInput(
                value = state.filter,
                onValueChange = viewModel::onFilterChange,
                placeholder = "Filtrar tus hábitos...",
                fontSize = 13.6.sp,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 13.8.dp, vertical = 7.4.dp),
                leading = { Text("🔍", color = Gray, fontSize = 12.8.sp) },
                modifier = Modifier.weight(1f),
            )
            Dropdown(
                value = state.statusFilter,
                options = HabitStatusFilter.entries,
                onSelect = viewModel::onStatusFilterChange,
                label = { "Estado: ${it.label}" },
                radius = 6.dp,
                padding = PaddingValues(start = 17.8.dp, end = 12.dp, top = 7.4.dp, bottom = 7.4.dp),
                textColor = Gray,
                fontSize = 13.6f,
            )
        }
        HabitsTable(state, viewModel::toggle)
    }
}

@Composable
private fun HabitsTable(state: HabitsState, onToggle: (Habit) -> Unit) {
    val headerUnderline = Mercury
    val rowDivider = CatskillWhite
    Column(Modifier.horizontalScroll(rememberScrollState())) {
        Row(Modifier.drawBehind { drawLine(headerUnderline, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }) {
            HeaderCell("Estado", ColStatus)
            HeaderCell("Hábito", ColName)
            HeaderCell("Categoría", ColCategory)
            HeaderCell("Racha Actual", ColStreak)
        }
        if (state.visibleHabits.isEmpty()) {
            Text(
                "No hay hábitos con este filtro.",
                color = Gray,
                fontSize = 13.6.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            )
        }
        state.visibleHabits.forEachIndexed { index, habit ->
            val paused = state.isPaused(habit)
            val last = index == state.visibleHabits.lastIndex
            // key: recrea la fila si cambia el estado de pausa (la opacidad no se redibuja al variar)
            key(habit.id, paused) { Row(
                modifier = Modifier
                    .alpha(if (paused) 0.5f else 1f)
                    .then(
                        if (!last) Modifier.drawBehind {
                            drawLine(rowDivider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                        } else Modifier
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(ColStatus).padding(horizontal = 8.dp, vertical = 17.8.dp)) {
                    if (paused) {
                        Box(
                            Modifier.size(24.dp).background(Color(0xFFEAEAEA), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center,
                        ) { Text("⏸", color = Gray, fontSize = 12.8.sp) }
                    } else {
                        RoundCheck(habit.doneToday) { onToggle(habit) }
                    }
                }
                Text(
                    habit.name,
                    color = if (habit.doneToday || paused) Gray else MineShaft,
                    fontSize = 16.sp,
                    fontWeight = if (paused) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (habit.doneToday || paused) TextDecoration.LineThrough else null,
                    modifier = Modifier.width(ColName).padding(horizontal = 8.dp),
                )
                Box(Modifier.width(ColCategory).padding(horizontal = 8.dp)) {
                    Tag(
                        habit.category,
                        contentColor = MineShaft,
                        fontWeight = FontWeight.Normal,
                        radius = 6.dp,
                        border = Mercury,
                        contentPadding = PaddingValues(horizontal = 10.6.dp, vertical = 5.8.dp),
                    )
                }
                Box(Modifier.width(ColStreak).padding(horizontal = 8.dp)) {
                    when {
                        paused -> Text("Pausado por IA", color = Zest, fontSize = 12.8.sp)
                        habit.streak > 0 -> Tag(
                            "🔥 ${habit.streak} ${if (habit.streak == 1) "día" else "días"}",
                            background = Serenade,
                            contentColor = Zest,
                            fontSize = 13.6.sp,
                            fontWeight = FontWeight.SemiBold,
                            radius = 20.dp,
                            contentPadding = PaddingValues(horizontal = 9.6.dp, vertical = 4.8.dp),
                        )
                        else -> Tag(
                            "0 días",
                            contentColor = Gray,
                            fontSize = 13.6.sp,
                            fontWeight = FontWeight.SemiBold,
                            radius = 20.dp,
                            contentPadding = PaddingValues(horizontal = 9.6.dp, vertical = 4.8.dp),
                        )
                    }
                }
            } }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text,
        color = Gray,
        fontSize = 13.6.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.width(width).padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 17.dp),
    )
}

@Composable
private fun <T> Dropdown(
    value: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    radius: Dp,
    padding: PaddingValues,
    textColor: Color,
    fontSize: Float,
    minWidth: Dp = Dp.Unspecified,
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(radius)
    Box {
        Row(
            modifier = Modifier
                .then(if (minWidth != Dp.Unspecified) Modifier.width(minWidth) else Modifier)
                .background(White, shape)
                .border(1.dp, Mercury, shape)
                .clickable { expanded = true }
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label(value), color = textColor, fontSize = fontSize.sp, modifier = Modifier.weight(1f, fill = false))
            Spacer(Modifier.width(12.dp))
            Text("▾", color = textColor, fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = White) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), fontSize = 13.6.sp, color = MineShaft) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SuggestionsCard(state: HabitsState, onAdd: (HabitSuggestion) -> Unit) {
    MindCard(padding = PaddingValues(32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Sugerencias de IA", color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
        Text(
            if (state.stressDetected) "Basado en tus registros recientes, te recomendamos rutinas que reduzcan el estrés."
            else "Rutinas recomendadas por MindFlow AI para complementar tu bienestar.",
            color = Gray,
            fontSize = 13.6.sp,
        )
        if (state.suggestions.isEmpty()) {
            Text("Ya añadiste todas las sugerencias disponibles.", color = Gray, fontSize = 13.6.sp)
        }
        state.suggestions.forEachIndexed { index, suggestion ->
            if (index > 0) HorizontalDivider(color = CatskillWhite)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(suggestion.name, color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.Medium)
                    Tag(
                        suggestion.category,
                        contentColor = MineShaft,
                        fontWeight = FontWeight.Normal,
                        radius = 6.dp,
                        border = Mercury,
                        contentPadding = PaddingValues(horizontal = 10.6.dp, vertical = 5.8.dp),
                    )
                    Text(suggestion.reason, color = Gray, fontSize = 12.8.sp)
                }
                SolidButton(
                    text = "+ Añadir",
                    onClick = { onAdd(suggestion) },
                    background = Downy,
                    radius = 8.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun HistoryCard(history: List<HabitLog>) {
    MindCard(padding = PaddingValues(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Historial", color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.Bold)
        if (history.isEmpty()) {
            Text("Todavía no has completado hábitos. Márcalos en Mis Rutinas.", color = Gray, fontSize = 13.6.sp)
        }
        history.forEachIndexed { index, log ->
            if (index > 0) HorizontalDivider(color = CatskillWhite)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(LocalDate.parse(log.date).longLabel(), color = Gray, fontSize = 12.sp)
                    Text(log.habitName, color = MineShaft, fontSize = 15.2.sp, fontWeight = FontWeight.Medium)
                }
                Tag(
                    "✓ Completado",
                    background = Downy.copy(alpha = 0.15f),
                    contentColor = Color(0xFF2E9E6B),
                    fontWeight = FontWeight.SemiBold,
                    radius = 20.dp,
                    contentPadding = PaddingValues(horizontal = 9.6.dp, vertical = 4.dp),
                )
            }
        }
    }
}
