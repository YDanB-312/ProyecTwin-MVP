package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.ApprenticeCreateRequest
import com.example.proyectwin.data.api.dto.ApprenticeDto
import com.example.proyectwin.data.api.dto.ApprenticeUpdateRequest
import com.example.proyectwin.data.api.dto.ClassGroupDto
import com.example.proyectwin.data.api.dto.InstructorCreateRequest
import com.example.proyectwin.data.api.dto.InstructorDto
import com.example.proyectwin.data.api.dto.InstructorUpdateRequest
import com.example.proyectwin.data.api.dto.JoinFichaRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Perfiles de instructor (`/v1/instructors`). */
interface InstructorsApi {

    @GET("instructors")
    suspend fun listar(@Query("included") included: String? = null): List<InstructorDto>

    @GET("instructors/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): InstructorDto

    @POST("instructors")
    suspend fun crear(@Body body: InstructorCreateRequest): InstructorDto

    @PUT("instructors/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: InstructorUpdateRequest): InstructorDto

    @DELETE("instructors/{id}")
    suspend fun eliminar(@Path("id") id: Int): InstructorDto
}

/** Perfiles de aprendiz y gestión de MI ficha (`/v1/apprentices`). */
interface ApprenticesApi {

    @GET("apprentices")
    suspend fun listar(@Query("included") included: String? = null): List<ApprenticeDto>

    @GET("apprentices/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): ApprenticeDto

    @POST("apprentices")
    suspend fun crear(@Body body: ApprenticeCreateRequest): ApprenticeDto

    @PUT("apprentices/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: ApprenticeUpdateRequest): ApprenticeDto

    @DELETE("apprentices/{id}")
    suspend fun eliminar(@Path("id") id: Int): ApprenticeDto

    @GET("apprentices/me/ficha/codigo/{codigo}")
    suspend fun fichaPorCodigo(@Path("codigo") codigo: String): ClassGroupDto

    @POST("apprentices/me/ficha")
    suspend fun unirmeAFicha(@Body body: JoinFichaRequest): ApprenticeDto

    @DELETE("apprentices/me/ficha")
    suspend fun salirDeFicha(): ApprenticeDto
}
