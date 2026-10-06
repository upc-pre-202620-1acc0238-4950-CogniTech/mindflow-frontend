package com.cognitech.mindflow.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Espejo del `SubscriptionsController` del backend (bounded context Subscriptions).
 *
 * Usa el endpoint `demo/plan/{plan}` en vez de `checkout`: ese último necesita credenciales
 * reales de Stripe (cuenta de prueba propia) que el proyecto no tiene configuradas todavía.
 * El endpoint demo activa el plan sin tocar Stripe, igual que ya anuncia el texto
 * "Modo demo: no se realizará ningún cobro real." en PlansScreen.
 */
interface SubscriptionApi {

    @POST("api/v1/subscriptions/demo/plan/{plan}")
    suspend fun setDemoPlan(@Path("plan") plan: String): Response<ResponseBody>
}
