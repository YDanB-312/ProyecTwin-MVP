package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorDto(
    val id: Int = 0,
    @SerialName("fecha_ingreso") val fechaIngreso: String? = null,
    @SerialName("id_usuario") val idUsuario: Int = 0,
    val generalUser: GeneralUserDto? = null,
)

@Serializable
data class InstructorCreateRequest(
    @SerialName("fecha_ingreso") val fechaIngreso: String,
    @SerialName("id_usuario") val idUsuario: Int,
)

@Serializable
data class InstructorUpdateRequest(
    @SerialName("fecha_ingreso") val fechaIngreso: String,
)

@Serializable
data class ApprenticeDto(
    val id: Int = 0,
    val codigo: String = "",
    @SerialName("id_class_group") val idClassGroup: Int? = null,
    @SerialName("id_usuario") val idUsuario: Int = 0,
    @SerialName("id_programa") val idPrograma: Int? = null,
    val generalUser: GeneralUserDto? = null,
    val classGroup: ClassGroupDto? = null,
)

@Serializable
data class ApprenticeCreateRequest(
    val codigo: String,
    @SerialName("id_usuario") val idUsuario: Int,
    @SerialName("id_class_group") val idClassGroup: Int? = null,
    @SerialName("id_programa") val idPrograma: Int? = null,
)

/**
 * PUT /apprentices: envía el objeto completo (como el frontend). Pasar
 * `idClassGroup`/`idPrograma` en null saca al aprendiz de su ficha.
 */
@Serializable
data class ApprenticeUpdateRequest(
    val codigo: String,
    @SerialName("id_usuario") val idUsuario: Int,
    @SerialName("id_class_group") val idClassGroup: Int?,
    @SerialName("id_programa") val idPrograma: Int?,
)

@Serializable
data class JoinFichaRequest(val codigo: String)
