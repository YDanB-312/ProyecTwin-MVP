package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.AgregarAprendizRequest
import com.example.proyectwin.data.api.dto.AgregarAprendizResponse
import com.example.proyectwin.data.api.dto.ClassGroupCreateRequest
import com.example.proyectwin.data.api.dto.ClassGroupDeleteResponse
import com.example.proyectwin.data.api.dto.ClassGroupDto
import com.example.proyectwin.data.api.dto.ClassGroupUpdateRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Fichas de formación (`/v1/class-groups`). */
interface ClassGroupsApi {

    @GET("class-groups")
    suspend fun listar(@Query("included") included: String? = null): List<ClassGroupDto>

    @GET("class-groups/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): ClassGroupDto

    @POST("class-groups")
    suspend fun crear(@Body body: ClassGroupCreateRequest): ClassGroupDto

    @PUT("class-groups/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: ClassGroupUpdateRequest): ClassGroupDto

    /** Asocia una cuenta o crea el aprendiz y lo matricula (admin/instructor). */
    @POST("class-groups/{id}/aprendices")
    suspend fun agregarAprendiz(
        @Path("id") id: Int,
        @Body body: AgregarAprendizRequest,
    ): AgregarAprendizResponse

    /** PDF de credenciales de la ficha (solo admin). */
    @GET("class-groups/{id}/credenciales")
    suspend fun credencialesPdf(@Path("id") id: Int): ResponseBody

    @DELETE("class-groups/{id}")
    suspend fun eliminar(@Path("id") id: Int): ClassGroupDeleteResponse
}
