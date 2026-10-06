package com.cognitech.mindflow.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Espejo del `ChatController` del backend (bounded context Chat). Solo cubre lo que el widget de
 * chat flotante necesita: crear la conversación con el primer mensaje y enviar los siguientes.
 */
interface ChatApi {

    @POST("api/v1/chat/conversations")
    suspend fun createConversation(@Body request: RequestBody): Response<ResponseBody>

    @POST("api/v1/chat/conversations/{id}/messages")
    suspend fun sendMessage(@Path("id") conversationId: Int, @Body request: RequestBody): Response<ResponseBody>
}
