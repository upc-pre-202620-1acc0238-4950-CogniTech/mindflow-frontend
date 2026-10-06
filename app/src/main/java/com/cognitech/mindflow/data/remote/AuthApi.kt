package com.cognitech.mindflow.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Espejo del `UsersController` del backend (bounded context IAM).
 *
 * Los bodies son [RequestBody]/[ResponseBody] crudos — [ApiClient] los serializa/parsea con
 * kotlinx.serialization (ver [ApiClient.toJsonBody]/[ApiClient.parseBody]).
 */
interface AuthApi {

    @POST("api/v1/users/sign-up")
    suspend fun signUp(@Body request: RequestBody): Response<ResponseBody>

    @POST("api/v1/users/sign-in")
    suspend fun signIn(@Body request: RequestBody): Response<ResponseBody>
}
