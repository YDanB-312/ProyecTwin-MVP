package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.DemoSimilitudRequest
import com.example.proyectwin.data.api.dto.DemoSimilitudResponse
import com.example.proyectwin.data.api.dto.MotorConfigDto
import com.example.proyectwin.data.api.dto.MotorConfigUpdateRequest
import com.example.proyectwin.data.api.dto.PublicResumenDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Configuración del motor de similitud y endpoints públicos
 * (`/v1/config-similitud` y endpoints públicos bajo `/v1/public`).
 */
interface MotorApi {

    @GET("config-similitud")
    suspend fun obtenerConfig(): MotorConfigDto

    @PUT("config-similitud")
    suspend fun actualizarConfig(@Body body: MotorConfigUpdateRequest): MotorConfigDto

    @GET("public/resumen")
    suspend fun resumen(): PublicResumenDto

    @POST("public/demo-similitud")
    suspend fun demo(@Body body: DemoSimilitudRequest): DemoSimilitudResponse
}
