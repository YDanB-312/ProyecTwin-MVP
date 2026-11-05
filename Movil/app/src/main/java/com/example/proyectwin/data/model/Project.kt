package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Int,
    val title: String,
    val description: String = "",
    val estado: String = ProjectStatus.BORRADOR.value,
    val studentId: Int? = null,
    val instructorId: Int? = null,
    val fichaId: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val studentName: String? = null,
    val instructorName: String? = null,
    val palabrasClave: String? = null,
    val areaAplicacion: String = "",
    val objetivoGeneral: String? = null,
    val objetivosEspecificos: List<String> = emptyList(),
    val programa: String? = null,
    val fichaEstado: String? = null,
    /** Estado del cálculo de similitud; null en respuestas antiguas. */
    val similitudEstado: String? = null,
    val similitudActualizadaEn: String? = null,
    val equipo: List<GeneralUser> = emptyList()
) {
    val projectStatus: ProjectStatus get() = ProjectStatus.fromValue(estado)
    val statusDisplay: String get() = when (projectStatus) {
        ProjectStatus.BORRADOR -> "Borrador"
        ProjectStatus.PENDIENTE -> "En revisión"
        ProjectStatus.APROBADO -> "Aprobado"
        ProjectStatus.RECHAZADO -> "Rechazado"
    }

    /** Estados en los que el aprendiz puede editar (regla del backend). */
    val esEditable: Boolean get() = projectStatus != ProjectStatus.APROBADO

    /** Solo borrador y rechazado pueden enviarse/reenviarse. */
    val puedeEnviar: Boolean get() =
        projectStatus == ProjectStatus.BORRADOR || projectStatus == ProjectStatus.RECHAZADO

    val similitudPendiente: Boolean get() =
        similitudEstado == "pendiente" || similitudEstado == "en_proceso"

    val similitudConError: Boolean get() = similitudEstado == "error"

    val sinReferenciasSimilitud: Boolean get() = similitudEstado == "sin_referencias"

    /** Compatibilidad: respuestas antiguas sin campo se consideran aprobables. */
    val puedeAprobarConSimilitud: Boolean get() =
        similitudConError.not() && similitudPendiente.not()
}

/**
 * Una propuesta es del usuario si la creó o si figura en su equipo. El backend
 * devuelve al aprendiz también las propuestas de su ficha; la UI de "Mis
 * proyectos" debe filtrarlas igual que la web.
 */
fun Project.perteneceA(usuarioId: Int?): Boolean =
    usuarioId != null && (studentId == usuarioId || equipo.any { it.id == usuarioId })
