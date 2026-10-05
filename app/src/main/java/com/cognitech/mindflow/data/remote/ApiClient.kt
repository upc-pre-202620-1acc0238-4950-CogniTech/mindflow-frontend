package com.cognitech.mindflow.data.remote

import com.cognitech.mindflow.BuildConfig
import com.cognitech.mindflow.data.local.SessionManager
import com.cognitech.mindflow.data.remote.dto.ApiErrorResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Composition root de la capa de red. Se inicializa una vez desde [com.cognitech.mindflow.MindFlowApplication]
 * con el [SessionManager] de la app, para que el interceptor pueda adjuntar el JWT a cada request.
 */
object ApiClient {

    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var sessionManager: SessionManager

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    if (::sessionManager.isInitialized) {
                        sessionManager.authToken?.let { addHeader("Authorization", "Bearer $it") }
                    }
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    /** Extrae el mensaje de error del body `{ "error": "..." }` que devuelve el backend en 4xx. */
    fun errorMessage(response: Response<*>): String? =
        response.errorBody()?.string()?.let {
            runCatching { json.decodeFromString(ApiErrorResponse.serializer(), it).error }.getOrNull()
        }
}
