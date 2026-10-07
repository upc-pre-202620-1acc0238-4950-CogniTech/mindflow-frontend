package com.cognitech.mindflow.ui.journal

import android.app.DatePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R

import com.cognitech.mindflow.data.model.JournalEntry
import com.cognitech.mindflow.data.model.JournalFilterChips
import com.cognitech.mindflow.data.model.Sentiment
import com.cognitech.mindflow.ui.common.dominantSentiment
import com.cognitech.mindflow.ui.common.entryDateLabel
import com.cognitech.mindflow.ui.common.monthYearLabel
import com.cognitech.mindflow.ui.common.toLocalDate
import com.cognitech.mindflow.ui.components.FilterPill
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.MindCard
import com.cognitech.mindflow.ui.components.MindFlowInput
import com.cognitech.mindflow.ui.components.Tag
import com.cognitech.mindflow.ui.home.sentimentColor
import com.cognitech.mindflow.ui.theme.Boulder
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.GoldenTainoi
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.VividTangerine
import com.cognitech.mindflow.ui.theme.White
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun JournalScreen(
    viewModel: JournalViewModel,
    onNavigate: (MainDestination) -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state = viewModel.state

    MainScaffold(
        current = MainDestination.JOURNAL,
        onNavigate = onNavigate,
        header = { JournalHeader(state.query, viewModel::onQueryChange) },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FiltersCalendarCard(state, viewModel)
            Spacer(Modifier.height(8.dp))
            val filtered = state.filtered
            if (filtered.isEmpty()) {
                Text(
                    stringResource(if (state.entries.isEmpty()) R.string.journal_empty_global else R.string.journal_empty_filtered),
                    color = Gray,
                    fontSize = 13.6.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
            filtered.forEach { EntryCard(it) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun JournalHeader(query: String, onQueryChange: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().background(White)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 13.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.journal_title), color = MineShaft, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            MindFlowInput(
                value = query,
                onValueChange = onQueryChange,
                placeholder = stringResource(R.string.journal_search_placeholder),
                fontSize = 13.6.sp,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 17.dp, vertical = 9.dp),
                leading = { Text("🔍", fontSize = 13.sp) },
                modifier = Modifier.width(300.dp),
            )
        }
        HorizontalDivider(color = Mercury)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersCalendarCard(state: JournalState, viewModel: JournalViewModel) {
    MindCard(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MindFlowInput(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = stringResource(R.string.journal_search_placeholder_card),
            background = White,
            fontSize = 14.4.sp,
            contentPadding = PaddingValues(13.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            JournalFilterChips.forEach { chip ->
                FilterPill(chip, selected = state.category == chip) { viewModel.onCategoryChange(chip) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SentimentSelector(state.sentiment, viewModel::onSentimentChange, Modifier.weight(1f))
            DateField(state.date, viewModel::onDateChange, Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(monthYearLabel(state.month.atDay(1)), color = MineShaft, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(
                stringResource(if (state.month == java.time.YearMonth.now()) R.string.journal_prev_month else R.string.journal_current_month),
                color = CornflowerBlue,
                fontSize = 12.8.sp,
                modifier = Modifier.clickable(onClick = viewModel::toggleMonth),
            )
        }
        MoodCalendar(state, viewModel::onDayClick)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(9.8.dp, Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.journal_legend_positive), color = Downy, fontSize = 12.sp)
            Text(stringResource(R.string.journal_legend_neutral), color = GoldenTainoi, fontSize = 12.sp)
            Text(stringResource(R.string.journal_legend_negative), color = VividTangerine, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SentimentSelector(value: String?, onChange: (String?) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(6.dp)
    val options = listOf(null, Sentiment.POSITIVE, Sentiment.NEUTRAL, Sentiment.NEGATIVE)
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White, shape)
                .border(1.dp, Mercury, shape)
                .clickable { expanded = true }
                .padding(start = 13.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.journal_sentiment_filter, value?.let(Sentiment::label) ?: stringResource(R.string.common_all)),
                color = Gray,
                fontSize = 12.8.sp,
                lineHeight = 15.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Text("▾", color = Gray, fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = White) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option?.let(Sentiment::label) ?: stringResource(R.string.common_all), fontSize = 13.6.sp, color = MineShaft) },
                    onClick = {
                        onChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun DateField(value: LocalDate?, onChange: (LocalDate?) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = modifier
            .background(White, shape)
            .border(1.dp, Mercury, shape)
            .clickable {
                val initial = value ?: LocalDate.now()
                DatePickerDialog(
                    context,
                    { _, y, m, d -> onChange(LocalDate.of(y, m + 1, d)) },
                    initial.year, initial.monthValue - 1, initial.dayOfMonth,
                ).show()
            }
            .padding(9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            value?.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) ?: "mm/dd/yyyy",
            color = if (value != null) MineShaft else Gray,
            fontSize = 12.8.sp,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text("✕", color = Gray, fontSize = 12.sp, modifier = Modifier.clickable { onChange(null) }.padding(horizontal = 4.dp))
        } else {
            Image(painterResource(R.drawable.ic_calendar), contentDescription = stringResource(R.string.journal_pick_date), modifier = Modifier.size(width = 12.8.dp, height = 12.dp))
        }
    }
}

@Composable
private fun MoodCalendar(state: JournalState, onDayClick: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val first = state.month.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val days = state.month.lengthOfMonth()
    val byDay = state.entries.groupBy { it.createdAt.toLocalDate() }

    Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        stringArrayResource(R.array.weekday_initials).forEach {
            Text(
                it,
                color = Gray,
                fontSize = 12.8.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
    val cells = offset + days
    val rows = (cells + 6) / 7
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0 until 7) {
                    val dayNumber = row * 7 + col - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f)) {
                        if (dayNumber in 1..days) {
                            val date = state.month.atDay(dayNumber)
                            val sentiment = byDay[date]?.dominantSentiment()
                            CalendarDay(
                                day = dayNumber,
                                sentiment = sentiment,
                                isToday = date == today,
                                isSelected = date == state.date,
                                isFuture = date.isAfter(today),
                                onClick = { onDayClick(date) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    sentiment: String?,
    isToday: Boolean,
    isSelected: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val background = when {
        sentiment != null -> sentimentColor(sentiment)
        isFuture -> White
        else -> CatskillWhite
    }
    val textColor = when {
        // Fijo: el fondo de este día es un color sólido de sentimiento (sentimentColor), no una superficie.
        sentiment == Sentiment.NEGATIVE -> Color.White
        sentiment != null -> MineShaft
        else -> Boulder
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(background, shape)
            .then(
                when {
                    isToday || isSelected -> Modifier.border(2.dp, if (isSelected) CornflowerBlue else MineShaft, shape)
                    isFuture -> Modifier.border(1.dp, CatskillWhite, shape)
                    else -> Modifier
                }
            )
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(day.toString(), color = textColor, fontSize = 13.6.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EntryCard(entry: JournalEntry) {
    MindCard(
        padding = PaddingValues(25.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Mercury),
        verticalArrangement = Arrangement.spacedBy(7.3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(painterResource(R.drawable.ic_entry_date), contentDescription = null, modifier = Modifier.size(width = 10.dp, height = 11.dp))
            Text(
                " " + entry.createdAt.entryDateLabel(),
                color = Gray,
                fontSize = 13.6.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            Tag(
                entry.category,
                fontWeight = FontWeight.SemiBold,
                contentPadding = PaddingValues(horizontal = 9.6.dp, vertical = 4.8.dp),
            )
        }
        HorizontalDivider(color = CatskillWhite, modifier = Modifier.padding(bottom = 1.4.dp))
        Text(entry.title, color = MineShaft, fontSize = 17.6.sp, fontWeight = FontWeight.SemiBold)
        Text(entry.content, color = MineShaft, fontSize = 15.2.sp, lineHeight = 24.32.sp)
    }
}
