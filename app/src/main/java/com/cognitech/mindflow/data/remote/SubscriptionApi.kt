package com.cognitech.mindflow.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.GET
import kotlinx.serialization.Serializable

/**
 * Espejo del `SubscriptionsController` del backend (bounded context Subscriptions).
 *
 * El backend crea la sesión de Stripe y conserva las claves privadas. La app solo recibe la
 * URL de Checkout y, al volver de Stripe, vuelve a consultar la suscripción confirmada.
 */
interface SubscriptionApi {

    @POST("api/v1/subscriptions/checkout")
    suspend fun createCheckout(): Response<ResponseBody>

    @GET("api/v1/subscriptions/me")
    suspend fun getMine(): Response<ResponseBody>
}

@Serializable
data class CheckoutSessionResponse(
    val checkoutUrl: String,
    val sessionId: String,
)

@Serializable
data class SubscriptionResponse(
    val plan: String = "freemium",
    val status: String = "inactive",
)
