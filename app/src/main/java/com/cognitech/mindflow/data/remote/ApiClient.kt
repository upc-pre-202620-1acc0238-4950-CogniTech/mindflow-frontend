package com.cognitech.mindflow.data.remote

import com.cognitech.mindflow.BuildConfig
import com.cognitech.mindflow.data.local.SessionManager
import com.cognitech.mindflow.data.remote.dto.ApiErrorResponse
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit

/**
 * Composition root de la capa de red. Se inicializa una vez desde [com.cognitech.mindflow.MindFlowApplication]
 * con el [SessionManager] de la app, para que el interceptor pueda adjuntar el JWT a cada request.
 *
 * Los bodies se serializan/deserializan a mano con [json] (ver [toJsonBody]/[parseBody]) en vez de un
 * Converter.Factory de terceros: para el puñado de endpoints que hoy consume el frontend es más simple
 * y evita depender de una librería externa.
 */
object ApiClient {

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { ignoreUnknownKeys = true; namingStrategy = JsonNamingStrategy.SnakeCase }
    private val jsonMediaType = "application/json".toMediaType()

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
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val journalApi: JournalApi by lazy { retrofit.create(JournalApi::class.java) }
    val subscriptionApi: SubscriptionApi by lazy { retrofit.create(SubscriptionApi::class.java) }

    fun <T> toJsonBody(strategy: SerializationStrategy<T>, value: T): RequestBody =
        json.encodeToString(strategy, value).toRequestBody(jsonMediaType)

    fun <T> parseBody(strategy: DeserializationStrategy<T>, body: ResponseBody): T =
        json.decodeFromString(strategy, body.string())

    /** Extrae el mensaje de error del body `{ "error": "..." }` que devuelve el backend en 4xx. */
    fun errorMessage(response: Response<*>): String? =
        response.errorBody()?.string()?.let {
            runCatching { json.decodeFromString(ApiErrorResponse.serializer(), it).error }.getOrNull()
        }
}
