package com.cognitech.mindflow.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpos de request/response del bounded context Journal del backend (`api/v1/journal`). */

@Serializable
data class CreateJournalEntryRequest(
    val date: String,
    val title: String,
    val content: String,
    val sentiment: String,
    val category: String,
)

/** Solo los campos que el cliente necesita cachear localmente; el resto los ignora `ignoreUnknownKeys`. */
@Serializable
data class JournalEntryResponse(
    val sentiment: String,
    val aiResponse: String? = null,
    val createdAt: String,
)
