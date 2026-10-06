package com.cognitech.mindflow.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.MindGradient
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.White
import kotlinx.coroutines.delay

enum class Intervention { BREATHING_478, MICRO_MEDITATION }

/** Guía de respiración 4-7-8 (4 ciclos) o micro-meditación de 3 minutos. */
@Composable
fun InterventionDialog(type: Intervention, onDismiss: () -> Unit) {
    var phase by remember { mutableStateOf("Prepárate...") }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1f else 0.55f,
        animationSpec = tween(durationMillis = if (expanded) 4000 else 8000),
        label = "breath",
    )

    LaunchedEffect(type) {
        delay(1000)
        when (type) {
            Intervention.BREATHING_478 -> repeat(4) {
                listOf(Triple("Inhala", 4, true), Triple("Sostén", 7, true), Triple("Exhala", 8, false)).forEach { (name, secs, grow) ->
                    phase = name
                    expanded = grow
                    for (s in secs downTo 1) {
                        secondsLeft = s
                        delay(1000)
                    }
                }
            }
            Intervention.MICRO_MEDITATION -> {
                val steps = listOf(
                    "Cierra los ojos y lleva la atención a tu respiración.",
                    "Nota las sensaciones de tu cuerpo, sin juzgarlas.",
                    "Si aparece un pensamiento, obsérvalo y déjalo pasar.",
                )
                expanded = true
                for (s in 180 downTo 1) {
                    phase = steps[(180 - s) / 60]
                    secondsLeft = s
                    delay(1000)
                }
            }
        }
        phase = "¡Bien hecho! Tómate un momento antes de continuar."
        secondsLeft = 0
        finished = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(if (finished) "Cerrar" else "Terminar", color = CornflowerBlue) }
        },
        title = {
            Text(
                if (type == Intervention.BREATHING_478) "Respiración 4-7-8" else "Micro-meditación (3 min)",
                color = MineShaft,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(160.dp)
                            .scale(scale)
                            .background(MindGradient, CircleShape)
                    )
                    if (secondsLeft > 0) {
                        val label = if (type == Intervention.MICRO_MEDITATION) {
                            "%d:%02d".format(secondsLeft / 60, secondsLeft % 60)
                        } else secondsLeft.toString()
                        Text(label, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    phase,
                    color = if (finished) Gray else MineShaft,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        },
    )
}
