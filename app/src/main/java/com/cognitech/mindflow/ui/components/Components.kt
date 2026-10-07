package com.cognitech.mindflow.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognitech.mindflow.R
import com.cognitech.mindflow.ui.theme.Boulder
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.CornflowerBlue
import com.cognitech.mindflow.ui.theme.Downy
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Inter
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MindGradient
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.SunsetOrange
import com.cognitech.mindflow.ui.theme.White

@Composable
fun MindFlowLogo(modifier: Modifier = Modifier, fontSize: TextUnit = 24.sp) {
    Text(
        text = "MindFlow",
        modifier = modifier,
        style = TextStyle(brush = MindGradient, fontSize = fontSize, fontWeight = FontWeight.Bold, fontFamily = Inter),
    )
}

/** Botón con el degradado azul → verde del Figma. */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fontSize: TextUnit = 16.sp,
    contentPadding: PaddingValues = PaddingValues(13.6.dp),
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MindGradient, shape, alpha = if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            // Fijo (no reactivo): el botón siempre tiene fondo de gradiente de marca.
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(19.dp))
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = fontSize)
        }
    }
}

/** Botón plano con color sólido (ej. "Guardar Cambios", "+ Crear"). */
@Composable
fun SolidButton(
    text: String,
    onClick: () -> Unit,
    background: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = Color.White, // fijo: el fondo siempre es un color sólido de marca
    fontSize: TextUnit = 13.3.sp,
    radius: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background.copy(alpha = background.alpha * if (enabled) 1f else 0.6f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = fontSize)
    }
}

/** Botón con borde (ej. "Cambiar Avatar", "Respiración 4-7-8", "Exportar PDF"). */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Mercury,
    contentColor: Color = MineShaft,
    background: Color = White,
    fontSize: TextUnit = 13.3.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    radius: Dp = 6.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 17.dp, vertical = 9.dp),
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(background, shape)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = contentColor, fontWeight = fontWeight, fontSize = fontSize)
    }
}

@Composable
fun GoogleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(White, shape)
            .border(1.dp, Mercury, shape)
            .clickable(onClick = onClick)
            .padding(14.6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painter = painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = MineShaft, fontWeight = FontWeight.SemiBold, fontSize = 15.2.sp)
    }
}

@Composable
fun DividerWithText(text: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f).padding(end = 6.8.dp), color = Mercury)
        Text(text, color = Gray, fontSize = 13.6.sp)
        HorizontalDivider(Modifier.weight(1f).padding(start = 6.8.dp), color = Mercury)
    }
}

/** Campo de texto con el estilo del Figma: fondo #F5F7FA, borde #E5E5E5, radio 8. */
@Composable
fun MindFlowInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    background: Color = CatskillWhite,
    fontSize: TextUnit = 15.2.sp,
    textColor: Color = MineShaft,
    fontWeight: FontWeight = FontWeight.Normal,
    contentPadding: PaddingValues = PaddingValues(horizontal = 17.dp, vertical = 13.8.dp),
    shape: Shape = RoundedCornerShape(8.dp),
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minHeight: Dp = Dp.Unspecified,
    isError: Boolean = false,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val obscureText = isPassword && !passwordVisible

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = singleLine,
        textStyle = TextStyle(fontSize = fontSize, color = textColor, fontFamily = Inter, fontWeight = fontWeight),
        cursorBrush = SolidColor(CornflowerBlue),
        visualTransformation = if (obscureText) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            autoCorrectEnabled = !isPassword && keyboardType != KeyboardType.Email,
        ),
        modifier = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (minHeight != Dp.Unspecified) Modifier.height(minHeight) else Modifier)
                    .background(background, shape)
                    .border(1.dp, if (isError) SunsetOrange else Mercury, shape)
                    .padding(contentPadding),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(8.dp))
                }
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Boulder, fontSize = fontSize, maxLines = if (singleLine) 1 else Int.MAX_VALUE, overflow = TextOverflow.Ellipsis)
                    }
                    inner()
                }
                if (isPassword) {
                    IconButton(onClick = { passwordVisible = !passwordVisible }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = stringResource(if (passwordVisible) R.string.common_hide_password else R.string.common_show_password),
                            tint = Boulder,
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
    labelColor: Color = MineShaft,
    labelGap: Dp = 6.39.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, color = labelColor, fontWeight = FontWeight.SemiBold, fontSize = 13.6.sp)
        Spacer(Modifier.height(labelGap))
        MindFlowInput(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            isPassword = isPassword,
            keyboardType = keyboardType,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(error, color = SunsetOrange, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Tarjeta blanca con radio 12 y sombra suave (drop-shadow 0 2 5 rgba(0,0,0,0.02)). */
@Composable
fun MindCard(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(24.dp),
    radius: Dp = 12.dp,
    background: Color = White,
    border: BorderStroke? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.04f))
            .background(background, shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .padding(padding),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

/** Tarjeta con borde izquierdo de color (tarjetas de Analíticas, alerta de Hábitos, respuesta IA). */
@Composable
fun LeftAccentCard(
    accent: Color,
    modifier: Modifier = Modifier,
    accentWidth: Dp = 4.dp,
    background: Brush = SolidColor(White),
    radius: Dp = 12.dp,
    padding: PaddingValues = PaddingValues(start = 28.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .drawBehind {
                drawRect(accent, topLeft = Offset.Zero, size = size.copy(width = accentWidth.toPx()))
            }
            .padding(padding),
        content = content,
    )
}

@Composable
fun Tag(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = CatskillWhite,
    contentColor: Color = CornflowerBlue,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    radius: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 1.6.dp),
    border: Color? = null,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .background(background, shape)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .padding(contentPadding),
    ) {
        Text(text, color = contentColor, fontSize = fontSize, fontWeight = fontWeight)
    }
}

/** Selector de filtros tipo píldora (Todos / Trabajo / Estudios / Familia). */
@Composable
fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) CornflowerBlue else White, shape)
            .border(1.dp, if (selected) CornflowerBlue else Mercury, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.8.dp, vertical = 7.4.dp),
    ) {
        Text(text, color = if (selected) Color.White else MineShaft, fontSize = 12.8.sp)
    }
}

/** Interruptor 44x24 del Figma: verde encendido / #E5E5E5 apagado, perilla blanca 20dp. */
@Composable
fun MindSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (checked) Downy else Mercury)
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .shadow(1.dp, CircleShape)
                .background(Color.White, CircleShape) // fijo: la perilla debe contrastar en ambos estados
        )
    }
}

/** Casilla cuadrada de 20dp de la lista "Hábitos Diarios". */
@Composable
fun SquareCheck(checked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(shape)
            .background(if (checked) Downy else Color.Transparent, shape)
            .border(2.dp, if (checked) Downy else Mercury, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** Casilla redonda de 24dp de la tabla de Hábitos. */
@Composable
fun RoundCheck(checked: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (checked) Downy else Color.Transparent, CircleShape)
            .border(2.dp, if (checked) Downy else Mercury, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Text("✓", color = Color.White, fontSize = 12.8.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GradientAvatar(initial: String, size: Dp = 40.dp, fontSize: TextUnit = 16.sp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(MindGradient, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = fontSize)
    }
}
