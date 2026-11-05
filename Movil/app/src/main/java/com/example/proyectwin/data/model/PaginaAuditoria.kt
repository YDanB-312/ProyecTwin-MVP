package com.example.proyectwin.data.model

/** Página de bitácora devuelta por `GET /audit-logs?paginado=1`. */
data class PaginaAuditoria(
    val registros: List<AuditLog>,
    val pagina: Int,
    val totalPaginas: Int,
    val totalRegistros: Int,
)
