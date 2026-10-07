package com.cognitech.mindflow.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R
import com.cognitech.mindflow.data.export.ReportExporter
import com.cognitech.mindflow.ui.common.displayLocale
import com.cognitech.mindflow.ui.components.LeftAccentCard
import com.cognitech.mindflow.ui.components.MainDestination
import com.cognitech.mindflow.ui.components.MainScaffold
import com.cognitech.mindflow.ui.components.MindCard
import com.cognitech.mindflow.ui.components.OutlineButton
import com.cognitech.mindflow.ui.components.ScreenHeader
import com.cognitech.mindflow.ui.home.sentimentColor
import com.cognitech.mindflow.ui.theme.Blue400
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Gray100
import com.cognitech.mindflow.ui.theme.Gray400
import com.cognitech.mindflow.ui.theme.Gray50
import com.cognitech.mindflow.ui.theme.Gray500
import com.cognitech.mindflow.ui.theme.Gray800
import com.cognitech.mindflow.ui.theme.Inter
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.Orange400
import com.cognitech.mindflow.ui.theme.Portage
import com.cognitech.mindflow.ui.theme.Purple500
import com.cognitech.mindflow.ui.theme.White
import com.cognitech.mindflow.ui.theme.Zest
import java.time.format.DateTimeFormatter

@Composable
private fun dayLabels() = stringArrayResource(R.array.weekday_short)

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onNavigate: (MainDestination) -> Unit,
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val state = viewModel.state
    val context = LocalContext.current
    var showPremiumDialog by remember { mutableStateOf(false) }

    val export: (pdf: Boolean) -> Unit = { pdf ->
        val user = state.user
        when {
            user == null -> Unit
            !user.isPremium -> showPremiumDialog = true
            pdf -> ReportExporter.sharePdf(context, user, state.entries)
            else -> ReportExporter.shareCsv(context, user, state.entries)
        }
    }

    MainScaffold(
        current = MainDestination.ANALYTICS,
        onNavigate = onNavigate,
        header = { ScreenHeader(stringResource(R.string.analytics_title)) },
    ) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { WeeklySummaryCard(state) }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(stringResource(R.string.analytics_entries_month), state.monthCount.toString(), CornflowerBlue)
                StatCard(stringResource(R.string.analytics_avg_mood), state.averageMood, Downy)
                StatCard(stringResource(R.string.analytics_best_streak), pluralStringResource(R.plurals.days_count, state.bestStreak, state.bestStreak), Zest)
            }
            FluctuationCard(state)
            WordCloudCard(state.topWords)
        }
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) { TrendCard(state.weekScores) }
        Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            ExportCard(onPdf = { export(true) }, onCsv = { export(false) })
        }
        Spacer(Modifier.height(16.dp))
    }

    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            containerColor = White,
            title = { Text(stringResource(R.string.analytics_premium_title), color = Portage, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.analytics_premium_body), color = MineShaft) },
            confirmButton = {
                TextButton(onClick = {
                    showPremiumDialog = false
                    onNavigate(MainDestination.PLANS)
                }) { Text(stringResource(R.string.analytics_view_plans), color = Portage) }
            },
            dismissButton = { TextButton(onClick = { showPremiumDialog = false }) { Text(stringResource(R.string.common_close), color = Gray) } },
        )
    }
}

