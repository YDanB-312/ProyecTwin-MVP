package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

/** Observación sobre una propuesta con hilo de respuestas (`comments`). */
@Serializable
data class Comment(
    val id: Int,
    val texto: String,
    val proyectoId: Int,
    val usuarioId: Int,
    val respuestaA: Int? = null,
    val autorNombre: String? = null,
    val autorFoto: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val respuestas: List<Comment> = emptyList()
)
