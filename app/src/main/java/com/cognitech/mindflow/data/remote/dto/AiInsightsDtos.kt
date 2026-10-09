package com.cognitech.mindflow.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HabitSuggestionsResponse(val suggestions: List<AiHabitSuggestion> = emptyList())

@Serializable
data class AiHabitSuggestion(
    val name: String,
    val category: String = "Bienestar",
    val frequency: String = "Daily",
    val reason: String = "",
)

@Serializable
data class StressCheckResponse(
    val stressLevel: String,
    val score: Int,
    val analyzedEntries: Int,
    val advice: String,
)
