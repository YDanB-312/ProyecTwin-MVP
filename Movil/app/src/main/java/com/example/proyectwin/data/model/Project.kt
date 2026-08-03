package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val title: String,
    val description: String = "",
    val estado: String = ProjectStatus.PENDIENTE.value,
    val studentId: Int? = null,
    val instructorId: Int? = null,
    val fichaId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val studentName: String? = null,
    val instructorName: String? = null
) {
    val projectStatus: ProjectStatus get() = ProjectStatus.fromValue(estado)
    val statusDisplay: String get() = when (projectStatus) {
        ProjectStatus.EN_PROGRESO -> "En Progreso"
        ProjectStatus.COMPLETADO -> "Completado"
        ProjectStatus.PENDIENTE -> "Pendiente"
        ProjectStatus.CANCELADO -> "Cancelado"
        ProjectStatus.APROBADO -> "Aprobado"
        ProjectStatus.RECHAZADO -> "Rechazado"
    }
}
