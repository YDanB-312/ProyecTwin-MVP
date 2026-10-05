package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reporte de falla (`bug_reports`). */
@Serializable
data class BugReportDto(
    val id: Int = 0,
    val titulo: String? = null,
    val descripcion: String = "",
    val tipo: String = "otro",
    val estado: String = "pendiente",
    val fecha: String? = null,
    @SerialName("id_usuario") val idUsuario: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val generalUser: GeneralUserDto? = null,
)

@Serializable
data class BugReportCreateRequest(
    val titulo: String? = null,
    val descripcion: String,
    val tipo: String,
    val estado: String = "pendiente",
    val fecha: String,
    @SerialName("id_usuario") val idUsuario: Int? = null,
)

/** PUT /bug-reports: objeto completo (cambio de estado desde el admin). `fecha` es obligatoria en el backend. */
@Serializable
data class BugReportUpdateRequest(
    val titulo: String?,
    val descripcion: String,
    val tipo: String,
    val estado: String,
    val fecha: String,
    @SerialName("id_usuario") val idUsuario: Int,
)
