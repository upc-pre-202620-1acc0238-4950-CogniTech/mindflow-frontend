package com.cognitech.mindflow.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST

/** Endpoints que generan insights con Gemini en el backend. */
interface AiInsightsApi {
    @POST("api/v1/habits/suggestions")
    suspend fun habitSuggestions(): Response<ResponseBody>

    @POST("api/v1/wellness/stress-check")
    suspend fun stressCheck(): Response<ResponseBody>
}
