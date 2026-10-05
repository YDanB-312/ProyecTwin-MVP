package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Ficha de formación (`class_groups`). */
@Serializable
data class ClassGroupDto(
    val id: Int = 0,
    val codigo: String = "",
    val numero: String? = null,
    val nombre: String = "",
    val estado: String = "activo",
    @SerialName("id_programa") val idPrograma: Int = 0,
    @SerialName("id_instructor") val idInstructor: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val program: TrainingProgramDto? = null,
    val instructor: InstructorDto? = null,
    val apprentices: List<ApprenticeDto>? = null,
    @SerialName("apprentices_count") val apprenticesCount: Int? = null,
)

@Serializable
data class ClassGroupCreateRequest(
    val codigo: String,
    val numero: String? = null,
    val nombre: String,
    val estado: String,
    @SerialName("id_programa") val idPrograma: Int,
    @SerialName("id_instructor") val idInstructor: Int,
)

/** PUT /class-groups: objeto completo (como el frontend); `numero` en null limpia el campo. */
@Serializable
data class ClassGroupUpdateRequest(
    val codigo: String,
    val numero: String?,
    val nombre: String,
    val estado: String,
    @SerialName("id_programa") val idPrograma: Int,
    @SerialName("id_instructor") val idInstructor: Int,
)
