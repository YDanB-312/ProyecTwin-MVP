package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.AuditLogDto
import retrofit2.http.GET
import retrofit2.http.Query

/** Bitácora de auditoría, solo lectura (`/v1/audit-logs`, rol admin). */
interface AuditApi {

    @GET("audit-logs")
    suspend fun listar(
        @Query("accion") accion: String? = null,
        @Query("entidad") entidad: String? = null,
        @Query("id_usuario") idUsuario: Int? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("included") included: String? = null,
    ): List<AuditLogDto>
}
