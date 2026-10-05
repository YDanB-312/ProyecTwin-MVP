package com.example.proyectwin.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Observación/comentario sobre una propuesta (`comments`), con hilos. */
@Serializable
data class CommentDto(
    val id: Int = 0,
    val texto: String = "",
    @SerialName("id_proyecto") val idProyecto: Int = 0,
    @SerialName("id_usuario") val idUsuario: Int = 0,
    @SerialName("respuesta_a") val respuestaA: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val user: GeneralUserDto? = null,
    val replies: List<CommentDto>? = null,
)

@Serializable
data class CommentCreateRequest(
    val texto: String,
    @SerialName("id_proyecto") val idProyecto: Int,
    @SerialName("respuesta_a") val respuestaA: Int? = null,
)

@Serializable
data class CommentUpdateRequest(
    val texto: String,
    @SerialName("id_proyecto") val idProyecto: Int,
    @SerialName("respuesta_a") val respuestaA: Int?,
)
