package com.cognitech.mindflow.infrastructure.adapter

import com.cognitech.mindflow.data.ai.LocalAiResponder
import com.cognitech.mindflow.data.remote.ApiClient
import com.cognitech.mindflow.data.remote.dto.ConversationDetailResponse
import com.cognitech.mindflow.data.remote.dto.CreateConversationRequest
import com.cognitech.mindflow.data.remote.dto.SendMessageRequest
import com.cognitech.mindflow.data.remote.dto.SendMessageResponse
import com.cognitech.mindflow.domain.model.ChatMessage
import com.cognitech.mindflow.domain.port.ChatResponder

/**
 * Habla con el backend (Gemini real, con historial de la conversación) en vez de la heurística
 * local. La conversación se crea perezosamente con el primer mensaje y su id se reutiliza
 * mientras viva el proceso — igual que [com.cognitech.mindflow.ui.chat.ChatViewModel], que ya
 * mantiene el chat a nivel de Activity. Si el backend no responde, cae a [fallback] para que el
 * chat nunca se quede mudo.
 */
class RemoteChatAdapter(private val fallback: LocalAiResponder) : ChatResponder {
    private var conversationId: Int? = null

    override suspend fun reply(text: String): ChatMessage {
        val remoteReply = runCatching { sendRemote(text) }.getOrNull()
        if (remoteReply != null) return ChatMessage(remoteReply, fromUser = false)

        val sentiment = fallback.detectSentiment(text)
        return ChatMessage(fallback.respond(text, sentiment), fromUser = false)
    }

    private suspend fun sendRemote(text: String): String? {
        val id = conversationId
        return if (id == null) {
            val body = ApiClient.toJsonBody(CreateConversationRequest.serializer(), CreateConversationRequest(text))
            val response = ApiClient.chatApi.createConversation(body)
            val responseBody = response.body() ?: return null
            if (!response.isSuccessful) return null
            val conversation = ApiClient.parseBody(ConversationDetailResponse.serializer(), responseBody)
            conversationId = conversation.id
            conversation.messages.lastOrNull { it.role == "assistant" }?.content
        } else {
            val body = ApiClient.toJsonBody(SendMessageRequest.serializer(), SendMessageRequest(text))
            val response = ApiClient.chatApi.sendMessage(id, body)
            val responseBody = response.body() ?: return null
            if (!response.isSuccessful) return null
            ApiClient.parseBody(SendMessageResponse.serializer(), responseBody).aiMessage.content
        }
    }
}
