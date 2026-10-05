package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

/** Entrada de bitácora de auditoría (`audit-logs`, solo lectura). */
@Serializable
data class AuditLog(
    val id: Int,
    val accion: String,
    val entidad: String? = null,
    val entidadId: Int? = null,
    val usuarioId: Int? = null,
    val usuarioNombre: String? = null,
    val detalle: String? = null,
    val ip: String? = null,
    val createdAt: String? = null
)
