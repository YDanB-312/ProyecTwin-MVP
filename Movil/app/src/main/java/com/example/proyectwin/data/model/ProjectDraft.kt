package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

/**
 * Borrador de propuesta para crear/editar en la API: mapea 1:1 con el payload
 * `payloadProyecto()` del frontend (POST/PUT /projects).
 */
@Serializable
data class ProjectDraft(
    val titulo: String,
    val resumen: String,
    val palabrasClave: String? = null,
    val areaAplicacion: String = "",
    val objetivoGeneral: String? = null,
    val objetivosEspecificos: List<String> = emptyList(),
    val estado: String = ProjectStatus.PENDIENTE.value,
    val idCreador: Int = 0,
    val idInstructorAsignado: Int? = null,
    val idClassGroup: Int? = null
)
