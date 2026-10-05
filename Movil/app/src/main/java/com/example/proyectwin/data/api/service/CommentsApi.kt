package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.CommentCreateRequest
import com.example.proyectwin.data.api.dto.CommentDto
import com.example.proyectwin.data.api.dto.CommentUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Observaciones sobre propuestas, con hilos de respuesta (`/v1/comments`). */
interface CommentsApi {

    @GET("comments")
    suspend fun listar(
        @Query("id_proyecto") idProyecto: Int? = null,
        @Query("included") included: String? = null,
    ): List<CommentDto>

    @GET("comments/{id}")
    suspend fun obtener(@Path("id") id: Int): CommentDto

    @POST("comments")
    suspend fun crear(@Body body: CommentCreateRequest): CommentDto

    @PUT("comments/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: CommentUpdateRequest): CommentDto

    @DELETE("comments/{id}")
    suspend fun eliminar(@Path("id") id: Int): CommentDto
}
