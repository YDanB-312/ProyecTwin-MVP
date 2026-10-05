package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.BugReportCreateRequest
import com.example.proyectwin.data.api.dto.BugReportDto
import com.example.proyectwin.data.api.dto.BugReportUpdateRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Reportes de falla (`/v1/bug-reports`). */
interface BugReportsApi {

    @GET("bug-reports")
    suspend fun listar(@Query("included") included: String? = null): List<BugReportDto>

    @GET("bug-reports/{id}")
    suspend fun obtener(
        @Path("id") id: Int,
        @Query("included") included: String? = null,
    ): BugReportDto

    @POST("bug-reports")
    suspend fun crear(@Body body: BugReportCreateRequest): BugReportDto

    @PUT("bug-reports/{id}")
    suspend fun actualizar(@Path("id") id: Int, @Body body: BugReportUpdateRequest): BugReportDto

    @DELETE("bug-reports/{id}")
    suspend fun eliminar(@Path("id") id: Int): BugReportDto
}
