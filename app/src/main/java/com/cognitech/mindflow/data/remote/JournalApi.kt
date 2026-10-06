package com.cognitech.mindflow.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Espejo del `JournalController` del backend (bounded context Journal).
 *
 * Los bodies son [RequestBody]/[ResponseBody] crudos — [ApiClient] los serializa/parsea con
 * kotlinx.serialization (ver [ApiClient.toJsonBody]/[ApiClient.parseBody]).
 */
interface JournalApi {

    @POST("api/v1/journal/entries")
    suspend fun createEntry(@Body request: RequestBody): Response<ResponseBody>
}
