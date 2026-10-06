package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Propuesta de proyecto (`projects`), tal como la responde Laravel. */
@Serializable
data class ProjectDto(
    val id: Int = 0,
    val titulo: String = "",
    val resumen: String = "",
    @SerialName("palabras_clave") val palabrasClave: String? = null,
    @SerialName("area_aplicacion") val areaAplicacion: String = "",
    @SerialName("objetivo_general") val objetivoGeneral: String? = null,
    @SerialName("objetivos_especificos") val objetivosEspecificos: List<String>? = null,
    val estado: String = "pendiente",
    @SerialName("id_creador") val idCreador: Int = 0,
    @SerialName("id_instructor_asignado") val idInstructorAsignado: Int? = null,
    @SerialName("id_class_group") val idClassGroup: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val creator: GeneralUserDto? = null,
    val instructor: InstructorDto? = null,
    val classGroup: ClassGroupDto? = null,
    val apprentices: List<ApprenticeDto>? = null,
    val comments: List<CommentDto>? = null,
)

/**
 * POST /projects: replica el payload de "Nueva Propuesta" del frontend.
 * Los campos en null viajan explícitos (`encodeDefaults=false` omite todo lo
 * que tenga valor por defecto; aquí nada lo tiene).
 */
@Serializable
data class ProjectCreateRequest(
    val titulo: String,
    val resumen: String,
    @SerialName("palabras_clave") val palabrasClave: String?,
    @SerialName("area_aplicacion") val areaAplicacion: String,
    @SerialName("objetivo_general") val objetivoGeneral: String?,
    @SerialName("objetivos_especificos") val objetivosEspecificos: List<String>?,
    val estado: String,
    @SerialName("id_creador") val idCreador: Int,
    @SerialName("id_instructor_asignado") val idInstructorAsignado: Int?,
    @SerialName("id_class_group") val idClassGroup: Int?,
)

/**
 * PUT /projects: objeto completo con los 10 campos escalares que exige el
 * backend (equivalente a `payloadProyecto()` del frontend).
 */
@Serializable
data class ProjectUpdateRequest(
    val titulo: String,
    val resumen: String,
    @SerialName("palabras_clave") val palabrasClave: String?,
    @SerialName("area_aplicacion") val areaAplicacion: String,
    @SerialName("objetivo_general") val objetivoGeneral: String?,
    @SerialName("objetivos_especificos") val objetivosEspecificos: List<String>?,
    val estado: String,
    @SerialName("id_creador") val idCreador: Int,
    @SerialName("id_instructor_asignado") val idInstructorAsignado: Int?,
    @SerialName("id_class_group") val idClassGroup: Int?,
    /** Observación del instructor al rechazar (opcional; null no viaja). */
    val observacion: String? = null,
)

/** Fila del historial de la propuesta (`GET /projects/{id}/historial`). */
@Serializable
data class ProjectHistoryDto(
    val id: Int = 0,
    @SerialName("id_proyecto") val idProyecto: Int = 0,
    @SerialName("id_usuario") val idUsuario: Int? = null,
    val accion: String = "",
    val detalle: JsonObject? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val user: GeneralUserDto? = null,
)

/** Fila del pivote `apprentice_projects` (equipo de la propuesta). */
@Serializable
data class ApprenticeProjectDto(
    val id: Int = 0,
    @SerialName("id_aprendiz") val idAprendiz: Int = 0,
    @SerialName("id_proyecto") val idProyecto: Int = 0,
    val apprentice: ApprenticeDto? = null,
    val project: ProjectDto? = null,
)

@Serializable
data class TeamMemberRequest(
    @SerialName("id_aprendiz") val idAprendiz: Int,
    @SerialName("id_proyecto") val idProyecto: Int,
)
