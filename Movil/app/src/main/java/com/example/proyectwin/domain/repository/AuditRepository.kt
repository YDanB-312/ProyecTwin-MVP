package com.example.proyectwin.domain.repository

import com.example.proyectwin.data.model.AuditLog

/** Bitácora de auditoría (`audit-logs`, solo lectura y rol admin). */
interface AuditRepository {

    suspend fun listar(
        accion: String? = null,
        entidad: String? = null,
        idUsuario: Int? = null,
        desde: String? = null,
        hasta: String? = null,
    ): Result<List<AuditLog>>
}
