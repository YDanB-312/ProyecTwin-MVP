package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.GeneralUserDto
import com.example.proyectwin.data.api.dto.PerfilDto
import com.example.proyectwin.data.api.dto.UserCreateRequest
import com.example.proyectwin.data.api.dto.UserUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Cuentas de usuario (`/v1/general-users`). */
interface UsersApi {

    @GET("general-users")
    suspend fun listar(
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("estado") estado: String? = null,
        @Query("ficha_id") fichaId: Int? = null,
        @Query("programa") programa: String? = null,
        @Query("included") included: String? = null,
    ): List<GeneralUserDto>

    @GET("general-users/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): GeneralUserDto

    @GET("general-users/{id}/perfil")
    suspend fun perfil(@Path("id") id: Int): PerfilDto

    @POST("general-users")
    suspend fun crear(@Body body: UserCreateRequest): GeneralUserDto

    @PUT("general-users/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: UserUpdateRequest): GeneralUserDto

    @DELETE("general-users/{id}")
    suspend fun eliminar(@Path("id") id: Int): GeneralUserDto
}
