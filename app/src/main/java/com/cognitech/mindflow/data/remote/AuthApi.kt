package com.cognitech.mindflow.data.remote

import com.cognitech.mindflow.data.remote.dto.AuthenticatedUserResponse
import com.cognitech.mindflow.data.remote.dto.SignInRequest
import com.cognitech.mindflow.data.remote.dto.SignUpRequest
import com.cognitech.mindflow.data.remote.dto.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Espejo del `UsersController` del backend (bounded context IAM). */
interface AuthApi {

    @POST("api/v1/users/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<UserResponse>

    @POST("api/v1/users/sign-in")
    suspend fun signIn(@Body request: SignInRequest): Response<AuthenticatedUserResponse>
}
