package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.RecalculateResponse
import com.example.proyectwin.data.api.dto.SimilarityDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Pares de similitud del motor de originalidad (`/v1/similarities`). */
interface SimilaritiesApi {

    @GET("similarities")
    suspend fun listar(
        @Query("proyecto_id") proyectoId: Int? = null,
        /** Con `true` devuelve la evidencia histórica (`vigente=false`) del proyecto. */
        @Query("historial") historial: Boolean? = null,
        @Query("related_to") relatedTo: String? = null,
        @Query("search") search: String? = null,
        @Query("ficha_id") fichaId: Int? = null,
        @Query("programa") programa: String? = null,
        @Query("included") included: String? = null,
    ): List<SimilarityDto>

    @GET("similarities/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): SimilarityDto

    @POST("similarities/recalculate")
    suspend fun recalcular(@Body body: Map<String, String>): RecalculateResponse

    @DELETE("similarities/{id}")
    suspend fun eliminar(@Path("id") id: Int): SimilarityDto
}
