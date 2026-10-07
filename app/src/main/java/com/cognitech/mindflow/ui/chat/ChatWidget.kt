package com.cognitech.mindflow.ui.chat

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cognitech.mindflow.MindFlowApplication
import com.cognitech.mindflow.R
import com.cognitech.mindflow.domain.model.ChatMessage
import com.cognitech.mindflow.ui.components.MindFlowInput
import com.cognitech.mindflow.ui.theme.CatskillWhite
import com.cognitech.mindflow.ui.theme.Gray
import com.cognitech.mindflow.ui.theme.Mercury
import com.cognitech.mindflow.ui.theme.MindGradient
import com.cognitech.mindflow.ui.theme.MineShaft
import com.cognitech.mindflow.ui.theme.White

/**
 * Botón flotante (abajo a la derecha) que abre el chat con MindFlow AI.
 * Debe colocarse dentro de un Box que ocupe toda la pantalla.
 */
@Composable
fun BoxScope.ChatWidget() {
    val app = LocalContext.current.applicationContext as MindFlowApplication
    val viewModel: ChatViewModel = viewModel(
        viewModelStoreOwner = LocalActivity.current as ComponentActivity,
        factory = viewModelFactory { initializer { ChatViewModel(app.chatUseCases) } },
    )
    val state = viewModel.state

    BackHandler(enabled = state.open, onBack = viewModel::close)

    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = state.open,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(1f, 1f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(1f, 1f)),
        ) {
            ChatPanel(
                state = state,
                onDraftChange = viewModel::onDraftChange,
                onSend = viewModel::send,
                onClose = viewModel::close,
            )
        }
        ChatFab(open = state.open, onClick = viewModel::toggle)
    }
}

@Composable
private fun ChatFab(open: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .background(MindGradient, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (open) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.chat_close), tint = Color.White)
        } else {
            Image(painterResource(R.drawable.ic_chat), contentDescription = stringResource(R.string.chat_open))
        }
    }
}

@Composable
private fun ChatPanel(
    state: ChatState,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onClose: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size, state.typing) {
        listState.animateScrollToItem(state.messages.size + if (state.typing) 1 else 0)
    }

    Column(
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .heightIn(max = 460.dp)
            .shadow(8.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
            .background(White, shape)
            .clip(shape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MindGradient)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("MindFlow AI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.chat_subtitle), color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Icon(
                Icons.Filled.Close,
                contentDescription = stringResource(R.string.chat_close),
                tint = Color.White,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose)
                    .padding(6.dp),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth()
                .background(CatskillWhite),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages) { MessageBubble(it) }
            if (state.typing) {
                item { MessageBubble(ChatMessage(stringResource(R.string.chat_typing), fromUser = false), muted = true) }
            }
        }

        HorizontalDivider(color = Mercury)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MindFlowInput(
                value = state.draft,
                onValueChange = onDraftChange,
                placeholder = stringResource(R.string.chat_placeholder),
                fontSize = 14.sp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
            )
            val canSend = state.draft.isNotBlank() && !state.typing
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MindGradient, CircleShape, alpha = if (canSend) 1f else 0.5f)
                    .clickable(enabled = canSend, onClick = onSend),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.common_send), tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, muted: Boolean = false) {
    val shape = if (message.fromUser) {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 4.dp, bottomEnd = 12.dp)
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.fromUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Text(
            message.text,
            color = when {
                message.fromUser -> Color.White
                muted -> Gray
                else -> MineShaft
            },
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier
                .widthIn(max = 260.dp)
                .then(
                    if (message.fromUser) Modifier.background(MindGradient, shape)
                    else Modifier.background(White, shape)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}
