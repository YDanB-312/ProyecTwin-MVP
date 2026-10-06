package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.ApprenticeProjectDto
import com.example.proyectwin.data.api.dto.MessageResponse
import com.example.proyectwin.data.api.dto.ProjectCreateRequest
import com.example.proyectwin.data.api.dto.ProjectDto
import com.example.proyectwin.data.api.dto.ProjectHistoryDto
import com.example.proyectwin.data.api.dto.ProjectUpdateRequest
import com.example.proyectwin.data.api.dto.TeamMemberRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Propuestas (`/v1/projects`) y su equipo (`/v1/apprentice-projects`). */
interface ProjectsApi {

    @GET("projects")
    suspend fun listar(
        @Query("search") search: String? = null,
        @Query("estado") estado: String? = null,
        @Query("ficha_id") fichaId: Int? = null,
        @Query("programa") programa: String? = null,
        @Query("included") included: String? = null,
    ): List<ProjectDto>

    @GET("projects/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): ProjectDto

    @POST("projects")
    suspend fun crear(@Body body: ProjectCreateRequest): ProjectDto

    /** Envío/reenvío explícito: el servidor valida, ejecuta el motor y notifica. */
    @POST("projects/{id}/enviar")
    suspend fun enviar(@Path("id") id: Int): ProjectDto

    @GET("projects/{id}/historial")
    suspend fun historial(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): List<ProjectHistoryDto>

    @PUT("projects/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: ProjectUpdateRequest): ProjectDto

    @DELETE("projects/{id}")
    suspend fun eliminar(@Path("id") id: Int): ProjectDto

    @GET("apprentice-projects")
    suspend fun listarEquipo(@Query("included") included: String? = null): List<ApprenticeProjectDto>

    @POST("apprentice-projects")
    suspend fun agregarAlEquipo(@Body body: TeamMemberRequest): ApprenticeProjectDto

    /** Idempotente: si la fila ya no existe responde con un mensaje. */
    @DELETE("apprentice-projects/{id}")
    suspend fun quitarDelEquipo(@Path("id") id: Int): MessageResponse
}
