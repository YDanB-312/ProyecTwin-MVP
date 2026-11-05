package com.example.proyectwin.data.api.service

import com.example.proyectwin.data.api.dto.AuditLogDto
import com.example.proyectwin.data.api.dto.PaginaAuditoriaDto
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

    @GET("audit-logs")
    suspend fun listarPaginado(
        @Query("paginado") paginado: Boolean = true,
        @Query("page") page: Int,
        @Query("por_pagina") porPagina: Int,
        @Query("buscar") buscar: String? = null,
        @Query("accion") accion: String? = null,
        @Query("entidad") entidad: String? = null,
        @Query("id_usuario") idUsuario: Int? = null,
        @Query("rol") rol: String? = null,
        @Query("desde") desde: String? = null,
        @Query("hasta") hasta: String? = null,
        @Query("orden") orden: String? = null,
        @Query("included") included: String? = null,
    ): PaginaAuditoriaDto
}
