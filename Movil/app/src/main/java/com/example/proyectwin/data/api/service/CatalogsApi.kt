package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.KnowledgeNetworkDto
import com.example.proyectwin.data.api.dto.KnowledgeNetworkRequest
import com.example.proyectwin.data.api.dto.TrainingProgramDto
import com.example.proyectwin.data.api.dto.TrainingProgramRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Redes de conocimiento y programas de formación (`/v1/knowledge-networks`, `/v1/training-programs`). */
interface CatalogsApi {

    @GET("knowledge-networks")
    suspend fun listarRedes(): List<KnowledgeNetworkDto>

    @GET("knowledge-networks/{id}")
    suspend fun obtenerRed(@Path("id") id: Int): KnowledgeNetworkDto

    @POST("knowledge-networks")
    suspend fun crearRed(@Body body: KnowledgeNetworkRequest): KnowledgeNetworkDto

    @PUT("knowledge-networks/{id}")
    suspend fun actualizarRed(@Path("id") id: Int, @Body body: KnowledgeNetworkRequest): KnowledgeNetworkDto

    @DELETE("knowledge-networks/{id}")
    suspend fun eliminarRed(@Path("id") id: Int): KnowledgeNetworkDto

    @GET("training-programs")
    suspend fun listarProgramas(@Query("included") included: String? = null): List<TrainingProgramDto>

    @GET("training-programs/{id}")
    suspend fun obtenerPrograma(@Path("id") id: Int): TrainingProgramDto

    @POST("training-programs")
    suspend fun crearPrograma(@Body body: TrainingProgramRequest): TrainingProgramDto

    @PUT("training-programs/{id}")
    suspend fun actualizarPrograma(@Path("id") id: Int, @Body body: TrainingProgramRequest): TrainingProgramDto

    @DELETE("training-programs/{id}")
    suspend fun eliminarPrograma(@Path("id") id: Int): TrainingProgramDto
}
