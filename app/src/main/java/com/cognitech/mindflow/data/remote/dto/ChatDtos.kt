package com.cognitech.mindflow.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpos de request/response del bounded context Chat del backend (`api/v1/chat`). */

@Serializable
data class CreateConversationRequest(val content: String, val category: String? = null)

@Serializable
data class SendMessageRequest(val content: String)

@Serializable
data class ChatMessageResponse(val role: String, val content: String)

@Serializable
data class ConversationDetailResponse(val id: Int, val messages: List<ChatMessageResponse>)

@Serializable
data class SendMessageResponse(val aiMessage: ChatMessageResponse)
