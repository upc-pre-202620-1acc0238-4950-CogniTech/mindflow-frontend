package com.cognitech.mindflow.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpos de request/response del bounded context IAM del backend (`api/v1/users`). */

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String? = null,
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String,
)

@Serializable
data class UserResponse(
    val id: Int,
    val email: String,
    val name: String? = null,
    val occupation: String? = null,
)

@Serializable
data class AuthenticatedUserResponse(
    val id: Int,
    val email: String,
    val token: String,
)

@Serializable
data class ApiErrorResponse(val error: String? = null)