@Composable
private fun WeeklySummaryCard(state: AnalyticsState) {
    val shape = RoundedCornerShape(24.dp)
    val range = DateTimeFormatter.ofPattern("d MMM", displayLocale())
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(White, shape)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.analytics_weekly_summary), color = Gray800, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp, modifier = Modifier.weight(1f))
            Text(
                "${range.format(state.weekStart)} - ${range.format(state.weekStart.plusDays(6))}".replace(".", ""),
                color = Gray500,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                Modifier
                    .size(64.dp)
                    .border(4.dp, Downy, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(state.positivity?.let { "$it%" } ?: "--", color = Downy, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
            }
            Row(Modifier.weight(1f)) {
                Text(stringResource(R.string.analytics_overall_positivity), color = Gray800, fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                Text(
                    when (val d = state.positivityDelta) {
                        null -> stringResource(R.string.analytics_no_last_week_data)
                        else -> stringResource(R.string.analytics_delta_from_last_week, if (d >= 0) "+" else "", d)
                    },
                    color = Gray500,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(Gray50, RoundedCornerShape(12.dp))
                .border(1.dp, Gray100, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Text("\"${state.insight}\"", color = Gray500, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, accent: Color) {
    LeftAccentCard(accent = accent, modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(0.04f), spotColor = Color.Black.copy(0.04f))) {
        Text(
            label.uppercase(displayLocale()),
            color = Gray,
            fontSize = 13.6.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(value, color = MineShaft, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FluctuationCard(state: AnalyticsState) {
    MindCard(
        padding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 74.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.analytics_fluctuation_title), color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
        val baselineColor = CatskillWhite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .drawBehind {
                    val stroke = 2.dp.toPx()
                    drawLine(baselineColor, Offset(0f, size.height - stroke / 2), Offset(size.width, size.height - stroke / 2), stroke)
                }
                .padding(top = 16.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            dayLabels().forEachIndexed { i, label ->
                val score = state.weekScores[i]
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (score != null) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height((140 * score).dp)
                                .background(sentimentColor(state.weekSentiments[i]), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                    Text(label, color = Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false, modifier = Modifier.padding(top = 10.dp))
                }
            }
        }
    }
}

private data class WordSlot(val x: Dp, val y: Dp, val size: TextUnit, val bold: Boolean, val color: Color)

@Composable
private fun WordCloudCard(words: List<String>) {
    // Posiciones, tamaños y colores de la nube de palabras del Figma, ordenados por frecuencia.
    // Se arma aquí (no como val de nivel superior) porque Gray400 es @Composable (reactivo a Modo Oscuro).
    val wordSlots = listOf(
        WordSlot(100.dp, 65.dp, 30.sp, true, Blue400),
        WordSlot(35.dp, 25.dp, 24.sp, true, CornflowerBlue),
        WordSlot(210.dp, 25.dp, 20.sp, false, Purple500),
        WordSlot(175.dp, 140.dp, 18.sp, false, Downy),
        WordSlot(40.dp, 105.dp, 16.sp, false, Orange400),
        WordSlot(35.dp, 150.dp, 14.sp, false, Gray400),
    )
    MindCard(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.analytics_word_cloud_title), color = MineShaft, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(White)
        ) {
            val scale = (maxWidth / 302.dp).coerceAtMost(1.2f)
            if (words.isEmpty()) {
                Text(
                    stringResource(R.string.analytics_word_cloud_empty),
                    color = Gray,
                    fontSize = 13.6.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp),
                )
            }
            words.zip(wordSlots).forEach { (word, slot) ->
                Text(
                    word,
                    color = slot.color,
                    fontSize = slot.size,
                    fontWeight = if (slot.bold) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    modifier = Modifier.offset(x = slot.x * scale - 20.dp, y = slot.y - 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TrendCard(scores: List<Float?>) {
    val shape = RoundedCornerShape(32.dp)
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Gray500, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = Inter)
    val gridLineColor = Gray100
    val trendDayLabels = dayLabels()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(White, shape)
            .padding(horizontal = 20.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        Text(
            stringResource(R.string.analytics_mood_trends_title),
            color = Gray800,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 28.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Box(Modifier.fillMaxWidth().height(260.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val left = 28.dp.toPx()
                val right = size.width - 8.dp.toPx()
                val top = 12.dp.toPx()
                val bottom = size.height - 28.dp.toPx()
                val chartH = bottom - top
                val stepX = (right - left) / 6f

                // Eje Y 0..10 y líneas guía
                for (v in 0..10 step 2) {
                    val y = bottom - chartH * v / 10f
                    drawLine(gridLineColor, Offset(left, y), Offset(right, y), 1.dp.toPx())
                    val text = measurer.measure(v.toString(), labelStyle)
                    drawText(text, topLeft = Offset(left - text.size.width - 6.dp.toPx(), y - text.size.height / 2))
                }
                trendDayLabels.forEachIndexed { i, label ->
                    val text = measurer.measure(label, labelStyle)
                    drawText(text, topLeft = Offset(left + stepX * i - text.size.width / 2, bottom + 6.dp.toPx()))
                }

                val points = scores.mapIndexedNotNull { i, s -> s?.let { Offset(left + stepX * i, bottom - chartH * (it * 10f - 0.5f).coerceIn(0f, 10f) / 10f) } }
                if (points.isEmpty()) return@Canvas

                val line = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val midX = (p0.x + p1.x) / 2
                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                    }
                }
                val fill = Path().apply {
                    addPath(line)
                    lineTo(points.last().x, bottom)
                    lineTo(points.first().x, bottom)
                    close()
                }
                drawPath(fill, Brush.verticalGradient(listOf(CornflowerBlue.copy(alpha = 0.18f), CornflowerBlue.copy(alpha = 0.02f)), startY = top, endY = bottom))
                drawPath(line, CornflowerBlue, style = Stroke(width = 2.5.dp.toPx()))
                points.forEach { drawCircle(CornflowerBlue, radius = 3.5.dp.toPx(), center = it) }
            }
            if (scores.all { it == null }) {
                Text(stringResource(R.string.analytics_no_entries_week), color = Gray, fontSize = 13.6.sp, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun ExportCard(onPdf: () -> Unit, onCsv: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Portage.copy(alpha = 0.1f), White)), shape)
            .border(BorderStroke(1.dp, Portage), shape)
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(8.6.dp),
    ) {
        Text(stringResource(R.string.analytics_export_title), color = Portage, fontSize = 18.7.sp, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.analytics_export_body),
            color = Gray,
            fontSize = 13.6.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(start = 10.dp, top = 4.dp)) {
            ExportButton(stringResource(R.string.analytics_export_pdf), onPdf)
            ExportButton(stringResource(R.string.analytics_export_csv), onCsv)
        }
    }
}

@Composable
private fun ExportButton(text: String, onClick: () -> Unit) {
    OutlineButton(
        text = text,
        onClick = onClick,
        borderColor = Portage,
        contentColor = Portage,
        radius = 8.dp,
        contentPadding = PaddingValues(horizontal = 25.dp, vertical = 13.dp),
    )
}
