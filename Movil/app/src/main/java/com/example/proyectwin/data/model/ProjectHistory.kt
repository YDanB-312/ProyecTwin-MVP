package com.example.proyectwin.data.model

/** Acción del historial de una propuesta (`GET /projects/{id}/historial`). */
data class ProjectHistory(
    val id: Int,
    val accion: String,
    val detalle: String? = null,
    val detalleMap: Map<String, String> = emptyMap(),
    val usuarioNombre: String? = null,
    val createdAt: String? = null,
) {
    val accionDisplay: String get() = when (accion) {
        "creada" -> "Propuesta creada"
        "enviada" -> "Enviada a revisión"
        "reenviada" -> "Reenviada a revisión"
        "aprobada" -> "Aprobada"
        "rechazada" -> "Rechazada"
        "devuelta_revision" -> "Devuelta a revisión"
        "actualizada" -> "Actualizada"
        else -> accion.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /** Observación registrada en la acción (rechazo), si existe. */
    val observacion: String? get() = detalleMap["observacion"]
}
