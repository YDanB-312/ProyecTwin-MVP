package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.data.model.PaginaAuditoria

/** Bitácora de auditoría (`audit-logs`, solo lectura y rol admin). */
interface AuditRepository {

    suspend fun listar(
        accion: String? = null,
        entidad: String? = null,
        idUsuario: Int? = null,
        desde: String? = null,
        hasta: String? = null,
    ): Result<List<AuditLog>>

    /** Paginación y filtros resueltos por el servidor. */
    suspend fun listarPaginado(
        page: Int,
        porPagina: Int,
        buscar: String? = null,
        accion: String? = null,
        entidad: String? = null,
        idUsuario: Int? = null,
        rol: String? = null,
        desde: String? = null,
        hasta: String? = null,
        orden: String? = null,
    ): Result<PaginaAuditoria>
}
